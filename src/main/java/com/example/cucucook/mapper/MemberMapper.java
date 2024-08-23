package com.example.cucucook.mapper;

import com.example.cucucook.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MemberMapper {

    Member findByUserId(@Param("userId") String userId);
    Member findByPhone(@Param("phone") String phone);
    Member findByEmail(@Param("email") String email);
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
    //아이디찾기
    Member findId(Member member);
}
