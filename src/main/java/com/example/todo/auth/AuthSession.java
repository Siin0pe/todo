package com.example.todo.auth;

import java.time.Instant;

public class AuthSession {
    private final String token;
    private final Long userId;
    private final String username;
    private final Instant createdAt;
    private final Instant expiresAt;

    public AuthSession(String token, Long userId, String username, Instant createdAt) {
        this(token, userId, username, createdAt, null);
    }

    public AuthSession(String token, Long userId, String username, Instant createdAt, Instant expiresAt) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && now != null && !expiresAt.isAfter(now);
    }
}
