package com.example.cucucook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.cucucook.domain.Member;
import com.example.cucucook.mapper.MemberMapper;

@Service
public class LoginAttemptService {
  private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

  private final MemberMapper memberMapper;

  public LoginAttemptService(MemberMapper memberMapper) {
    this.memberMapper = memberMapper;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int increaseFailedAttempts(String userId) {
    logger.info("사용자 ID {}에 대한 로그인 실패 횟수를 업데이트합니다. 현재 트랜잭션 활성 여부: {}",
        userId, TransactionSynchronizationManager.isActualTransactionActive());

    int updatedAttempts = 0;
    try {
      // 실패 횟수 조회 및 업데이트
      Member member = memberMapper.findByUserId(userId);
      if (member != null) {
        memberMapper.updateFailedAttempts(userId);
        Member updatedMember = memberMapper.findByUserId(userId);
        updatedAttempts = updatedMember.getFailedAttempts();
        logger.info("업데이트 후 실패 횟수: {} (사용자 ID: {})", updatedAttempts, userId);
      }
    } catch (Exception e) {
      logger.error("로그인 실패 횟수 증가 중 예외가 발생했습니다. 사용자 ID = {}, 예외 메시지: {}", userId, e.getMessage());
      throw e;
    }
    return updatedAttempts; // 최신 실패 횟수 반환
  }

  public void resetFailedAttempts(String userId) {
    memberMapper.resetFailedAttempts(userId);
  }

}
