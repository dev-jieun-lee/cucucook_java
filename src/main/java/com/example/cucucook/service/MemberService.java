package com.example.cucucook.service;

import com.example.cucucook.domain.Member;
import java.util.List;

public interface MemberService {
    Member validateMember(String userId, String password);
    Member login(String userId, String password);
    boolean checkPhoneExists(String phone);
    void registerMember(Member member);
    boolean checkUserIdExists(String userId);
    boolean checkEmailExists(String email);
    Member getMember(Long memberId);
    void updateMember(Member member);
    void deleteMember(Long memberId);
    void updateMemberPassword(Member member);
    int getMemberCount(String search);
    List<Member> getMemberList(String search, int start, int display);
}
