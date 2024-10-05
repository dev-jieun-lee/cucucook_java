package com.example.cucucook.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import com.example.cucucook.domain.Token;
import com.example.cucucook.mapper.TokenMapper;
import com.example.cucucook.repository.TokenRepository;
import com.example.cucucook.service.TokenService;

@Service
public class TokenServiceImpl implements TokenService {

  @Autowired
  private TokenMapper tokenMapper;

  @Autowired
  private TokenRepository tokenRepository; // 올바른 패키지로 import 확인
  private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
  private static final int maxRetryAttempts = 3; // 재시도 최대 횟수

  @Override
  public void storeRefreshToken(String refreshToken, int memberId, String expiresAt) {
    logger.info("[storeRefreshToken] 시작 - refreshToken: {}, memberId: {}, expiresAt: {}", refreshToken, memberId,
        expiresAt);
    int retryCount = 0;
    boolean success = false;
    String formattedCreatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String formattedExpiresAt = expiresAt.formatted(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    logger.info("[storeRefreshToken] formattedCreatedAt: {}, formattedExpiresAt: {}", formattedCreatedAt,
        formattedExpiresAt);

    Token token = new Token();
    logger.info("[storeRefreshToken] 빈 Token 객체 생성 - {}", token);

    token.setToken(refreshToken);
    token.setMemberId(memberId);
    token.setCreatedAt(formattedCreatedAt);
    token.setExpiresAt(formattedExpiresAt);
    token.setTokenType("refresh");
    logger.info("[storeRefreshToken] Token 객체 최종 설정 완료 - {}", token);

    // 재시도 로직 구현
    while (!success && retryCount < maxRetryAttempts) {
      try {
        tokenMapper.insertToken(token);
        success = true; // 성공 시 루프 종료
        logger.info("[storeRefreshToken] 토큰 저장 성공 - memberId: {}", memberId);
      } catch (DataAccessResourceFailureException e) {
        retryCount++;
        logger.warn("[storeRefreshToken] 데이터베이스 연결 오류 발생 - 재시도 {}/{}회: {}", retryCount, maxRetryAttempts,
            e.getMessage());

        // 재시도 횟수를 초과하면 예외 발생
        if (retryCount >= maxRetryAttempts) {
          logger.error("[storeRefreshToken] 최대 재시도 횟수 초과 - memberId: {}. 에러: {}", memberId, e.getMessage());
          throw e;
        }

        // 짧은 지연시간 후 재시도 (100ms)
        try {
          Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
          Thread.currentThread().interrupt();
          logger.error("[storeRefreshToken] 재시도 중단됨: {}", interruptedException.getMessage());
        }
      } catch (Exception e) {
        logger.error("[storeRefreshToken] 토큰 저장 실패 - memberId: {}. 에러: {}", memberId,
            e.getMessage());
        throw e; // 다른 예외 발생 시 즉시 종료
      }
    }
  }

  @Override
  public boolean verifyToken(String token, String tokenType) {
    return tokenMapper.existsByTokenAndType(token, tokenType);
  }

  @Override
  public void deleteToken(String token) {
    tokenMapper.deleteTokenByToken(token);
  }

  @Override
  public void deleteTokensByMemberId(int memberId) {
    tokenMapper.deleteTokensByMemberId(memberId);
  }

}