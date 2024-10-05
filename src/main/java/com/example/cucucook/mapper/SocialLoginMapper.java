package com.example.cucucook.mapper;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.SocialLogin;

public interface SocialLoginMapper {
  void insertSocialLogin(SocialLogin socialLogin);

  // 카카오로그인시 회원정보 찾아오기
  Member findBySocialId(String socialId);
}