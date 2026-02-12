package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CategoryUpdateRequest", description = "Payload de mise a jour d'une categorie")
public class CategoryUpdateRequest {
    @Schema(description = "Libelle categorie", example = "Services", maxLength = 64)
    @NotBlank
    @Size(max = 64)
    private String label;

    public CategoryUpdateRequest() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
