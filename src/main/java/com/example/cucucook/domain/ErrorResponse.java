package com.example.cucucook.domain;

public class ErrorResponse {

    private String message;

    // 기본 생성자
    public ErrorResponse() {
    }

    // 매개변수 생성자
    public ErrorResponse(String message) {
        this.message = message;
    }

    // Getter 및 Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
