package com.example.todo.auth;

import com.example.todo.model.User;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStore {
    private static final ConcurrentHashMap<String, AuthSession> SESSIONS = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    public static AuthSession createSession(User user) {
        String token = UUID.randomUUID().toString();
        AuthSession session = new AuthSession(token, user.getId(), user.getUsername(), Instant.now());
        SESSIONS.put(token, session);
        return session;
    }

    public static AuthSession getSession(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return SESSIONS.get(token);
    }
}
