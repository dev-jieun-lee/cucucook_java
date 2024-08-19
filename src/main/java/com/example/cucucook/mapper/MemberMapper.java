package com.example.cucucook.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Member;

@Mapper
public interface MemberMapper {

    Member findByUserId(@Param("userId") String userId);

    int getMemberCount(@Param("search") String search);

    List<Member> getMemberList(@Param("search") String search, @Param("start") int start, @Param("display") int display);

    Member getMember(@Param("memberId") int memberId);

    void insertMember(Member member);

    void updateMember(Member member);

    void deleteMember(@Param("memberId") int memberId);

    void updateMemberPassword(@Param("memberId") int memberId, @Param("password") String password);

    boolean existsByUserId(@Param("userId") String userId);

    boolean existsByEmail(@Param("email") String email);

    void updateMemberPassword(Member member);

}
