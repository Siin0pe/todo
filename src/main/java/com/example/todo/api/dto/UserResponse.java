package com.example.todo.api.dto;

public class UserResponse {
    private Long id;
    private String username;
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
