package com.example.cucucook.domain;

public class SocialLogin {
  private int socialLoginId; // 고유 식별자
  private int memberId; // 기존 members 테이블의 회원 ID
  private String provider; // 소셜 로그인 제공자
  private String providerId; // 소셜 로그인 제공자가 제공하는 사용자 ID
  private String accessToken; // 소셜 로그인 시 받은 액세스 토큰
  private String refreshToken; // 소셜 로그인 시 받은 리프레시 토큰
  private String nickname; // 사용자 이름
  private String email; // 사용자 이메일
  private String createdAt; // 레코드 생성 시각
  private String updatedAt; // 마지막으로 업데이트된 시각

  // Getter와 Setter 추가
  public int getSocialLoginId() {
    return socialLoginId;
  }

  public void setSocialLoginId(int socialLoginId) {
    this.socialLoginId = socialLoginId;
  }

  public int getMemberId() {
    return memberId;
  }

  // memberId를 int로 받는 setter 메서드
  public void setMemberId(int memberId) {
    this.memberId = memberId;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
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

  @Override
  public String toString() {
    return "SocialLogin{" +
        "socialLoginId=" + socialLoginId +
        ", memberId=" + memberId +
        ", provider='" + provider + '\'' +
        ", providerId='" + providerId + '\'' +
        ", accessToken='" + accessToken + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }

}