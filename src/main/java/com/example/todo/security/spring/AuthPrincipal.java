package com.example.todo.security.spring;

import java.security.Principal;

public final class AuthPrincipal implements Principal {
    private final Long userId;
    private final String name;

    public AuthPrincipal(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }
}
