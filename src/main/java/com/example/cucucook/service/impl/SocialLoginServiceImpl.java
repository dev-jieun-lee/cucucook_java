package com.example.cucucook.service.impl;

import java.sql.Timestamp;
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
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.cucucook.controller.KakaoAuthController;
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
  private static final Logger logger = LoggerFactory.getLogger(KakaoAuthController.class);

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

  public Member getOrCreateMember(SocialLogin socialLogin) {
    String socialId = socialLogin.getProviderId(); // SocialLogin 객체의 providerId를 socialId로 사용
    logger.info("소셜 ID와 이메일로 기존 멤버 검색 중: socialId={}, 이메일={}", socialId, socialLogin.getEmail());

    boolean exists = memberMapper.existsByEmail(socialLogin.getEmail());
    Member member = memberMapper.findByEmailAndSocialId(socialLogin.getEmail(), socialId);

    if (!exists || member == null) {
      logger.info("기존 멤버가 존재하지 않음. 새로운 멤버 생성 시작.");
      member = createMember(socialLogin, socialId);
    } else {
      logger.info("기존 멤버 발견: memberId={}", member.getMemberId());
      updateMemberInformation(member, socialLogin);
    }

    return member;
  }

  private Member createMember(SocialLogin socialLogin, String socialId) {
    Member member = new Member();
    member.setUserId(socialId); // 여기서 user_id를 socialId로 설정
    member.setEmail(socialLogin.getEmail());
    member.setName(socialLogin.getNickname());
    member.setSocialId(socialId);

    // 전화번호가 null이면 기본값으로 설정 (예: "없음" 또는 빈 문자열)
    if (member.getPhone() != null) {
      member.setPhone(member.getPhone());
    } else {
      member.setPhone(""); // 기본값으로 설정
    }

    memberMapper.insertMember(member);
    logger.info("새로운 멤버가 데이터베이스에 저장되었습니다: memberId={}", member.getMemberId());
    return member;
  }

  private void updateMemberInformation(Member member, SocialLogin socialLogin) {
    member.setEmail(socialLogin.getEmail());
    member.setName(socialLogin.getNickname());
    member.setSocialId(socialLogin.getProviderId());
    memberMapper.updateMemberInfo(member);
    logger.info("멤버 정보 업데이트: memberId={}", member.getMemberId());
  }

  private void insertSocialLoginInfo(Member member, SocialLogin socialLogin) {
    // 소셜 로그인 정보 저장
    socialLogin.setMemberId(member.getMemberId());
    socialLoginMapper.insertSocialLogin(socialLogin);
    logger.info("소셜 로그인 정보가 저장되었습니다: memberId={}, provider={}", member.getMemberId(), socialLogin.getProvider());

    // 토큰 저장 로직 (필요한 경우)
    Token token = new Token();
    token.setMemberId(member.getMemberId());
    token.setTokenType("access");
    token.setTokenType(socialLogin.getAccessToken());
    token.setExpiresAt(getExpirationTime());
    tokenMapper.insertToken(token);
    logger.info("액세스 토큰이 데이터베이스에 저장되었습니다: memberId={}, token={}", member.getMemberId(), token.getToken());
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
  public void insertSocialLogin(SocialLogin socialLogin) {
    socialLoginMapper.insertSocialLogin(socialLogin);
  }

  private String getExpirationTime() {
    // 현재 시간에 1시간(3600초)을 더한 값을 Timestamp로 생성한 후, 문자열로 변환하여 반환
    Timestamp timestamp = new Timestamp(System.currentTimeMillis() + (60 * 60 * 1000)); // 1시간 후의 Timestamp 객체
    return timestamp.toString(); // Timestamp를 String으로 변환하여 반환
  }

}
