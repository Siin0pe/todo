package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Token d'authentification")
public class LoginResponse {
    @Schema(description = "Token bearer a envoyer dans Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
