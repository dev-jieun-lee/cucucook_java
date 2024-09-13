// AccountLockedException.java
package com.example.cucucook.exception;

public class AccountLockedException extends RuntimeException {
    private int failedAttempts;
    private long lockoutDuration;

    public AccountLockedException(String message, int failedAttempts, long lockoutDuration) {
        super(message); // RuntimeException 생성자 호출
        this.failedAttempts = failedAttempts;
        this.lockoutDuration = lockoutDuration;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getLockoutDuration() {
        return lockoutDuration;
    }
}
