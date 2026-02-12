package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CategoryResponse", description = "Representation d'une categorie")
public class CategoryResponse {
    @Schema(example = "2")
    private Long id;
    @Schema(example = "Immobilier")
    private String label;

    public CategoryResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CategoryResponse dto = new CategoryResponse();

        public Builder id(Long id) {
            dto.setId(id);
            return this;
        }

        public Builder label(String label) {
            dto.setLabel(label);
            return this;
        }

        public CategoryResponse build() {
            return dto;
        }
    }
}
