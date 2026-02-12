package com.example.todo.api.mapper;

import com.example.todo.api.dto.UserResponse;
import com.example.todo.model.User;

public class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
