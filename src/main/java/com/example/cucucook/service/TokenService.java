package com.example.cucucook.service;

public interface TokenService {

  boolean verifyToken(String token, String tokenType);

  void deleteToken(String token);

  void deleteTokensByMemberId(int memberId);

  void storeRefreshToken(String refreshToken, int memberId, String expiresAt);

  // void storeRefreshToken(String refreshToken, Member memberId, LocalDateTime
  // plusDays);

}
