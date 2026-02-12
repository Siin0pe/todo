package com.example.todo.api.security;

import com.example.todo.auth.AuthSession;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class UserSecurityContext implements SecurityContext {
    private final AuthSession session;
    private final boolean secure;

    public UserSecurityContext(AuthSession session, boolean secure) {
        this.session = session;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> session.getUsername();
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
}
