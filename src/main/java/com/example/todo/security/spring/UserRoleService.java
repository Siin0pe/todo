package com.example.todo.security.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserRoleService {
    private static final String USER_ROLE = "ROLE_USER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final Set<String> adminUsers;

    public UserRoleService(@Value("${todo.security.admin-users:}") String adminUsersRaw) {
        this.adminUsers = parseAdminUsers(adminUsersRaw);
    }

    public List<String> resolveRoles(String username) {
        List<String> roles = new ArrayList<>();
        roles.add(USER_ROLE);
        if (username != null && adminUsers.contains(username)) {
            roles.add(ADMIN_ROLE);
        }
        return roles;
    }

    private Set<String> parseAdminUsers(String adminUsersRaw) {
        Set<String> parsed = new HashSet<>();
        if (adminUsersRaw == null || adminUsersRaw.isBlank()) {
            return parsed;
        }
        String[] values = adminUsersRaw.split(",");
        for (String value : values) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                parsed.add(trimmed);
            }
        }
        return parsed;
    }
}
