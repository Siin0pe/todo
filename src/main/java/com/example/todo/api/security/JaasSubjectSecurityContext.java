package com.example.todo.api.security;

import com.example.todo.security.jaas.RolePrincipal;
import com.example.todo.security.jaas.UserPrincipal;
import jakarta.ws.rs.core.SecurityContext;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Set;

public class JaasSubjectSecurityContext implements SecurityContext {
    private final Subject subject;
    private final UserPrincipal principal;
    private final boolean secure;

    public JaasSubjectSecurityContext(Subject subject, boolean secure) {
        this.subject = subject;
        this.secure = secure;
        Set<UserPrincipal> principals = subject == null ? null : subject.getPrincipals(UserPrincipal.class);
        this.principal = principals == null || principals.isEmpty() ? null : principals.iterator().next();
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (subject == null || role == null) {
            return false;
        }
        return subject.getPrincipals(RolePrincipal.class).contains(new RolePrincipal(role));
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
        return principal == null ? null : principal.getUserId();
    }

    public Subject getSubject() {
        return subject;
    }
}
