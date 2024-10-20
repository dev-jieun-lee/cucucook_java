package com.example.cucucook.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.PasswordFindResponse;

public interface MemberService {

    Member validateMember(String userId, String password);

    Map<String, String> login(String userId, String password);

    boolean checkPhoneExists(String phone);

    void registerMember(Member member);

    boolean checkUserIdExists(String userId);

    boolean checkEmailExists(String email);

    Member getMember(int memberId);

    void updateMember(Member member);

    void deleteMember(int memberId);

    void updateMemberPassword(Member member);

    int getMemberCount(String search);

    // 회원 리스트 조회
    public ApiResponse<List<Member>> getMemberList(String search, String searchType, int start, int display);

    // 아이디 찾기
    Member findId(Member member);

    // 비밀번호 찾기
    PasswordFindResponse findPassword(Member member) throws Exception;

    // 이메일 인증코드 발송
    void sendVerificationCode(String email, boolean skipEmailCheck);

    boolean verifyEmailCode(String email, String code);

    // 로그인 실패 시 처리 로직 추가
    // void increaseFailedAttempts(String userId);

    // void resetFailedAttempts(String userId);

    LocalDateTime lockMemberAccount(String userId);

    Member validateMemberByUserId(String userId);

    int getRemainingLockoutTime(String userId);

}