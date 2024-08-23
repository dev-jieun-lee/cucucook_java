package com.example.cucucook.controller;

import com.example.cucucook.config.JwtTokenProvider;
import com.example.cucucook.domain.Member;
import com.example.cucucook.service.MemberService;
import com.example.cucucook.util.ValidationException;
import com.example.cucucook.domain.PasswordFindResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Member loginRequest) {
        Member member = memberService.validateMember(loginRequest.getUserId(), loginRequest.getPassword());
        if (member != null) {
             String token = tokenProvider.createToken(member.getUserId(), member.getRole());
            String userId = member.getUserId();
            String name = member.getName();

            // 여러 값을 포함하는 Map 생성
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", userId);
            response.put("name", name);

            return ResponseEntity.ok().body(response); // JWT 토큰과 사용자 ID 반환
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Authentication failed")); // 로그인 실패
        }
    }


    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
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

    //핸드폰번호 중복 확인
    @PostMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneNumber(@RequestBody PhoneRequest request) {
        boolean exists = memberService.checkPhoneExists(request.getPhone());
        System.out.println("응답값 : "+exists);
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
    //아이다찾기 응답부분
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
    @PostMapping("/find-pw")
    public ResponseEntity<PasswordFindResponse> findPassword(@RequestBody Member member) {
        try {
            PasswordFindResponse response = memberService.findPassword(member);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new PasswordFindResponse(false, "서버 오류가 발생했습니다.", null, null));
        }
    }
}