package com.example.cucucook.exception;

public class InvalidPasswordException extends RuntimeException {

    private final int failedAttempts;
    private final long lockoutTimeRemaining;

    public InvalidPasswordException(String message, int failedAttempts, long lockoutTimeRemaining) {
        super(message);
        this.failedAttempts = failedAttempts;
        this.lockoutTimeRemaining = lockoutTimeRemaining;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public long getLockoutTimeRemaining() {
        return lockoutTimeRemaining;
    }
}
