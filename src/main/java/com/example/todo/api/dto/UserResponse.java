package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponse", description = "Representation utilisateur")
public class UserResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "john.doe")
    private String username;
    @Schema(example = "john.doe@example.com")
    private String email;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final UserResponse dto = new UserResponse();

        public Builder id(Long id) {
            dto.setId(id);
            return this;
        }

        public Builder username(String username) {
            dto.setUsername(username);
            return this;
        }

        public Builder email(String email) {
            dto.setEmail(email);
            return this;
        }

        public UserResponse build() {
            return dto;
        }
    }
}
