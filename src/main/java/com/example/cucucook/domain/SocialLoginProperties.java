package com.example.cucucook.domain;

import org.springframework.stereotype.Component;

@Component
// @ConfigurationProperties(prefix = "social")
public class SocialLoginProperties {
  private String clientId;
  private String clientSecret;
  private String redirectUri;

  // Getter & Setter
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  // Naver properties
  private String naverClientId;
  private String naverClientSecret;
  private String naverRedirectUri;

  public String getNaverClientId() {
    return naverClientId;
  }

  public void setNaverClientId(String naverClientId) {
    this.naverClientId = naverClientId;
  }

  public String getNaverClientSecret() {
    return naverClientSecret;
  }

  public void setNaverClientSecret(String naverClientSecret) {
    this.naverClientSecret = naverClientSecret;
  }

  public String getNaverRedirectUri() {
    return naverRedirectUri;
  }

  public void setNaverRedirectUri(String naverRedirectUri) {
    this.naverRedirectUri = naverRedirectUri;
  }
}
