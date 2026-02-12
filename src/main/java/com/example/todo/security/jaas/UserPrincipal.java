package com.example.todo.security.jaas;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

public class UserPrincipal implements Principal, Serializable {
    private final String username;
    private final Long userId;

    public UserPrincipal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }

    @Override
    public String getName() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserPrincipal)) {
            return false;
        }
        UserPrincipal that = (UserPrincipal) other;
        return Objects.equals(username, that.username) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, userId);
    }
}
