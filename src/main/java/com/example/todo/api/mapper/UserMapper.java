package com.example.todo.api.mapper;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "annonces", ignore = true)
    User toEntity(RegisterRequest request);
}
