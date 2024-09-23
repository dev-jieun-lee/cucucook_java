package com.example.cucucook.domain;

public class PasswordFindResponse {
    private boolean success;
    private String message;
    private String foundId; // 찾은 ID 또는 임시 비밀번호 등 추가 정보
    private String tempPassword; // (선택 사항) 임시 비밀번호, 보안 상 이유로 민감한 정보는 제외할 수도 있음

    // 기본 생성자
    public PasswordFindResponse() {}

    // 생성자
    public PasswordFindResponse(boolean success, String message, String foundId, String tempPassword) {
        this.success = success;
        this.message = message;
        this.foundId = foundId;
        this.tempPassword = tempPassword;
    }

    // Getter 및 Setter
    public boolean isSuccess() {
        return success;
    }

    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }


    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFoundId() {
        return foundId;
    }

    public void setFoundId(String foundId) {
        this.foundId = foundId;
    }
}
