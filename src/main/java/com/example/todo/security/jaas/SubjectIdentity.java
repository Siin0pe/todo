package com.example.todo.security.jaas;

import javax.security.auth.Subject;
import java.util.Set;

public final class SubjectIdentity {
    private SubjectIdentity() {
    }

    public static Long currentUserId() {
        Subject subject = SubjectContextHolder.get();
        if (subject == null) {
            return null;
        }
        Set<UserPrincipal> principals = subject.getPrincipals(UserPrincipal.class);
        if (principals == null || principals.isEmpty()) {
            return null;
        }
        return principals.iterator().next().getUserId();
    }

    public static boolean hasRole(String role) {
        Subject subject = SubjectContextHolder.get();
        if (subject == null || role == null) {
            return false;
        }
        return subject.getPrincipals(RolePrincipal.class).contains(new RolePrincipal(role));
    }
}
