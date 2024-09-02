package com.example.cucucook.domain;

public class LoginRequest {

    private String userId;
    private String password;

    // 기본 생성자
    public LoginRequest() {
    }

    // 매개변수 생성자
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    // Getter 및 Setter
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
}
