package com.example.todo.api.mapper;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceDTO;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.model.Annonce;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.sql.Timestamp;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnnonceMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "date", source = "date", qualifiedByName = "timestampToIso")
    @Mapping(target = "status", source = "status")
    AnnonceDTO toDto(Annonce annonce);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    Annonce toEntity(AnnonceCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateFromRequest(AnnonceUpdateRequest request, @MappingTarget Annonce annonce);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    void patchFromRequest(AnnoncePatchRequest request, @MappingTarget Annonce annonce);

    @Named("timestampToIso")
    default String timestampToIso(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().toString();
    }
}
