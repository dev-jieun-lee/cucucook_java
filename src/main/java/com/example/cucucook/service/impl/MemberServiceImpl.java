package com.example.cucucook.service.impl;

import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.PasswordFindResponse;
import com.example.cucucook.domain.VerificationCode;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.cucucook.service.EmailService;
import com.example.cucucook.domain.VerificationCode;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import org.mindrot.jbcrypt.BCrypt;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private VerificationCode VerificationCode;

    @Autowired
    public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

    //비밀번호 찾기
    @Override
    public PasswordFindResponse findPassword(Member member) throws Exception {
        Member existingMember = memberMapper.findMemberByIdNameAndEmail(member);
        if (existingMember != null) {
            // 임시 비밀번호 발급
            String tempPassword = generateTempPassword();
            String hashedPassword = passwordEncoder.encode(tempPassword); // bcrypt로 암호화

            existingMember.setPassword(hashedPassword);
            memberMapper.updatePassword(existingMember);

            // 비밀번호 전송 로직 구현
            sendTempPassword(existingMember, tempPassword); // 원본 비밀번호를 이메일로 전송

            // 응답 객체 생성
            return new PasswordFindResponse(true, "Temporary password has been sent to the email.",
                    existingMember.getUserId(), tempPassword);
        } else {
            return new PasswordFindResponse(false, "No member found with the provided details.", null, null);
        }
    }

    private String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void sendTempPassword(Member member, String tempPassword) {
        String subject = "임시 비밀번호 발급 안내";
        String body = "안녕하세요,\n\n임시 비밀번호가 발급되었습니다. 로그인 후 반드시 비밀번호를 변경해주세요.\n\n임시 비밀번호: " + tempPassword;

        // 이메일 발송
        emailService.send(member.getEmail(), subject, body);
    }

    //이메일 인증코드 발송
    @Override
    public void sendVerificationCode(String email) {
        String code = generateVerificationCode();
        VerificationCode existingCode = memberMapper.findVerificationCodeByEmail(email);

        if (existingCode != null) {
            // 이미 존재하는 이메일이 있다면 인증 코드를 업데이트합니다.
            existingCode.setCode(code);
            existingCode.setCreatedAt(LocalDateTime.now());
            existingCode.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            memberMapper.updateVerificationCode(existingCode);
        } else {
            // 이메일이 없으면 새로운 인증 코드를 생성합니다.
            VerificationCode newCode = new VerificationCode(email, code, LocalDateTime.now().plusMinutes(15));
            memberMapper.saveVerificationCode(newCode);
        }

        // 이메일 발송
        emailService.send(email, "Your verification code", "Your code: " + code);
    }

    //이메일코드 검증
    @Override
    public boolean verifyEmailCode(String email, String code) {
        // 이메일과 코드로 데이터베이스에서 인증 코드 조회
        VerificationCode verificationCode = memberMapper.findByEmailAndCode(email, code);

        if (verificationCode == null) {
            // 인증 코드가 존재하지 않음
            System.out.println("인증 코드 검증 실패: 인증 코드가 존재하지 않습니다.");
            return false;
        }

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 인증 코드가 만료됨
            System.out.println("인증 코드 검증 실패: 인증 코드가 만료되었습니다.");
            return false;
        }

        // 인증 성공
        System.out.println("인증 코드 검증 성공: 인증 코드가 유효합니다.");
        return true;
    }


    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }


}
