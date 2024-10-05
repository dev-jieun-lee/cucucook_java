package com.example.cucucook.controller;

import java.util.Map;

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

import com.example.cucucook.config.JwtTokenProvider;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.SocialLogin;
import com.example.cucucook.service.SocialLoginService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class KakaoAuthController {

  private final RestTemplate restTemplate;
  private static final Logger logger = LoggerFactory.getLogger(KakaoAuthController.class);

  @Value("${kakao.client.id}")
  private String kakaoClientId;

  @Value("${kakao.redirect.uri}")
  private String redirectUri;

  @Value("${kakao.client.secret}")
  private String clientSecret;

  private final SocialLoginService socialLoginService;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public KakaoAuthController(RestTemplate restTemplate, SocialLoginService socialLoginService,
      JwtTokenProvider jwtTokenProvider) {
    this.restTemplate = restTemplate;
    this.socialLoginService = socialLoginService;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @PostMapping("/kakao/login")
  public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code) {
    logger.info("카카오 로그인 요청 받음, 인증 코드: {}", code);

    if (code == null || code.trim().isEmpty()) {
      logger.warn("받은 인증 코드가 유효하지 않거나 비어 있습니다.");
      return ResponseEntity.badRequest().body("인증 코드가 필요합니다.");
    }

    try {
      String accessToken = getAccessToken(code);
      if (accessToken == null) {
        logger.error("액세스 토큰을 가져오는 데 실패했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("액세스 토큰을 가져오는 데 실패했습니다.");
      }

      // 사용자 정보를 가져오기
      SocialLogin socialLogin = getKakaoUserInfo(accessToken);
      if (socialLogin == null) {
        logger.error("사용자 정보를 가져오는 데 실패했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보를 가져오는 데 실패했습니다.");
      }

      // socialId를 Long에서 String으로 변환
      socialLogin.setProviderId(String.valueOf(socialLogin.getProviderId()));

      // 기존 멤버 검색 및 생성
      Member member = socialLoginService.getOrCreateMember(socialLogin);
      String jwtToken = jwtTokenProvider.createToken(String.valueOf(member.getMemberId()), "ROLE_USER");

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);

      return ResponseEntity.ok().headers(headers).body(member);
    } catch (HttpClientErrorException ex) {
      logger.error("카카오 로그인 과정 중 오류 발생, HTTP 상태: {}, 오류 내용: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
      return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
    } catch (Exception ex) {
      logger.error("카카오 로그인 처리 중 예상치 못한 오류 발생: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예상치 못한 오류가 발생했습니다.");
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
    // 이메일 수집 동의를 추가합니다.
    params.add("scope", "account_email, profile");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return (String) response.getBody().get("access_token");
      }
    } catch (HttpClientErrorException ex) {
      logger.error("액세스 토큰 요청 중 오류 발생, HTTP 상태: {}, 응답 내용: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
      throw ex;
    }
    return null;
  }

  private SocialLogin getKakaoUserInfo(String accessToken) {
    String url = "https://kapi.kakao.com/v2/user/me";
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

      // 응답 확인
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> userInfo = response.getBody();
        logger.info("카카오 사용자 정보: {}", userInfo); // 사용자 정보 로그 출력

        // SocialLogin 객체 생성 및 정보 설정
        SocialLogin socialLogin = new SocialLogin();
        socialLogin.setProviderId(String.valueOf(userInfo.get("id"))); // 사용자 ID 설정
        // 이메일이 존재하는 경우 이메일 설정
        if (userInfo.containsKey("kakao_account")) {
          Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
          String kakaoEmail = (String) kakaoAccount.get("email"); // 변수명을 email에서 kakaoEmail로 변경
          socialLogin.setEmail(kakaoEmail);
        }

        return socialLogin;
      } else {
        logger.error("사용자 정보를 가져오는 데 실패했습니다. HTTP 상태: {}, 응답 내용: {}", response.getStatusCode(), response.getBody());
      }
    } catch (Exception ex) {
      logger.error("카카오 사용자 정보를 가져오는 중 오류 발생: {}", ex.getMessage());
    }
    return null;
  }

}
