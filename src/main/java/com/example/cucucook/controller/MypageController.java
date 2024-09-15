package com.example.cucucook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cucucook.domain.Member;
import com.example.cucucook.service.MypageService;

@RestController
@RequestMapping("/api/mypage")
public class MypageController {

    @Autowired
    private MypageService mypageService;

    @PostMapping("/verify-password")
    public ResponseEntity<String> verifyPassword(@RequestBody Member member) {
        boolean isVerified = mypageService.verifyPassword(member.getUserId(), member.getPassword());

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("회원 정보가 없거나 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호가 일치하면 200 OK와 함께 메시지 반환
        return ResponseEntity.ok("비밀번호가 확인되었습니다.");
    }
}
