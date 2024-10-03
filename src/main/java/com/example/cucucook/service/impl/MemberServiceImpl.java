package com.example.cucucook.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.domain.KakaoProperties;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.PasswordFindResponse;
import com.example.cucucook.domain.VerificationCode;
import com.example.cucucook.exception.AccountLockedException;
import com.example.cucucook.exception.InvalidPasswordException;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.service.EmailService;
import com.example.cucucook.service.MemberService;

@Service
public class MemberServiceImpl implements MemberService {

  private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

  private final MemberMapper memberMapper;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final KakaoProperties kakaoProperties;

  // 로그인 실패 기록 저장
  private final Map<String, Integer> failedAttemptsMap = new HashMap<>();

  public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder, EmailService emailService,
      KakaoProperties kakaoProperties) {
    this.memberMapper = memberMapper;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.kakaoProperties = kakaoProperties;
  }

  @Override
  public Member login(String userId, String password) {
    return validateMember(userId, password); // 로그인은 validateMember 메서드를 사용
  }

  @Transactional(rollbackFor = InvalidPasswordException.class)
  public Member validateMember(String userId, String password) {
    Member member = memberMapper.findByUserId(userId);
    if (member != null) {
      if (member.getLockoutTime() != null && member.getLockoutTime().isAfter(LocalDateTime.now())) {
        // AccountLockedException 예외 발생 시
        throw new AccountLockedException("계정이 잠금 상태입니다.", member.getFailedAttempts(),
            ChronoUnit.SECONDS.between(LocalDateTime.now(), member.getLockoutTime()));
      }

      // 비밀번호가 틀렸을 때 예외 발생
      if (!passwordEncoder.matches(password, member.getPassword())) {
        increaseFailedAttempts(userId);
        member = memberMapper.findByUserId(userId); // 업데이트된 데이터를 다시 가져옴

        // 남은 잠금 시간 계산
        long lockoutTimeRemaining = member.getLockoutTime() != null
            ? ChronoUnit.SECONDS.between(LocalDateTime.now(), member.getLockoutTime())
            : 0;

        logger.warn("InvalidPasswordException 발생: userId: {}, 실패 횟수: {}, 남은 잠금 시간: {}초",
            userId, member.getFailedAttempts(), lockoutTimeRemaining);

        throw new InvalidPasswordException(
            "비밀번호가 잘못되었습니다.",
            member.getFailedAttempts(),
            lockoutTimeRemaining);
      }

      resetFailedAttempts(userId);
      return member;
    }
    throw new RuntimeException("사용자를 찾을 수 없습니다.");
  }

  @Transactional
  public void increaseFailedAttempts(String userId) {
    logger.info("사용자 ID {}에 대한 로그인 실패 횟수를 업데이트합니다.", userId);

    // 실패 횟수 조회
    Member member = memberMapper.findByUserId(userId);
    if (member != null) {
      int currentAttempts = member.getFailedAttempts();
      logger.info("업데이트 전 실패 횟수: {}", currentAttempts);

      // 실패 횟수 업데이트

      memberMapper.updateFailedAttempts(userId);
      logger.info("실패 횟수 업데이트 쿼리 실행 완료: 사용자 ID = {}", userId);

      // 업데이트 후 실패 횟수 재조회
      Member updatedMember = memberMapper.findByUserId(userId);
      if (updatedMember != null) {
        logger.info("업데이트 후 실패 횟수: {}", updatedMember.getFailedAttempts());
      } else {
        logger.error("업데이트 후 사용자 정보를 다시 조회하는데 실패했습니다. 사용자 ID = {}", userId);
      }
    } else {
      logger.warn("해당 사용자 ID를 찾을 수 없습니다: {}", userId);
    }
  }

  @Transactional
  public void resetFailedAttempts(String userId) {
    memberMapper.resetFailedAttempts(userId);
  }

  @Transactional
  public void lockMemberAccount(String userId) {
    LocalDateTime lockoutTime = LocalDateTime.now().plusMinutes(10); // 10분 동안 계정 잠금
    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("lockoutTime", lockoutTime);
    memberMapper.lockAccount(params);
  }

  @Override
  public boolean checkPhoneExists(String phone) {
    return memberMapper.findByPhone(phone) != null;
  }

  @Override
  // 비밀번호를 암호화하여 저장하는 예시
  public void registerMember(Member member) {
    // 비밀번호를 BCrypt로 암호화
    String encodedPassword = passwordEncoder.encode(member.getPassword());
    member.setPassword(encodedPassword);

    // 회원 등록 로직
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
  public Member getMember(int memberId) {
    return memberMapper.getMember(memberId);
  }

  @Override
  public void updateMember(Member member) {
    memberMapper.updateMemberInfo(member);
  }

  @Override
  public void updateMemberPassword(Member member) {
    member.setPassword(passwordEncoder.encode(member.getPassword()));
    // memberMapper.updateMemberPassword(member);
  }

  @Override
  public int getMemberCount(String search) {
    return memberMapper.getMemberCount(search);
  }

  // 회원 목록 조회
  @Override
  public ApiResponse<List<Member>> getMemberList(String search, String searchType, int start, int display) {
    start = start > 0 ? start : 1;
    display = display > 0 ? display : 10;

    List<Member> memberList = memberMapper.getMemberList(search, searchType, start, display);
    String message = (memberList == null || memberList.isEmpty()) ? "회원 목록이 없습니다." : "회원 목록 조회 성공";
    boolean success = memberList != null && !memberList.isEmpty();
    return new ApiResponse<>(success, message, memberList, null);
  }

  // 아이디찾기
  @Override
  public Member findId(Member member) {
    return memberMapper.findId(member);
  }

  // 비밀번호 찾기
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

  // 이메일 인증코드 발송
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

  // 이메일코드 검증
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

  public Member validateMemberByUserId(String userId) {
    return memberMapper.findByUserId(userId);
  }

  // 회원탈퇴
  @Override
  public void deleteMember(int memberId) {
    memberMapper.deleteMember(memberId);
  }

  // 카카오로그인
  @Override
  public String kakaoLogin(String code) throws Exception {
    // REST API로 카카오 토큰 요청
    RestTemplate restTemplate = new RestTemplate();
    String tokenUrl = "https://kauth.kakao.com/oauth/token";

    // 토큰 요청 파라미터 설정
    String requestBody = String.format(
        "grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s&client_secret=%s",
        kakaoProperties.getClientId(), kakaoProperties.getRedirectUri(), code,
        kakaoProperties.getClientSecret());

    // 요청 및 응답 처리
    Map<String, Object> response = restTemplate.postForObject(tokenUrl, requestBody, Map.class);
    if (response == null || !response.containsKey("access_token")) {
      throw new Exception("카카오 토큰 요청 실패");
    }

    // access_token 반환
    return response.get("access_token").toString();
  }
}