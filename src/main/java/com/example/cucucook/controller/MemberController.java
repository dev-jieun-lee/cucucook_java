package com.example.cucucook.controller;

import com.example.cucucook.domain.Member;
import com.example.cucucook.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
             System.out.println("로그인 성공");
            return ResponseEntity.ok("Login successful");
        } else {
            System.out.println("로그인 실패");
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
        System.out.println("로그아웃 성공");
        System.out.println("auth: " + auth.getPrincipal() + " " + auth.getDetails());
    }

    //로그인 체크
    @GetMapping("/check-login")
    public void  checkLoginStatus(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false); // 기존 세션이 없으면 null 반환

        if (auth == null
            || !auth.isAuthenticated()
            || (auth.getPrincipal() instanceof UserDetails && ((UserDetails) auth.getPrincipal()).getUsername().equals("anonymousUser"))
            || session == null) {
            // 인증되지 않은 경우
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드 반환
            System.out.println("인증되지 않은 경우 401");
        } else {
            // 인증된 경우
            response.setStatus(HttpServletResponse.SC_OK); // 200 상태 코드 반환
            System.out.println("인증된 경우 200");
            System.out.println("200의 경우: " + HttpServletResponse.SC_OK);
            System.out.println("auth: " + auth.getPrincipal() + " " + auth.getDetails());
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
