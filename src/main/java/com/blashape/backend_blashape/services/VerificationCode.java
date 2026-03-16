package com.blashape.backend_blashape.services;

class VerificationCode {

    private final String code;
    private final long expiresAt;
    private final long createdAt;

    public VerificationCode(String code, long expiresAt, long createdAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getCode() {
        return code;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}