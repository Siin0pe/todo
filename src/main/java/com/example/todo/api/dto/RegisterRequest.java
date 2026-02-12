package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Payload d'inscription utilisateur")
public class RegisterRequest {
    @Schema(example = "john.doe", maxLength = 64)
    @NotBlank
    @Size(max = 64)
    private String username;

    @Schema(example = "john.doe@example.com", maxLength = 128)
    @NotBlank
    @Email
    @Size(max = 128)
    private String email;

    @Schema(example = "secret123", minLength = 6, maxLength = 255)
    @NotBlank
    @Size(min = 6, max = 255)
    private String password;

    public RegisterRequest() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
