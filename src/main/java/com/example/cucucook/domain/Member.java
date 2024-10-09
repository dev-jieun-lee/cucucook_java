package com.example.cucucook.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    // 회원 고유번호
    private int memberId;

    // 회원아이디
    private String userId;
    private String socialId; // 카카오 ID 추가

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    // 회원이름
    private String name;

    // 회원번호
    private String phone;

    // 회원 패스워드
    private String password;

    // 회원 권한 (0:관리자, 1:사용자, 2:총괄관리자)
    private String role;

    // 회원 이메일
    private String email;

    // sms 수신여부
    private boolean smsNoti;

    // 이메일 수신여부
    private boolean emailNoti;

    private LocalDateTime lockoutTime;
    private int failedAttempts;

    // 소셜로그인 플래그
    private boolean isSocialLogin;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLockoutTime() {
        return lockoutTime;
    }

    public void setLockoutTime(LocalDateTime lockoutTime) {
        this.lockoutTime = lockoutTime;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    @JsonIgnore
    public String getVerificationCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}