package com.example.cucucook.domain;

import java.time.LocalDateTime;

public class VerificationCode {

    private Long id; // 자동 증가되는 기본 키
    private String phoneNumber; // 사용자 전화번호
    private String code; // 6자리 인증 코드
    private LocalDateTime createdAt; // 인증 코드 생성 시간
    private LocalDateTime expiresAt; // 인증 코드 만료 시간

    // 기본 생성자
    public VerificationCode() {
    }

    // 매개변수를 받는 생성자
    public VerificationCode(String phoneNumber, String code, LocalDateTime expiresAt) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now(); // 생성 시점의 시간으로 초기화
    }

    // Getter 및 Setter 메서드
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "VerificationCode{" +
                "id=" + id +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", code='" + code + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
