package com.example.cucucook.service;

import java.util.Map;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.SocialLogin;

public interface SocialLoginService {
  Member getOrCreateMember(SocialLogin socialLogin);

  // 소셜 로그인 정보 등록 또는 업데이트
  void insertOrUpdateSocialLoginInfo(Member member, SocialLogin socialLogin);

  // 카카오로부터 사용자 정보를 가져옴
  SocialLogin getKakaoUserInfo(String accessToken);

  // 액세스 토큰 요청
  String getAccessToken(String code);

  Map<String, String> saveTokensForMember(Member member);
}
