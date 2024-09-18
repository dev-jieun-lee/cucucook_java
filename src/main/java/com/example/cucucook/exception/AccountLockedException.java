package com.example.cucucook.exception;

import java.time.LocalDateTime;

public class AccountLockedException extends RuntimeException {
    private int failedAttempts;
    private long lockoutDuration; // 잠금 시간이 있을 경우 남은 시간
    private final LocalDateTime lockTime;

    public AccountLockedException(String message, int failedAttempts, long lockoutDuration, LocalDateTime lockTime) {
        super(message);
        this.failedAttempts = failedAttempts;
        this.lockoutDuration = lockoutDuration;
        this.lockTime = lockTime;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getLockoutDuration() {
        return lockoutDuration;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }
}
