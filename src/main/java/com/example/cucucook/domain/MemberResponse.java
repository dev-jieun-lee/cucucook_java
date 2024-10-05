package com.example.cucucook.domain;

public class MemberResponse {
    private int memberId;
    private String userId;
    private String name;
    private String phone;
    private String email;
    private String role;
    private boolean smsNoti;
    private boolean emailNoti;

    // 생성자
    public MemberResponse(int memberId, String userId, String name, String phone, String email, String role,
            boolean smsNoti, boolean emailNoti) {
        this.memberId = memberId;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.smsNoti = smsNoti;
        this.emailNoti = emailNoti;
    }

    // Getter와 Setter
    public int getMemberId() {
        return memberId;
    }

    public void berId(int memberId) {
        this.memberId = memberId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isSmsNoti() {
        return smsNoti;
    }

    public void setSmsNoti(boolean smsNoti) {
        this.smsNoti = smsNoti;
    }

    public boolean isEmailNoti() {
        return emailNoti;
    }

    public void setEmailNoti(boolean emailNoti) {
        this.emailNoti = emailNoti;
    }
}
