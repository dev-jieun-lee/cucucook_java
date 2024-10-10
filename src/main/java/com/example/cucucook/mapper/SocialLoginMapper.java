package com.example.cucucook.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.SocialLogin;

@Mapper
public interface SocialLoginMapper {

  void insertSocialLogin(SocialLogin socialLogin);

  void updateSocialLogin(SocialLogin socialLogin);

  // 소셜 로그인 정보 조회
  SocialLogin findSocialLoginByProviderId(@Param("providerId") String providerId, @Param("provider") String provider);
}
