package com.example.todo.api.mapper;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "annonces", ignore = true)
    Category toEntity(CategoryCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "annonces", ignore = true)
    void updateFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);
}
