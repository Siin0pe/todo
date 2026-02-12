package com.example.todo.api.security;

import jakarta.ws.rs.core.SecurityContext;

public class AuthUtil {
    private AuthUtil() {
    }

    public static Long requireUserId(SecurityContext securityContext) {
        if (securityContext instanceof UserSecurityContext) {
            return ((UserSecurityContext) securityContext).getUserId();
        }
        return null;
    }
}
