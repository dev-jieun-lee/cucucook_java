package com.example.cucucook.domain;

import java.io.Serializable;

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

    //회원 고유번호
    private int memberId;

    //회원아이디
    private String userId;

    //회원이름
    private String name;

    //회원번호
    private String phone;

    //회원 패스워드
    private String password;

    //회원 권한 (0:관리자, 1:사용자, 2:총괄관리자)
    private String role;

    //회원 이메일
    private String email;

    //sms 수신여부
    private boolean smsNoti;

    //이메일 수신여부
    private boolean emailNoti;

}