package com.example.cucucook.controller;
//요청데이터 정의 클래스
//HTTP 요청의 본문에서 클라이언트가 보내는 데이터를 받아서 컨트롤러에서 처리
//도메인과 따로관리(유지보수 용이)

public class PhoneRequest {
    private String phone;

    // 기본 생성자
    public PhoneRequest() {}

    // 매개변수가 있는 생성자
    public PhoneRequest(String phone) {
        this.phone = phone;
    }

    // getter 및 setter
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
