package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Payload de connexion")
public class LoginRequest {
    @Schema(description = "Username", example = "john.doe")
    @NotBlank
    private String login;

    @Schema(description = "Mot de passe", example = "secret123")
    @NotBlank
    private String password;

    public LoginRequest() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
