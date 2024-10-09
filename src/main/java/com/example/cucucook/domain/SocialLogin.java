package com.example.cucucook.domain;

import java.util.UUID;

public class SocialLogin {

  private String socialLoginId; // String 타입으로 변경
  private String providerId;
  private String provider;
  private String nickname;
  private String email;
  private int memberId;

  private boolean isSocialLogin;

  public boolean isSocialLogin() {
    return isSocialLogin;
  }

  public void setSocialLogin(boolean socialLogin) {
    isSocialLogin = socialLogin;
  }

  // 정적 팩토리 메서드
  public static SocialLogin createNewSocialLogin(String providerId, String provider, Integer memberId) {
    SocialLogin socialLogin = new SocialLogin();
    socialLogin.setSocialLoginId(UUID.randomUUID().toString());
    socialLogin.setProviderId(providerId);
    socialLogin.setProvider(provider);
    socialLogin.setMemberId(memberId);
    return socialLogin;
  }

  // Getter 및 Setter 메서드
  public String getSocialLoginId() {
    return socialLoginId;
  }

  public void setSocialLoginId(String socialLoginId) { // String 타입으로 변경
    this.socialLoginId = socialLoginId;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int getMemberId() {
    return memberId;
  }

  public void setMemberId(int memberId) {
    this.memberId = memberId;
  }
}
