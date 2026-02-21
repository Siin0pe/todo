package com.example.todo.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Payload de connexion")
public class LoginRequest {
    @Schema(description = "Nom d'utilisateur ou email", example = "user")
    @NotBlank
    @JsonAlias("login")
    private String username;

    @Schema(description = "Mot de passe", example = "secret123")
    @NotBlank
    private String password;

    public LoginRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLogin() {
        return username;
    }

    public void setLogin(String login) {
        this.username = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
