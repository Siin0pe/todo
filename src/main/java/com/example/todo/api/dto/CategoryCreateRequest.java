package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CategoryCreateRequest", description = "Payload de creation d'une categorie")
public class CategoryCreateRequest {
    @Schema(description = "Libelle categorie", example = "Immobilier", maxLength = 64)
    @NotBlank
    @Size(max = 64)
    private String label;

    public CategoryCreateRequest() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
