package com.example.cucucook.controller;

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

import com.example.cucucook.config.JwtTokenProvider;
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

  private final ConcurrentHashMap<String, Long> requestCache = new ConcurrentHashMap<>();
  private static final long DUPLICATE_TIMEOUT = TimeUnit.MINUTES.toMillis(1); // 1분 타임아웃

  private boolean isDuplicateRequest(String code) {
    Long lastTime = requestCache.putIfAbsent(code, System.currentTimeMillis());
    if (lastTime != null) {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - lastTime) < DUPLICATE_TIMEOUT) {
        return true; // 이는 타임아웃 기간 내 중복 요청임을 의미합니다
      } else {
        // 새 시간으로 업데이트하고 요청을 허용합니다
        requestCache.put(code, currentTime);
        return false;
      }
    }
    return false;
  }

  @PostMapping("/kakao/login")
  public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code) {
    logger.info("카카오 로그인 요청 받음, 인증 코드: {}", code);

    // 중복 요청 확인 로직 추가 예시
    if (isDuplicateRequest(code)) {
      logger.info("중복 요청 감지, 인증 코드: {}", code);
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate request detected");
    }

    try {
      String accessToken = getAccessToken(code);
      if (accessToken == null) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("액세스 토큰을 가져오는 데 실패했습니다.");
      }

      SocialLogin socialLogin = getKakaoUserInfo(accessToken);
      if (socialLogin == null) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보를 가져오는 데 실패했습니다.");
      }

      // 사용자 정보 반환
      return ResponseEntity.ok().body(socialLogin);
    } catch (Exception e) {
      logger.error("서버 오류 발생: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생: " + e.getMessage());
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
    // 닉네임,프로필사진,이메일 수집 동의를 추가합니다.
    params.add("scope", "account_email, profile_nickname, profile_image");

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
