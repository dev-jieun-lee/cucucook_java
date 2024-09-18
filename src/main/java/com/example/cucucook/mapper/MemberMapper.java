package com.example.cucucook.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.VerificationCode;

@Mapper
public interface MemberMapper {

    // 사용자 아이디를 기준으로 사용자를 조회
    Member findByUserId(@Param("userId") String userId);

    // 사용자 핸드폰번호를 기준으로 사용자를 조회
    Member findByPhone(@Param("phone") String phone);

    // 이메일을 기준으로 사용자를 조회
    Member findByEmail(@Param("email") String email);

    // 회원 수 조회
    int getMemberCount(@Param("search") String search);

    // 회원 목록 조회
    List<Member> getMemberList(@Param("search") String search, @Param("start") int start,
            @Param("display") int display);

    // 회원 보기
    Member getMember(@Param("memberId") int memberId);

    // 회원 등록
    void insertMember(Member member);

    // 회원 수정
    void updateMember(Member member);

    // 회원 삭제
    void deleteMember(@Param("memberId") int memberId);

    // 비밀번호 변경
    void updateMemberPassword(@Param("memberId") int memberId, @Param("password") String password);

    // 비밀번호 변경 (기존)
    void updateMemberPassword(Member member);

    // 아이디 중복 체크
    boolean existsByUserId(@Param("userId") String userId);

    // 이메일 중복 체크
    boolean existsByEmail(@Param("email") String email);

    // 아이디 찾기
    Member findId(Member member);

    // 비밀번호 찾기
    Member findMemberByIdNameAndEmail(Member member);

    // 비밀번호 업데이트
    void updatePassword(Member member);

    // 이메일과 코드로 인증 코드 찾기
    VerificationCode findByEmailAndCode(@Param("email") String email, @Param("code") String code);

    // 이메일로 인증 코드 찾기
    VerificationCode findVerificationCodeByEmail(@Param("email") String email);

    // 기존 인증 코드 업데이트
    void updateVerificationCode(VerificationCode verificationCode);

    // 새로운 인증 코드 저장
    void saveVerificationCode(VerificationCode verificationCode);

    // 실패 횟수 초기화
    void resetFailedAttempts(@Param("userId") String userId);

    // 실패 횟수 업데이트 메서드 (새로운 실패 횟수를 파라미터로 받음)
    void updateFailedAttempts(@Param("userId") String userId, @Param("failedAttempts") int failedAttempts);

    // 계정 잠금
    void lockAccount(Map<String, Object> params);

    // 사용자 ID로 실패 횟수 조회
    int getFailedAttempts(@Param("userId") String userId);
}