package com.example.cucucook.exception;

public class AccountLockedException extends RuntimeException {
    private final int failedAttempts; // 실패한 시도 횟수
    private final long remainingTime; // 남은 잠금 시간(초 단위)

    // 생성자
    public AccountLockedException(String message, int failedAttempts, long remainingTime) {
        super(message);
        this.failedAttempts = failedAttempts;
        this.remainingTime = remainingTime;
    }

    // 실패한 시도 횟수 반환
    public int getFailedAttempts() {
        return failedAttempts;
    }

    // 남은 잠금 시간 반환
    public long getRemainingTime() {
        return remainingTime;
    }
}
