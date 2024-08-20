package com.example.cucucook.service;

import com.example.cucucook.domain.Member;

import java.util.List;

public interface MemberService {

    //로그인
    Member login(String userId, String password);

    int getMemberCount(String search);
    List<Member> getMemberList(String search, int start, int display);
    Member getMember(Long memberId);

    //회원가입
    boolean checkPhoneExists(String phone);
    void registerMember(Member member);
    boolean checkUserIdExists(String userId);
    boolean checkEmailExists(String email);

    //회원정보 수정
    void updateMember(Member member);
    void deleteMember(Long memberId);
    void updateMemberPassword(Member member);




}
