package com.example.cucucook.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.config.JwtTokenProvider;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.PasswordFindResponse;
import com.example.cucucook.domain.SocialLoginProperties;
import com.example.cucucook.domain.Token;
import com.example.cucucook.domain.VerificationCode;
import com.example.cucucook.exception.AccountLockedException;
import com.example.cucucook.exception.InvalidPasswordException;
import com.example.cucucook.mapper.MemberMapper;
import com.example.cucucook.mapper.SocialLoginMapper;
import com.example.cucucook.repository.TokenRepository;
import com.example.cucucook.service.EmailService;
import com.example.cucucook.service.LoginAttemptService;
import com.example.cucucook.service.MemberService;
import com.example.cucucook.service.TokenService;

@Service
public class MemberServiceImpl implements MemberService {

  private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

  private final MemberMapper memberMapper;
  private final SocialLoginMapper socialLoginMapper;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final SocialLoginProperties kakaoProperties;
  private final TokenRepository tokenRepository;
  @Autowired
  private JwtTokenProvider tokenProvider;
  @Autowired
  private TokenService tokenService;

  // 로그인 실패 기록 저장
  private final Map<String, Integer> failedAttemptsMap = new HashMap<>();

  private LoginAttemptService loginAttemptService;

  public MemberServiceImpl(MemberMapper memberMapper, PasswordEncoder passwordEncoder, EmailService emailService,
      SocialLoginProperties kakaoProperties, LoginAttemptService loginAttemptService,
      SocialLoginMapper socialLoginMapper, TokenRepository tokenRepository, JwtTokenProvider tokenProvider) {
    this.memberMapper = memberMapper;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
    this.kakaoProperties = kakaoProperties;
    this.loginAttemptService = loginAttemptService;
    this.socialLoginMapper = socialLoginMapper;
    this.tokenRepository = tokenRepository;
    this.tokenProvider = tokenProvider;
  }

  @Override
  public Map<String, String> login(String userId, String password) {
    // 회원 검증 로직
    Member member = validateMember(userId, password);

    // 기존 리프레시 토큰 확인
    Optional<Token> existingTokenOpt = tokenRepository.findTokenByMemberId(member.getMemberId());
    String refreshToken;
    String expiresAt; // String 타입으로 만료 시간 선언

    // 현재 시간을 포맷팅하여 String 값으로 변환
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    expiresAt = LocalDateTime.now().plusDays(7).format(formatter); // 7일 뒤 만료 시간 String으로 설정

    // 기존 리프레시 토큰 확인 및 갱신 로직
    if (existingTokenOpt.isPresent()) {
      Token existingToken = existingTokenOpt.get();
      if (!existingToken.isExpired()) {
        // 만료되지 않은 경우 기존 토큰 사용
        refreshToken = existingToken.getToken();
      } else {
        // 만료된 경우 새로운 리프레시 토큰 발급
        refreshToken = tokenProvider.createRefreshToken(userId);
        existingToken.setToken(refreshToken);
        existingToken.setExpiresAt(expiresAt); // String 값으로 만료 시간 설정
        tokenRepository.save(existingToken);
      }
    } else {
      // 리프레시 토큰이 존재하지 않으면 새로 발급
      refreshToken = tokenProvider.createRefreshToken(userId);
      tokenService.storeRefreshToken(refreshToken, member.getMemberId(), expiresAt); // String 타입의 만료 시간 전달
    }

    // Access Token 발급
    String accessToken = tokenProvider.createToken(userId, member.getRole());

    // 토큰 정보 반환
    Map<String, String> tokenData = new HashMap<>();
    tokenData.put("accessToken", accessToken);
    tokenData.put("refreshToken", refreshToken);
    tokenData.put("userId", userId);
    tokenData.put("expiresAt", expiresAt); // 만료 시간을 응답에 포함
    tokenData.put("name", member.getName()); // 이름 추가
    tokenData.put("role", member.getRole()); // 역할 추가
    tokenData.put("memberId", String.valueOf(member.getMemberId())); // memberId 값을 Map에 넣음
    tokenData.put("failedAttempts", String.valueOf(member.getFailedAttempts())); // 실패 횟수 값 추가

    return tokenData;
  }

  // String으로 저장된 만료 시간(`expiresAt`)을 확인하는 메서드
  private boolean isTokenExpired(String expiresAt, DateTimeFormatter formatter) {
    LocalDateTime expiryDateTime = LocalDateTime.parse(expiresAt, formatter);
    return expiryDateTime.isBefore(LocalDateTime.now());
  }

  @Transactional(rollbackFor = InvalidPasswordException.class)
  public Member validateMember(String userId, String password) {
    Member member = memberMapper.findByUserId(userId);
    if (member != null) {
      logger.info("사용자 정보 확인: userId: {}, isSocialLogin: {}", userId, member.isSocialLogin()); // 추가된 로그
      if (member.isSocialLogin()) {
        logger.info("소셜 로그인으로 인증된 사용자입니다. 비밀번호 확인을 건너뜁니다. userId: {}", userId);
        return member; // 비밀번호 검증을 건너뛰고 회원 정보를 반환
      }

      // 나머지 기존 로직
      if (member.getLockoutTime() != null && member.getLockoutTime().isAfter(LocalDateTime.now())) {
        throw new AccountLockedException("계정이 잠금 상태입니다.", member.getFailedAttempts(),
            ChronoUnit.SECONDS.between(LocalDateTime.now(), member.getLockoutTime()));
      }

      if (!passwordEncoder.matches(password, member.getPassword())) {
        int updatedAttempts = loginAttemptService.increaseFailedAttempts(userId);
        if (updatedAttempts >= 5) {
          LocalDateTime lockoutTime = lockMemberAccount(userId);
          logger.warn("계정 잠금: userId: {}, 잠금 시간: {}", userId, lockoutTime);

          throw new AccountLockedException("계정이 잠금 처리되었습니다.", updatedAttempts,
              ChronoUnit.SECONDS.between(LocalDateTime.now(), lockoutTime));
        }

        long lockoutTimeRemaining = member.getLockoutTime() != null
            ? ChronoUnit.SECONDS.between(LocalDateTime.now(), member.getLockoutTime())
            : 0;

        logger.warn("InvalidPasswordException 발생: userId: {}, 실패 횟수: {}, 남은 잠금 시간: {}초",
            userId, updatedAttempts, lockoutTimeRemaining);

        throw new InvalidPasswordException(
            "비밀번호가 잘못되었습니다.",
            updatedAttempts,
            lockoutTimeRemaining);
      }

      loginAttemptService.resetFailedAttempts(userId);
      return member;
    }
    throw new RuntimeException("사용자를 찾을 수 없습니다.");
  }

  public LocalDateTime lockMemberAccount(String userId) {
    LocalDateTime lockoutTime = LocalDateTime.now().plusMinutes(10); // 10분 동안 계정 잠금
    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("lockoutTime", lockoutTime);
    memberMapper.lockAccount(params);
    return lockoutTime; // 잠금 시간 반환
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
      return false;
    }

    if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
      // 인증 코드가 만료됨
      return false;
    }

    // 인증 성공
    return true;
  }

  private String generateVerificationCode() {
    return String.valueOf(new Random().nextInt(900000) + 100000);
  }

  // userId값으로 회원정보 가져오기
  public Member validateMemberByUserId(String userId) {
    return memberMapper.findByUserId(userId);
  }

  // 회원탈퇴
  @Override
  public void deleteMember(int memberId) {
    memberMapper.deleteMember(memberId);
  }

  // 잠금상태일 경우 로그인 시도
  @Override
  public int getRemainingLockoutTime(String userId) {
    Member member = memberMapper.findByUserId(userId);

    // member가 존재하고, 잠금 시간이 설정되어 있을 경우 남은 시간을 계산하여 반환
    if (member != null && member.getLockoutTime() != null) {
      // 현재 시간과 잠금 시간의 차이를 계산하여 초 단위로 변환
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime lockoutTime = member.getLockoutTime();

      // 남은 잠금 시간을 초 단위로 반환
      return (int) Duration.between(now, lockoutTime).getSeconds();
    }

    // 잠금 시간이 없으면 0을 반환
    return 0;
  }

}