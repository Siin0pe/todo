package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Format de reponse d'erreur")
public class ErrorResponse {
    @Schema(description = "Message d'erreur lisible", example = "Validation failed: title is required")
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
