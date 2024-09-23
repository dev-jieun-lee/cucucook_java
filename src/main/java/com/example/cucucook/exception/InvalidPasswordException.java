package com.example.cucucook.exception;

public class InvalidPasswordException extends RuntimeException {
    private int failedAttempts;
    private long lockoutRemainingSeconds;

    public InvalidPasswordException(String message, int failedAttempts, long lockoutRemainingSeconds) {
        super(message);
        this.failedAttempts = failedAttempts;
        this.lockoutRemainingSeconds = lockoutRemainingSeconds;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getLockoutRemainingSeconds() {
        return lockoutRemainingSeconds;
    }
}
