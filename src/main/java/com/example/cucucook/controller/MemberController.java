package com.example.cucucook.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.common.ApiResponse;
import com.example.cucucook.config.JwtTokenProvider;
import com.example.cucucook.domain.Member;
import com.example.cucucook.domain.MemberResponse;
import com.example.cucucook.domain.PasswordFindResponse;
import com.example.cucucook.exception.AccountLockedException;
import com.example.cucucook.exception.InvalidPasswordException;
import com.example.cucucook.service.MemberService;
import com.example.cucucook.service.TokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/members")
public class MemberController {

  private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

  @Autowired
  private MemberService memberService;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private TokenService tokenService;

  private String key;

  @Value("${acessCookie.expired}")
  private int acessCookieExpired;
  @Value("${refreshCookie.expired}")
  private int refreshCookieExpired;
  @Value("${autoLoginCookie.expired}")
  private int autoLoginCookieExpired;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Member loginRequest, HttpServletResponse response,
      HttpServletRequest request) {
    String userId = loginRequest.getUserId();

    // 잠금상태일 경우 로그인 시도
    int remainingLockoutTime = memberService.getRemainingLockoutTime(loginRequest.getUserId());
    if (remainingLockoutTime > 0) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of(
              "message", "계정이 잠겨 있습니다. 잠금이 해제될 때까지 기다려 주세요.",
              "lockoutTime", remainingLockoutTime));

    }

    try {
      // 서비스에서 로그인 처리 후 토큰 데이터를 반환받음
      Map<String, String> tokenData = memberService.login(loginRequest.getUserId(), loginRequest.getPassword());

      // 사용자가 자동 로그인을 체크했는지 확인
      boolean rememberLogin = loginRequest.isRememberLogin();
      // 자동 로그인 체크 여부에 따라 쿠키 만료 시간을 다르게 설정
      Integer accessTokenMaxAge = rememberLogin ? acessCookieExpired : null; // 자동 로그인 시 1일 유지, 아니면 세션 쿠키
      Integer refreshTokenMaxAge = rememberLogin ? refreshCookieExpired : null; // 자동 로그인 시 7일 유지, 아니면 세션 쿠키

      // 액세스 토큰과 리프레시 토큰을 쿠키에 설정
      setTokenInCookie(response, tokenData.get("accessToken"), "access_token", true, request.isSecure(), "/",
          accessTokenMaxAge);
      setTokenInCookie(response, tokenData.get("refreshToken"), "refresh_token", true, request.isSecure(), "/",
          refreshTokenMaxAge);
      // 자동 로그인 체크 여부쿠키에 저장
      if (rememberLogin) {
        setTokenInCookie(response, "Y", "remember_login", false, request.isSecure(), "/",
            autoLoginCookieExpired);
      }

      // 응답 데이터 구성
      Map<String, String> responseBody = new HashMap<>();
      responseBody.put("message", "로그인 성공");
      responseBody.put("accessToken", tokenData.get("accessToken"));
      responseBody.put("refreshToken", tokenData.get("refreshToken"));
      responseBody.put("userId", tokenData.get("userId"));
      responseBody.put("memberId", tokenData.get("memberId")); // 이미 String 타입이므로 변환 불필요
      responseBody.put("name", tokenData.get("name")); // 이름 필드 추가
      responseBody.put("role", tokenData.get("role")); // 역할 필드 추가
      responseBody.put("failedAttempts", tokenData.get("failedAttempts")); // 실패 횟수 추가
      responseBody.put("lockoutTime", tokenData.get("lockoutTime")); // 잠금시간 추가

      return ResponseEntity.ok().body(responseBody);

    } catch (InvalidPasswordException e) {
      String message = getMessageFromKey(getMessageKey(e.getFailedAttempts()));
      logger.warn("사용자 '{}' 비밀번호 오류: {}", userId, message);
      return buildErrorResponse(message, "InvalidPassword", HttpStatus.UNAUTHORIZED, e.getFailedAttempts(), null);

    } catch (AccountLockedException e) {
      logger.warn("사용자 '{}' 계정 잠김. 남은 잠금 시간: {}초", userId, e.getRemainingTime());
      return buildErrorResponse(getMessageFromKey("locked_time"), "AccountLocked", HttpStatus.FORBIDDEN, null,
          e.getRemainingTime());

    } catch (Exception e) {
      logger.error("로그인 처리 중 서버 오류 발생", e);
      return buildErrorResponse("서버 오류가 발생했습니다. 관리자에게 문의하세요.", "ServerError", HttpStatus.INTERNAL_SERVER_ERROR,
          null, null);
    }
  }

  // 토큰 쿠키 설정 메서드
  private void setTokenInCookie(HttpServletResponse response, String token,
      String name, boolean isHttpOnly, boolean secure, String path, Integer maxAge) {
    Cookie cookie = new Cookie(name, token);
    cookie.setHttpOnly(isHttpOnly);
    cookie.setSecure(secure);
    cookie.setPath(path);
    if (maxAge != null)
      cookie.setMaxAge(maxAge);
    response.addCookie(cookie);
  }

  // 실패 횟수에 따른 메시지 키 반환 메서드
  private String getMessageKey(int failedAttempts) {
    switch (failedAttempts) {
      case 3:
        return "attempt_3";
      case 4:
        return "attempt_4";
      default:
        return "invalid_password";
    }
  }

  // 메시지 키에 따라 사용자 메시지 반환
  private String getMessageFromKey(String messageKey) {
    switch (messageKey) {
      case "attempt_3":
        return "경고: 세 번 연속 로그인에 실패했습니다. 계속 실패할 경우 계정이 잠길 수 있습니다.";
      case "attempt_4":
        return "경고: 네 번 연속 로그인에 실패했습니다. 한 번 더 실패 시 계정이 잠깁니다.";
      case "locked_time":
        return "로그인 시도 횟수 초과로 인해 계정이 잠겼습니다. 관리자에게 문의하세요.";
      case "invalid_password":
      default:
        return "비밀번호가 일치하지 않습니다. 다시 시도해 주세요.";
    }
  }

  // 오류 응답 생성 유틸리티 메서드
  private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, String errorType, HttpStatus status,
      Integer failedAttempts, Long lockoutTime) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", message);
    body.put("errorType", errorType);
    body.put("timestamp", System.currentTimeMillis());

    if (failedAttempts != null) {
      body.put("failedAttempts", failedAttempts); // 실패 횟수 추가
    }
    if (lockoutTime != null && lockoutTime > 0) {
      body.put("lockoutTime", lockoutTime); // 남은 잠금 시간 추가
    }

    return new ResponseEntity<>(body, status);
  }

  // 로그아웃 API
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    // JWT 토큰 쿠키 삭제
    Cookie accessToken = new Cookie("access_token", null);
    Cookie refreshToken = new Cookie("refresh_token", null);
    Cookie isRemember = new Cookie("remember_login", null);
    accessToken.setHttpOnly(true);
    accessToken.setSecure(request.isSecure()); // HTTPS에서만 사용
    accessToken.setPath("/"); // 전체 경로에 대해 유효
    accessToken.setMaxAge(0); // 쿠키 삭제
    response.addCookie(accessToken);
    refreshToken.setHttpOnly(true);
    refreshToken.setSecure(request.isSecure()); // HTTPS에서만 사용
    refreshToken.setPath("/"); // 전체 경로에 대해 유효
    refreshToken.setMaxAge(0); // 쿠키 삭제
    response.addCookie(refreshToken);
    isRemember.setHttpOnly(false);
    isRemember.setSecure(request.isSecure()); // HTTPS에서만 사용
    isRemember.setPath("/"); // 전체 경로에 대해 유효
    isRemember.setMaxAge(0); // 쿠키 삭제
    response.addCookie(isRemember);

    // 인증 정보 로그아웃 처리
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    // 로그아웃 성공 메시지
    return ResponseEntity.ok().body("Logged out successfully");
  }

  // 회원 등록
  @PostMapping("/register")
  public ResponseEntity<String> registerMember(@RequestBody Member member) {
    try {
      memberService.registerMember(member);
      return ResponseEntity.ok("User registered successfully");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // 핸드폰번호 중복 확인
  @PostMapping("/check-phone")
  public ResponseEntity<Boolean> checkPhoneNumber(@RequestBody PhoneRequest request) {
    boolean exists = memberService.checkPhoneExists(request.getPhone());
    return ResponseEntity.ok(exists);
  }

  // 아이디 중복 체크
  @GetMapping("/check-id/{userId}")
  public ResponseEntity<Boolean> checkUserId(@PathVariable String userId) {
    logger.info("아이디 '{}' 중복 여부를 확인하는 요청을 받음.", userId);

    logger.debug("memberService.checkUserIdExists 메서드를 호출. userId: {}", userId);
    boolean userIdExists = memberService.checkUserIdExists(userId);

    logger.debug("memberService.checkUserIdExists 호출 결과 - userId '{}': {}", userId, userIdExists);

    boolean isAvailable = !userIdExists;

    if (isAvailable) {
      logger.info("아이디 '{}'는 사용 가능.", userId);
    } else {
      logger.warn("아이디 '{}'는 이미 사용 중.", userId);
    }

    logger.debug("userId '{}'에 대한 응답 값: {}", userId, isAvailable);

    return ResponseEntity.ok(isAvailable);
  }

  // 이메일 중복 체크
  @GetMapping("/check-email/{email}")
  public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
    return ResponseEntity.ok(memberService.checkEmailExists(email));
  }

  // 아이디 찾기
  @PostMapping("/find-id")
  public ResponseEntity<?> findId(@RequestBody Member member) {
    logger.info("Received Member(요청데이터) : ", member); // 로그에 Member 객체 출력
    try {
      Member foundMember = memberService.findId(member);
      if (foundMember != null && foundMember.getUserId() != null) {
        return ResponseEntity.ok().body(new FindIdResponse(foundMember.getUserId()));
      } else {
        return ResponseEntity.ok().body(new FindIdResponse(null));
      }
    } catch (Exception e) {
      return ResponseEntity.status(500).body("아이디 찾기 오류");
    }
  }

  // 아이다찾기 응답부분
  public static class FindIdResponse {

    private String foundId;

    public FindIdResponse(String foundId) {
      this.foundId = foundId;
    }

    public String getFoundId() {
      return foundId;
    }

    public void setFoundId(String foundId) {
      this.foundId = foundId;
    }
  }

  // 비밀번호찾기
  @PostMapping("/find-pw")
  public ResponseEntity<PasswordFindResponse> findPassword(@RequestBody Member member) {
    try {
      logger.info("비밀번호 찾기 요청: UserId={}, Email={}", member.getUserId(), member.getEmail());

      // 회원 존재 여부 확인
      if (!memberService.checkEmailExists(member.getEmail())) {
        logger.warn("등록된 회원이 존재하지 않음: Email={}", member.getEmail());
        return ResponseEntity.status(404).body(
            new PasswordFindResponse(false, "등록된 회원이 존재하지 않습니다.", null, null));
      }

      // 사용자 아이디로 회원 정보 조회
      Member existingMember = memberService.validateMemberByUserId(member.getUserId());
      if (existingMember == null) {
        logger.warn("해당 아이디로 등록된 회원이 존재하지 않음: UserId={}", member.getUserId());
        return ResponseEntity.status(404).body(
            new PasswordFindResponse(false, "해당 아이디로 등록된 회원이 존재하지 않습니다.", null, null));
      }

      // 아이디와 이메일 일치 여부 확인
      if (!existingMember.getEmail().equals(member.getEmail())) {
        logger.warn("아이디와 이메일 불일치: UserId={}, 입력된 Email={}, 실제 Email={}",
            member.getUserId(), member.getEmail(), existingMember.getEmail());
        return ResponseEntity.status(400).body(
            new PasswordFindResponse(false, "아이디와 이메일이 일치하지 않습니다.", null, null));
      }

      // 인증 코드 검증
      boolean isCodeValid = memberService.verifyEmailCode(member.getEmail(), member.getVerificationCode());
      if (!isCodeValid) {
        logger.warn("인증 코드 검증 실패: Email={}, 입력된 코드={}", member.getEmail(), member.getVerificationCode());
        return ResponseEntity.status(400).body(
            new PasswordFindResponse(false, "인증 코드가 올바르지 않습니다.", null, null));
      }

      // 비밀번호 찾기 로직
      PasswordFindResponse response = memberService.findPassword(member);

      // 비밀번호 찾기 성공 여부에 따라 응답
      if (response.isSuccess()) {
        logger.info("비밀번호 찾기 성공: UserId={}, 임시 비밀번호가 이메일로 전송되었습니다.", member.getUserId());
        return ResponseEntity.ok(response);
      } else {
        logger.error("비밀번호 찾기 실패: UserId={}", member.getUserId());
        return ResponseEntity.status(500).body(
            new PasswordFindResponse(false, "비밀번호 찾기에 실패했습니다.", null, null));
      }
    } catch (Exception e) {
      // 예외 발생 시 상세 로그 기록
      logger.error("비밀번호 찾기 중 오류 발생: UserId={}, Email={}, 오류={}",
          member.getUserId(), member.getEmail(), e.getMessage(), e);
      return ResponseEntity.status(500).body(
          new PasswordFindResponse(false, "서버 오류가 발생했습니다.", null, null));
    }
  }

  // 이메일 인증 코드 발송
  @PostMapping("/sendVerificationCode")
  public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> payload) {
    String email = payload.get("email");
    try {
      memberService.sendVerificationCode(email);
      return ResponseEntity.ok("Verification code sent successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Failed to send verification code: " + e.getMessage());
    }
  }

  // 이메일 인증 코드 검증
  @PostMapping("/verify")
  public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, String> payload) {
    String email = payload.get("email");
    String code = payload.get("code");
    Map<String, Object> response = new HashMap<>();
    try {
      boolean isVerified = memberService.verifyEmailCode(email, code);
      response.put("success", isVerified);
      response.put("message", isVerified ? "Email verified successfully" : "Invalid verification code");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "Error verifying email: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/getMember")
  public ResponseEntity<MemberResponse> getMember(@RequestParam int memberId) {
    logger.info("요청 받은 memberId: {}", memberId);

    try {
      Member foundMember = memberService.getMember(memberId);

      if (foundMember != null && foundMember.getUserId() != null) {
        MemberResponse response = new MemberResponse(
            foundMember.getMemberId(),
            foundMember.getUserId(),
            foundMember.getName(),
            foundMember.getPhone(),
            foundMember.getEmail(),
            foundMember.getRole(),
            foundMember.isSmsNoti(),
            foundMember.isEmailNoti());

        logger.info("회원 정보 조회 성공: {}", response);
        return ResponseEntity.ok().body(response);
      } else {
        logger.warn("해당 회원을 찾을 수 없음: memberId={}", memberId);
        return ResponseEntity.status(404).build();
      }
    } catch (Exception e) {
      logger.error("회원 조회 중 오류 발생: memberId={}", memberId, e);
      return ResponseEntity.status(500).body(null);
    }
  }

  // 회원탈퇴
  @DeleteMapping("/deleteAccount/{memberId}")
  public ResponseEntity<String> deleteAccount(@PathVariable int memberId) {
    try {
      memberService.deleteMember(memberId);
      return ResponseEntity.ok("회원 탈퇴 성공");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 실패: " + e.getMessage());
    }
  }

  // 회원 목록 조회
  @GetMapping(value = "/getMemberList")
  public ApiResponse<List<Member>> getMemberList(@RequestParam String search,
      @RequestParam String searchType,
      @RequestParam(value = "start", required = false, defaultValue = "1") int start,
      @RequestParam(value = "display", required = true, defaultValue = "20") int display) {
    return memberService.getMemberList(search, searchType, start, display);
  }

  // 자동로그인시 가져오는 회원정보
  @GetMapping(value = "/getAutoLogin")
  public ResponseEntity<?> getAutoLogin(HttpServletResponse response, HttpServletRequest request) {
    String refreshToken = getTokenFromCookies(request);

    if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰");
    }

    String userId = tokenProvider.getUserId(refreshToken);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 정보 없음");
    }
    Member member = memberService.validateMemberByUserId(userId);

    String accessToken = tokenProvider.createToken(userId, member.getRole());

    setTokenInCookie(response, accessToken, "access_token", true, true, "/",
        acessCookieExpired);
    // 응답 데이터 구성

    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("message", "로그인 성공");
    responseBody.put("accessToken", accessToken);
    responseBody.put("refreshToken", refreshToken);
    responseBody.put("userId", userId);
    responseBody.put("memberId", Integer.toString(member.getMemberId()));
    responseBody.put("name", member.getName());
    responseBody.put("role", member.getRole());
    responseBody.put("failedAttempts", Integer.toString(member.getFailedAttempts())); // 실패 횟수 추가
    return ResponseEntity.ok().body(responseBody);

  }

  public String getTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies(); // 수정된 부분
    if (cookies != null) {
      for (Cookie cookie : cookies) {

        // refresh_token 으로 검사
        if ("refresh_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

}