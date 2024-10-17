package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.SocialLogin;
import com.example.cucucook.service.SocialLoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class SocialLoginController {
  private final SocialLoginService socialLoginService;

  private final RestTemplate restTemplate;
  private static final Logger logger = LoggerFactory.getLogger(SocialLoginController.class);

  @Value("${kakao.client.id}")
  private String kakaoClientId;

  @Value("${kakao.redirect.uri}")
  private String redirectUri;

  @Value("${kakao.client.secret}")
  private String clientSecret;

  @Value("${naver.client.id}")
  private String naverClientId;

  @Value("${naver.redirect.uri}")
  private String naverRedirectUri;

  @Value("${naver.client.secret}")
  private String naverClientSecret;

  private static final long DUPLICATE_TIMEOUT = TimeUnit.MINUTES.toMillis(1); // 1분 타임아웃
  private final ConcurrentHashMap<String, Long> requestCache = new ConcurrentHashMap<>();

  @Autowired
  public SocialLoginController(RestTemplate restTemplate, SocialLoginService socialLoginService) {
    this.restTemplate = restTemplate;
    this.socialLoginService = socialLoginService;
  }

  // 중복 요청 감지 메서드
  private boolean isDuplicateRequest(String code) {
    Long lastTime = requestCache.putIfAbsent(code, System.currentTimeMillis());
    if (lastTime != null) {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - lastTime) < DUPLICATE_TIMEOUT) {
        return true; // 이는 타임아웃 기간 내 중복 요청임을 의미합니다.
      } else {
        // 새 시간으로 업데이트하고 요청을 허용합니다.
        requestCache.put(code, currentTime);
        return false;
      }
    }
    return false;
  }

  @PostMapping("/kakao/login")
  public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response,
      HttpServletRequest request) {
    logger.info("카카오 로그인 요청 받음, 인증 코드: {}", code);

    if (isDuplicateRequest(code)) {
      logger.warn("중복 요청 감지, 인증 코드: {}", code);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("중복 요청입니다. 잠시 후 다시 시도하세요.");
    }

    try {
      String accessToken = getAccessToken(code);
      if (accessToken == null) {
        logger.error("카카오 액세스 토큰을 가져오는 데 실패했습니다.");
        return ResponseEntity.badRequest().body("액세스 토큰을 가져오는 데 실패했습니다.");
      }

      // 사용자 정보 가져오기
      SocialLogin socialLogin = getKakaoUserInfo(accessToken);
      if (socialLogin == null) {
        logger.error("카카오 사용자 정보를 가져오는 데 실패했습니다.");
        return ResponseEntity.badRequest().body("사용자 정보를 가져오는 데 실패했습니다.");
      }

      // 회원 정보 생성 또는 가져오기
      Member member = socialLoginService.getOrCreateMember(socialLogin);

      // 1. 토큰 생성 및 저장
      Map<String, String> tokens = socialLoginService.saveTokensForMember(member);

      // 2. 사용자 정보와 함께 두 개의 토큰을 반환
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("member", member);
      responseBody.put("accessToken", tokens.get("accessToken"));
      responseBody.put("refreshToken", tokens.get("refreshToken"));

      logger.info("카카오 로그인 성공: userId: {}, accessToken: {}, refreshToken: {}", member.getUserId(),
          tokens.get("accessToken"), tokens.get("refreshToken"));
      return ResponseEntity.ok(responseBody);

    } catch (Exception ex) {
      logger.error("카카오 로그인 처리 중 오류 발생: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류가 발생했습니다.");
    }
  }

  private String getAccessToken(String code) {
    String url = "https://kauth.kakao.com/oauth/token";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", kakaoClientId);
    params.add("redirect_uri", redirectUri);
    params.add("code", code);
    params.add("client_secret", clientSecret);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
    return response.getBody() != null ? (String) response.getBody().get("access_token") : null;
  }

  public SocialLogin getKakaoUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);

    HttpEntity<String> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(
          "https://kapi.kakao.com/v2/user/me",
          HttpMethod.GET,
          request,
          Map.class);

      if (response.getBody() != null) {
        Map<String, Object> userInfo = response.getBody();
        SocialLogin socialLogin = new SocialLogin();
        socialLogin.setProvider("kakao");
        socialLogin.setProviderId(String.valueOf(userInfo.get("id"))); // 카카오 ID
        socialLogin.setNickname((String) ((Map) userInfo.get("properties")).get("nickname")); // 사용자 이름
        socialLogin.setEmail((String) ((Map) userInfo.get("kakao_account")).get("email")); // 사용자 이메일
        socialLogin.setSocialLogin(true); // 소셜 로그인 플래그 설정

        return socialLogin; // 성공적으로 가져왔을 때 SocialLogin 객체 반환
      }
    } catch (HttpClientErrorException e) {
      logger.error("카카오 사용자 정보 요청 중 오류 발생: {}", e.getResponseBodyAsString());
      throw new RuntimeException("카카오 사용자 정보 요청 실패"); // 예외 발생
    }

    // 모든 경로에서 반환하도록 설정 (여기에서는 예외로 대체 가능)
    throw new RuntimeException("카카오 사용자 정보를 가져올 수 없습니다.");
  }

  @PostMapping("/naver/login")
  public ResponseEntity<?> naverLogin(@RequestParam("code") String code, HttpServletResponse response,
      HttpServletRequest request) {
    logger.info("네이버 로그인 요청 받음, 인증 코드: {}", code);

    if (isDuplicateRequest(code)) {
      logger.warn("중복 요청 감지, 인증 코드: {}", code);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("중복 요청입니다. 잠시 후 다시 시도하세요.");
    }

    try {
      String accessToken = getNaverAccessToken(code);
      if (accessToken == null) {
        logger.error("네이버 액세스 토큰을 가져오는 데 실패했습니다.");
        return ResponseEntity.badRequest().body("액세스 토큰을 가져오는 데 실패했습니다.");
      }

      // 사용자 정보 가져오기
      SocialLogin socialLogin = getNaverUserInfo(accessToken);
      if (socialLogin == null) {
        logger.error("네이버 사용자 정보를 가져오는 데 실패했습니다.");
        return ResponseEntity.badRequest().body("사용자 정보를 가져오는 데 실패했습니다.");
      }

      // 사용자 정보 로그
      logger.info("네이버 사용자 정보: {}", socialLogin);

      // 회원 정보 생성 또는 가져오기
      Member member = socialLoginService.getOrCreateMember(socialLogin);

      // 1. 토큰 생성 및 저장
      Map<String, String> tokens = socialLoginService.saveTokensForMember(member);

      // 2. 사용자 정보와 함께 두 개의 토큰을 반환
      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("member", member);
      responseBody.put("accessToken", tokens.get("accessToken"));
      responseBody.put("refreshToken", tokens.get("refreshToken"));

      logger.info("네이버 로그인 성공: userId: {}, accessToken: {}, refreshToken: {}", member.getUserId(),
          tokens.get("accessToken"), tokens.get("refreshToken"));
      return ResponseEntity.ok(responseBody);
    } catch (Exception ex) {
      logger.error("네이버 로그인 처리 중 오류 발생: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류가 발생했습니다.");
    }
  }

  private String getNaverAccessToken(String code) {
    String url = "https://nid.naver.com/oauth2.0/token";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", naverClientId);
    params.add("redirect_uri", naverRedirectUri);
    params.add("code", code);
    params.add("client_secret", naverClientSecret);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
      logger.info("네이버 액세스 토큰 응답: {}", response.getBody()); // 응답 로그 추가
      return response.getBody() != null ? (String) response.getBody().get("access_token") : null;
    } catch (HttpClientErrorException e) {
      logger.error("네이버 Access Token 요청 중 오류 발생: {}", e.getResponseBodyAsString());
      return null;
    }
  }

  public SocialLogin getNaverUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);

    HttpEntity<String> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(
          "https://openapi.naver.com/v1/nid/me",
          HttpMethod.GET,
          request,
          Map.class);

      logger.info("네이버 사용자 정보 응답: {}", response.getBody()); // 응답 로그 추가
      if (response.getBody() != null) {
        Map<String, Object> userInfo = response.getBody();
        logger.info("네이버 사용자 정보 응답: {}", userInfo); // 전체 응답 로그

        // userInfo에서 "response" 키의 값을 Map으로 가져오기
        Map<String, Object> responseData = (Map<String, Object>) userInfo.get("response");

        // 이메일과 다른 정보 출력
        logger.info("사용자 이메일: {}", responseData.get("email")); // 이메일 출력

        SocialLogin socialLogin = new SocialLogin();
        socialLogin.setProvider("naver");
        socialLogin.setProviderId(String.valueOf(responseData.get("id"))); // 네이버 ID
        socialLogin.setNickname((String) responseData.get("nickname")); // 사용자 이름
        socialLogin.setEmail((String) responseData.get("email")); // 사용자 이메일
        socialLogin.setPhone((String) responseData.get("mobile")); // 모바일 번호
        socialLogin.setSocialLogin(true); // 소셜 로그인 플래그 설정

        return socialLogin; // 성공적으로 가져왔을 때 SocialLogin 객체 반환
      }
    } catch (HttpClientErrorException e) {
      logger.error("네이버 사용자 정보 요청 중 오류 발생: {}", e.getResponseBodyAsString());
    }

    return null; // 실패 시 null 반환
  }

}