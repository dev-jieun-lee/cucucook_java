package com.example.cucucook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.cucucook.domain.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

  Optional<Token> findTokenByMemberId(int memberId);
  // 추가적으로 커스텀 메서드를 정의할 수 있습니다.
}
