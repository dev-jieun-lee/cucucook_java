package com.example.cucucook.mapper;

import com.example.cucucook.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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

    //아이디 찾기
    Member findId(Member member);

    //비밀번호 찾기
    Member findMemberByIdNameAndPhone(Member member);

    //임시비밀번호 발급 후 임시비밀번호로 멤버테이블 업데이트
    void updatePassword(Member member);

    //본인인증
    void insertVerificationCode(@Param("phoneNumber") String phoneNumber, @Param("code") String code, @Param("expiresAt") LocalDateTime expiresAt);
    String selectVerificationCode(@Param("phoneNumber") String phoneNumber);
    void deleteExpiredVerificationCodes(); // 만료된 인증 코드 삭제
}
