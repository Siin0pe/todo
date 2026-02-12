package com.example.todo.api.mapper;

import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.model.Category;

public class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .label(category.getLabel())
                .build();
    }
}
