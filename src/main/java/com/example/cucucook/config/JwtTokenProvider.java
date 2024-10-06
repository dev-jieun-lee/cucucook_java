package com.example.cucucook.config;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.cucucook.mapper.MemberMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey; // JWT 비밀 키
  @Value("${token.expired}")
  private int tokenExpired;
  @Autowired
  private MemberMapper memberMapper;

  // 비밀 키를 Key 객체로 변환.
  private Key getKey() {
    return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)); // Base64로 인코딩된 문자열을 byte 배열로 변환
  }

  // JWT 토큰 생성 메서드
  public String createToken(String userId, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + 3600000); // 토큰 유효 시간: 1시간

    // JwtBuilder를 사용하여 JWT 생성
    JwtBuilder builder = Jwts.builder()
        .subject(userId) // 사용자 ID 설정
        .claim("roles", role) // 역할 설정
        .issuedAt(now) // 발급 시간 설정
        .expiration(validity) // 만료 시간 설정
        .signWith(this.getKey()); // Key 객체와 알고리즘을 사용하여 서명

    return builder.compact(); // JWT 문자열 반환
  }

  // JWT 토큰 유효성 검사 메서드
  public boolean validateToken(String token) {
    try {
      // JWT를 파싱하여 Claims를 얻.
      JsonNode claims = parseClaims(token);

      // 만료 시간을 검사.
      Date expiration = new Date(claims.get("exp").asLong() * 1000);
      if (expiration.before(new Date())) {
        return false;
      }

      // 서명 검증
      String header = token.split("\\.")[0];
      String payload = token.split("\\.")[1];
      String signature = token.split("\\.")[2];

      String computedSignature = computeSignature(header, payload);

      return signature.equals(computedSignature);

    } catch (Exception e) {
      return false; // 유효하지 않은 토큰
    }
  }

  // JWT 토큰에서 사용자 ID 추출: 토큰에서 사용자 ID를 안전하게 추출
  public String getUserId(String token) {
    JsonNode claims = parseClaims(token);
    return claims.get("sub").asText();
  }

  // JWT Claims 파싱 메서드
  private JsonNode parseClaims(String token) {
    // 토큰을 '.'로 분리하여 페이로드를 Base64로 디코딩.
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("JWT does not have 3 parts");
    }

    String payload = parts[1];
    String decodedPayload = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);

    // JSON 객체로 변환
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode;
    try {
      jsonNode = objectMapper.readTree(decodedPayload);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse JWT payload", e);
    }

    return jsonNode;
  }

  // 서명 계산 메서드
  private String computeSignature(String header, String payload) {
    // HMAC SHA-256 서명을 수동으로 계산.
    try {
      byte[] key = Base64.getDecoder().decode(secretKey);
      javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
      javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key, "HmacSHA256");
      mac.init(secretKeySpec);

      byte[] signatureBytes = mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

    } catch (Exception e) {
      throw new RuntimeException("Failed to compute JWT signature", e);
    }
  }

  // TODO: 추가 구현 필요한 메서드
  public UserDetails loadUserByUserId(String userId) {
    throw new UnsupportedOperationException("Unimplemented method 'loadUserByUserId'");
  }

  // 리프레시 토큰 생성 메서드: 아직 구현되지 않음
  public String createRefreshToken(String userId) {
    long validityInMilliseconds = 604800000; // 7 days, 리프레시 토큰의 유효 기간 설정
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
        .subject(userId) // 사용자 ID 설정
        .issuedAt(now) // 발급 시간 설정
        .expiration(validity) // 만료 시간 설정
        .signWith(this.getKey())
        .compact();
  }

}
