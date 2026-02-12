package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "CategoryPageResponse", description = "Reponse paginee des categories")
public class CategoryPageResponse extends PaginatedResponse<CategoryResponse> {

    @Override
    @ArraySchema(arraySchema = @Schema(description = "Liste de categories"), schema = @Schema(implementation = CategoryResponse.class))
    public List<CategoryResponse> getItems() {
        return super.getItems();
    }
}
