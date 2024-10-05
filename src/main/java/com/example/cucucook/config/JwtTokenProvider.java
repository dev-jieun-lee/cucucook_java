package com.example.cucucook.config;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey; // JWT 비밀 키

    // 비밀 키를 Key 객체로 변환. 이 키는 토큰 서명에 사용됩니다.
    private Key getKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    // 액세스 토큰 생성 메서드: 사용자 ID와 역할을 기반으로 액세스 토큰 생성
    public String createToken(String userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 토큰 유효 시간: 1시간

        // JWT 생성: 사용자 ID와 역할을 페이로드에 포함
        JwtBuilder builder = Jwts.builder()
                .subject(userId) // 사용자 ID 설정
                .claim("roles", role) // 역할 설정
                .issuedAt(now) // 발급 시간 설정
                .expiration(validity) // 만료 시간 설정
                .signWith(this.getKey()); // 서명 키 설정

        return builder.compact(); // JWT 문자열 반환
    }

    // JWT 토큰 유효성 검사 메서드: 토큰의 만료 시간과 서명을 검증
    public boolean validateToken(String token) {
        try {
            JsonNode claims = parseClaims(token);
            Date expiration = new Date(claims.get("exp").asLong() * 1000);
            if (expiration.before(new Date())) {
                return false; // 토큰이 만료되었습니다.
            }
            return true; // 토큰이 유효합니다.
        } catch (Exception e) {
            return false; // 토큰 검증 실패
        }
    }

    // JWT 토큰에서 사용자 ID 추출: 토큰에서 사용자 ID를 안전하게 추출
    public String getUserId(String token) {
        JsonNode claims = parseClaims(token);
        return claims.get("sub").asText();
    }

    // JWT Claims 파싱: 토큰의 페이로드를 파싱하여 JSON 객체로 변환
    private JsonNode parseClaims(String token) {
        String[] parts = token.split("\\.");
        String payload = parts[1];
        String decodedPayload = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(decodedPayload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT payload", e);
        }
    }

    // 서명 계산: 토큰의 헤더와 페이로드로부터 서명을 계산
    private String computeSignature(String header, String payload) {
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
