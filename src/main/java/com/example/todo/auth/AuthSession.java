package com.example.todo.auth;

import java.time.Instant;

public class AuthSession {
    private final String token;
    private final Long userId;
    private final String username;
    private final Instant createdAt;

    public AuthSession(String token, Long userId, String username, Instant createdAt) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
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
}
