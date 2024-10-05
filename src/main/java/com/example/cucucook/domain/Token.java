package com.example.cucucook.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens")
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "token_id")
  private Long tokenId;

  @Column(name = "member_id", nullable = false)
  private Integer memberId;

  @Column(name = "token", nullable = false)
  private String token;

  @Column(name = "token_type", nullable = false)
  private String tokenType;

  @Column(name = "created_at", nullable = false)
  private String createdAt;

  @Column(name = "expires_at", nullable = false)
  private String expiresAt;

  // Getter and Setter Methods
  public Long getTokenId() {
    return tokenId;
  }

  public void setTokenId(Long tokenId) {
    this.tokenId = tokenId;
  }

  public Integer getMemberId() {
    return memberId;
  }

  public void setMemberId(Integer memberId) {
    this.memberId = memberId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
  }

  // 토큰 만료 여부를 확인하는 메서드
  public boolean isExpired() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime expiresAtTime = LocalDateTime.parse(this.expiresAt, formatter);

    // 현재 시간과 expiresAtTime을 비교하여 만료 여부를 반환
    return LocalDateTime.now().isAfter(expiresAtTime);
  }
}
