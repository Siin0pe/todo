package com.example.todo.api.security;

import com.example.todo.auth.AuthSession;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class UserSecurityContext implements SecurityContext {
    private final AuthSession session;
    private final boolean secure;
    private final AuthPrincipal principal;

    public UserSecurityContext(AuthSession session, boolean secure) {
        this.session = session;
        this.secure = secure;
        this.principal = new AuthPrincipal(session.getUserId(), session.getUsername());
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Token";
    }

    public Long getUserId() {
        return session.getUserId();
    }

    public static final class AuthPrincipal implements Principal {
        private final Long userId;
        private final String username;

        public AuthPrincipal(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
