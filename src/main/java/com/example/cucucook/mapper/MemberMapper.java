package com.example.cucucook.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.VerificationCode;

@Mapper
public interface MemberMapper {

    Member findByUserId(@Param("userId") String userId);

    Member findByPhone(@Param("phone") String phone);

    Member findByEmail(@Param("email") String email);

    int getMemberCount(@Param("search") String search);

    List<Member> getMemberList(@Param("search") String search, @Param("start") int start,
            @Param("display") int display);

    Member getMember(@Param("memberId") int memberId);

    void insertMember(Member member);

    void updateMember(Member member);

    void deleteMember(@Param("memberId") int memberId);

    void updateMemberPassword(@Param("memberId") int memberId, @Param("password") String password);

    boolean existsByUserId(@Param("userId") String userId);

    boolean existsByEmail(@Param("email") String email);

    void updateMemberPassword(Member member);

    // 아이디 찾기
    Member findId(Member member);

    // 비밀번호 찾기
    Member findMemberByIdNameAndEmail(Member member);

    // 임시비밀번호 발급 후 임시비밀번호로 멤버테이블 업데이트
    void updatePassword(Member member);

    // 이메일과 코드로 인증 코드 찾기
    VerificationCode findByEmailAndCode(@Param("email") String email, @Param("code") String code);

    // 기존 인증 코드를 이메일로 찾기
    VerificationCode findVerificationCodeByEmail(String email);

    // 기존 인증 코드를 업데이트
    void updateVerificationCode(VerificationCode verificationCode);

    // 새로운 인증 코드를 저장
    void saveVerificationCode(VerificationCode verificationCode);

    // 로그인 실패 시 처리 로직 추가
    void updateFailedAttempts(@Param("userId") String userId);

    void resetFailedAttempts(@Param("userId") String userId);

    void lockAccount(@Param("userId") String userId, @Param("lockoutTime") LocalDateTime lockoutTime);
}
