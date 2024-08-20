package com.example.cucucook.service.impl;

import com.example.cucucook.domain.Board;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.RecipeComment;
import com.example.cucucook.domain.RecipeLike;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    //로그인
    @Override
    public Member login(String userId, String password) {
        Member member = memberMapper.findByUserId(userId);
        if (member != null && passwordEncoder.matches(password, member.getPassword())) {
            return member; // 로그인 성공 시 Member 객체 반환
        }
        return null; // 로그인 실패 시 null 반환
    }

    //회원가입
    @Override
    public boolean checkPhoneExists(String phone) {
        Member member = memberMapper.findByPhone(phone);
        return member != null;
    }

    @Override
    public void registerMember(Member member) {
        // 아이디 중복 확인
        if (memberMapper.existsByUserId(member.getUserId())) {
            throw new IllegalArgumentException("User ID is already taken.");
        }

        // 이메일 중복 확인
        if (memberMapper.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        // 비밀번호 암호화
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // 회원 데이터 저장
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
        memberMapper.updateMemberPassword(member);
    }





    @Override
    public int getMemberCount(String search) {
        return memberMapper.getMemberCount(search);
    }

    @Override
    public List<Member> getMemberList(String search, int start, int display) {
        return memberMapper.getMemberList(search, start, display);
    }
}
