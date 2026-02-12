package com.example.todo.api.security;

import com.example.todo.security.jaas.UserPrincipal;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class AuthUtil {
    private AuthUtil() {
    }

    public static Long requireUserId(SecurityContext securityContext) {
        if (securityContext instanceof JaasSubjectSecurityContext) {
            return ((JaasSubjectSecurityContext) securityContext).getUserId();
        }
        if (securityContext instanceof UserSecurityContext) {
            return ((UserSecurityContext) securityContext).getUserId();
        }
        if (securityContext != null) {
            Principal principal = securityContext.getUserPrincipal();
            if (principal instanceof UserPrincipal) {
                return ((UserPrincipal) principal).getUserId();
            }
        }
        return null;
    }
}
