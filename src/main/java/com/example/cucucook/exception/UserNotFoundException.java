package com.example.cucucook.exception;

public class UserNotFoundException extends RuntimeException {
    private String userId; // 사용자 아이디를 추가로 저장

    // 기본 생성자
    public UserNotFoundException(String message) {
        super(message);
    }

    // 사용자 아이디를 추가로 저장할 수 있는 생성자
    public UserNotFoundException(String message, String userId) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
