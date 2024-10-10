package com.example.cucucook.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.cucucook.controller.SocialLoginController;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.SocialLogin;
import com.example.cucucook.domain.Token;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.mapper.SocialLoginMapper;
import com.example.cucucook.mapper.TokenMapper;
import com.example.cucucook.service.SocialLoginService;

@Service
public class SocialLoginServiceImpl implements SocialLoginService {

  private final RestTemplate restTemplate;
  private final MemberMapper memberMapper;
  private final SocialLoginMapper socialLoginMapper;
  private final TokenMapper tokenMapper;
  private static final Logger logger = LoggerFactory.getLogger(SocialLoginController.class);

  @Value("${kakao.client.id}")
  private String kakaoClientId;

  @Value("${kakao.redirect.uri}")
  private String redirectUri;

  @Value("${kakao.client.secret}")
  private String clientSecret;

  @Autowired
  public SocialLoginServiceImpl(RestTemplate restTemplate, MemberMapper memberMapper,
      SocialLoginMapper socialLoginMapper, TokenMapper tokenMapper) {
    this.restTemplate = restTemplate;
    this.memberMapper = memberMapper;
    this.socialLoginMapper = socialLoginMapper;
    this.tokenMapper = tokenMapper; // TokenMapper 초기화
  }

  @Override
  public ResponseEntity<?> kakaoLogin(String code) {
    try {
      // Your logic to get access token and user info
      String accessToken = getAccessToken(code); // Implement this method
      SocialLogin socialLogin = getKakaoUserInfo(accessToken); // Implement this method

      // Process the socialLogin object and return member info
      Member member = getOrCreateMember(socialLogin); // Implement this method
      Map<String, String> tokens = saveTokensForMember(member); // Implement this method

      return ResponseEntity.ok(Map.of(
          "member", member,
          "accessToken", tokens.get("accessToken"),
          "refreshToken", tokens.get("refreshToken")));
    } catch (Exception e) {
      logger.error("Error during Kakao login: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Kakao login processing error");
    }
  }

  @Override
  public ResponseEntity<?> naverLogin(String code) {
    logger.info("네이버 로그인 요청 받음, 인증 코드: {}", code);

    if (isDuplicateRequest(code)) {
      logger.warn("중복 요청 감지, 인증 코드: {}", code);
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("중복 요청입니다. 잠시 후 다시 시도하세요.");
    }

    try {
      String accessToken = getNaverAccessToken(code); // 네이버의 액세스 토큰을 가져오는 메서드
      if (accessToken == null) {
        logger.error("네이버 액세스 토큰을 가져오는 데 실패했습니다.");
        return ResponseEntity.badRequest().body("액세스 토큰을 가져오는 데 실패했습니다.");
      }

      // 사용자 정보 가져오기
      SocialLogin socialLogin = getNaverUserInfo(accessToken); // 네이버 사용자 정보를 가져오는 메서드
      if (socialLogin == null) {
        logger.error("네이버 사용자 정보를 가져오는 데 실패했습니다.");
        return ResponseEntity.badRequest().body("사용자 정보를 가져오는 데 실패했습니다.");
      }

      // 회원 정보 생성 또는 가져오기
      Member member = getOrCreateMember(socialLogin);
      // 1. 토큰 생성 및 저장
      Map<String, String> tokens = saveTokensForMember(member); // Implement this method

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

  private SocialLogin getNaverUserInfo(String accessToken) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getNaverUserInfo'");
  }

  private String getNaverAccessToken(String code) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getNaverAccessToken'");
  }

  private boolean isDuplicateRequest(String code) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isDuplicateRequest'");
  }

  @Override
  @Transactional
  public Member getOrCreateMember(SocialLogin socialLogin) {
    // social_login_id가 null인 경우 UUID 생성
    if (socialLogin.getSocialLoginId() == null) {
      socialLogin.setSocialLoginId(UUID.randomUUID().toString());
    }

    // 1. 이메일로 기존 회원 찾기
    Member existingMember = memberMapper.findByEmail(socialLogin.getEmail());

    // 2. 기존 회원이 존재하지 않으면 회원 정보 생성
    if (existingMember == null) {
      Member newMember = new Member();
      newMember.setUserId(socialLogin.getProviderId()); // 사용자 ID는 소셜 로그인에서 제공받은 providerId로 설정
      newMember.setEmail(socialLogin.getEmail());
      newMember.setPassword("default_password");
      newMember.setName(socialLogin.getNickname() != null ? socialLogin.getNickname() : "SocialUser");
      newMember.setSocialLogin(true); // 소셜 로그인 플래그 설정
      String mobile = (String) socialLogin.getPhone(); // phone을 String으로 변환
      newMember.setPhone(mobile != null ? mobile : "00000000000"); // 모바일 번호 설정

      memberMapper.insertMember(newMember);
      socialLogin.setMemberId(newMember.getMemberId());
      socialLoginMapper.insertSocialLogin(socialLogin);

      return newMember;
    }

    // 3. 소셜 로그인 정보 확인
    SocialLogin existingSocialLogin = socialLoginMapper.findSocialLoginByProviderId(socialLogin.getProviderId(),
        socialLogin.getProvider());

    if (existingSocialLogin == null) {
      // 4. 기존 소셜 로그인 정보가 없으면 등록
      socialLogin.setMemberId(existingMember.getMemberId());
      socialLoginMapper.insertSocialLogin(socialLogin);
    } else {
      // 5. 이미 존재하는 소셜 로그인 정보가 있으면 업데이트
      socialLoginMapper.updateSocialLogin(socialLogin);
    }

    return existingMember;
  }

  public Member createMember(SocialLogin socialLogin) {
    Member member = new Member();
    member.setUserId(socialLogin.getProviderId());
    member.setPassword("default_password"); // 소셜 로그인 시 기본 비밀번호 설정

    // 이름 필드가 비어 있을 경우 기본값으로 설정
    String memberName = socialLogin.getNickname() != null && !socialLogin.getNickname().trim().isEmpty()
        ? socialLogin.getNickname()
        : "KakaoUser";
    member.setName(memberName);

    member.setEmail(socialLogin.getEmail());
    member.setRole("1");
    member.setPhone(""); // 기본값 설정
    member.setEmailNoti(false);
    member.setSmsNoti(false);

    memberMapper.insertMember(member);
    logger.info("신규 회원 생성 완료: {}", member);
    return member;
  }

  @Override
  public void insertOrUpdateSocialLoginInfo(Member member, SocialLogin socialLogin) {
    SocialLogin existingSocialLogin = socialLoginMapper.findSocialLoginByProviderId(socialLogin.getProviderId(),
        socialLogin.getProvider());
    if (existingSocialLogin != null) {
      socialLoginMapper.updateSocialLogin(socialLogin);
    } else {
      socialLoginMapper.insertSocialLogin(socialLogin);
    }
  }

  // Access Token 요청 후 사용자 정보를 가져오는 메서드
  @Override
  public String getAccessToken(String code) { // public 접근 제어자 추가
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

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return (String) response.getBody().get("access_token");
      }
    } catch (HttpClientErrorException e) {
      // Access Token 요청 중 오류 발생
      logger.error("Access Token 요청 중 오류 발생: {}", e.getResponseBodyAsString());
    }
    return null; // 실패 시 null 반환
  }

  // 카카오 사용자 정보를 가져오는 메서드
  @Override
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
        socialLogin.setNickname(String.valueOf(userInfo.get("nickname"))); // 사용자 이름
        socialLogin.setEmail(String.valueOf(userInfo.get("email"))); // 사용자 이메일

        return socialLogin; // 소셜 로그인 정보 반환
      }
    } catch (HttpClientErrorException e) {
      // 사용자 정보 요청 중 오류 발생
      logger.error("카카오 사용자 정보 요청 중 오류 발생: {}", e.getResponseBodyAsString());
    }
    return null; // 실패 시 null 반환
  }

  @Override
  @Transactional
  public Map<String, String> saveTokensForMember(Member member) {
    // 1. 엑세스 토큰 및 리프레시 토큰 생성
    String accessTokenValue = UUID.randomUUID().toString();
    String refreshTokenValue = UUID.randomUUID().toString();

    // 2. 만료 시간 설정
    LocalDateTime accessTokenExpiresAt = LocalDateTime.now().plusHours(1); // 엑세스 토큰 유효기간: 1시간
    LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(14); // 리프레시 토큰 유효기간: 14일

    // 3. 엑세스 토큰 객체 생성
    Token accessToken = new Token();
    accessToken.setToken(accessTokenValue);
    accessToken.setMemberId(member.getMemberId());
    accessToken.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    accessToken.setExpiresAt(accessTokenExpiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    accessToken.setTokenType("access");

    // 4. 리프레시 토큰 객체 생성
    Token refreshToken = new Token();
    refreshToken.setToken(refreshTokenValue);
    refreshToken.setMemberId(member.getMemberId());
    refreshToken.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    refreshToken.setExpiresAt(refreshTokenExpiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    refreshToken.setTokenType("refresh");

    // 5. DB에 각각 저장
    tokenMapper.insertToken(accessToken); // 엑세스 토큰 저장
    tokenMapper.insertToken(refreshToken); // 리프레시 토큰 저장

    // 6. 엑세스 토큰 및 리프레시 토큰 반환
    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessTokenValue);
    tokens.put("refreshToken", refreshTokenValue);

    return tokens;
  }
}
