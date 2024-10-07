package com.example.cucucook.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Token;

@Mapper
public interface TokenMapper {
  void insertToken(Token token);

  boolean existsByTokenAndType(@Param("token") String token, @Param("tokenType") String tokenType);

  void deleteTokenByToken(String token);

  void deleteTokensByMemberId(int memberId);
}
