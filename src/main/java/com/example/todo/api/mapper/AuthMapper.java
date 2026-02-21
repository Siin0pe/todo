package com.example.todo.api.mapper;

import com.example.todo.api.dto.LoginResponse;
import com.example.todo.auth.AuthSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    LoginResponse toLoginResponse(AuthSession session);
}
