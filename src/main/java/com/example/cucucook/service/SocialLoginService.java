package com.example.cucucook.service;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.SocialLogin;

public interface SocialLoginService {
  void insertSocialLogin(SocialLogin socialLogin);

  Member getOrCreateMember(SocialLogin socialId);

  String getAccessToken(String code);

  SocialLogin getKakaoUserInfo(String accessToken);
}
