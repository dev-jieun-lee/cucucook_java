package com.example.cucucook.controller;

import com.example.cucucook.domain.Member;
import com.example.cucucook.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

        //로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Member loginRequest) {
        Member member = memberService.login(loginRequest.getUserId(), loginRequest.getPassword());
        if (member != null) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    //로그아웃
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
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
        logger.info("아이디 '{}' 중복 여부를 확인하는 요청을 받았습니다.", userId);

        logger.debug("memberService.checkUserIdExists 메서드를 호출합니다. userId: {}", userId);
        boolean userIdExists = memberService.checkUserIdExists(userId);

        logger.debug("memberService.checkUserIdExists 호출 결과 - userId '{}': {}", userId, userIdExists);

        boolean isAvailable = !userIdExists;

        if (isAvailable) {
            logger.info("아이디 '{}'는 사용 가능합니다.", userId);
        } else {
            logger.warn("아이디 '{}'는 이미 사용 중입니다.", userId);
        }

        logger.debug("userId '{}'에 대한 응답 값: {}", userId, isAvailable);

        return ResponseEntity.ok(isAvailable);
    }



    // 이메일 중복 체크
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok(memberService.checkEmailExists(email));
    }


}
