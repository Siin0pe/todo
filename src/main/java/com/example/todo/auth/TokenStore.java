package com.example.todo.auth;

import com.example.todo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenStore.class);
    private static final ConcurrentHashMap<String, AuthSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private TokenStore() {
    }

    public static AuthSession createSession(User user) {
        return createSession(user.getId(), user.getUsername());
    }

    public static AuthSession createSession(Long userId, String username) {
        return createSession(userId, username, Instant.now().plus(DEFAULT_TTL));
    }

    public static AuthSession createSession(Long userId, String username, Instant expiresAt) {
        String token = UUID.randomUUID().toString();
        AuthSession session = new AuthSession(token, userId, username, Instant.now(), expiresAt);
        SESSIONS.put(token, session);
        LOGGER.info("auth_session_created userId={} expiresAt={} activeSessions={}", userId, expiresAt, SESSIONS.size());
        return session;
    }

    public static AuthSession getSession(String token) {
        if (token == null || token.isEmpty()) {
            LOGGER.warn("auth_session_lookup_missing_token");
            return null;
        }
        AuthSession session = SESSIONS.get(token);
        if (session == null) {
            LOGGER.warn("auth_session_not_found");
            return null;
        }
        if (session.isExpired(Instant.now())) {
            SESSIONS.remove(token);
            LOGGER.warn("auth_session_expired userId={}", session.getUserId());
            return null;
        }
        return session;
    }
}
