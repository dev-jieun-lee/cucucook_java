package com.example.cucucook.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.cucucook.domain.Member;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.service.MemberService;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member login(String userId, String password) {
        return validateMember(userId, password); // 로그인은 validateMember 메서드를 사용
    }

    @Override
    public Member validateMember(String userId, String password) {
        Member member = memberMapper.findByUserId(userId);
        if (member != null && passwordEncoder.matches(password, member.getPassword())) {
            return member;
        }
        return null;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        return memberMapper.findByPhone(phone) != null;
    }

    @Override
    public void registerMember(Member member) {
        if (memberMapper.existsByUserId(member.getUserId())) {
            throw new IllegalArgumentException("User ID is already taken.");
        }
        if (memberMapper.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberMapper.insertMember(member);
    }

    @Override
    public boolean checkUserIdExists(String userId) {
        return memberMapper.existsByUserId(userId);
    }

    @Override
    public boolean checkEmailExists(String email) {
        return memberMapper.existsByEmail(email);
    }

    @Override
    public Member getMember(Long memberId) {
        return memberMapper.getMember(memberId.intValue());
    }

    @Override
    public void updateMember(Member member) {
        memberMapper.updateMember(member);
    }

    @Override
    public void deleteMember(Long memberId) {
        memberMapper.deleteMember(memberId.intValue());
    }

    @Override
    public void updateMemberPassword(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        //  memberMapper.updateMemberPassword(member);
    }

    @Override
    public int getMemberCount(String search) {
        return memberMapper.getMemberCount(search);
    }

    @Override
    public List<Member> getMemberList(String search, int start, int display) {
        return memberMapper.getMemberList(search, start, display);
    }

    //아이디찾기
    @Override
    public Member findId(Member member) {
        return memberMapper.findId(member);
    }
}
