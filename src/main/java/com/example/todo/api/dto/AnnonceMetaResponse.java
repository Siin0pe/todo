package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

@Schema(name = "AnnonceMetaResponse", description = "Metadata exposant les champs de recherche et tri des annonces")
public class AnnonceMetaResponse {
    @ArraySchema(schema = @Schema(example = "title"))
    private Set<String> sortableFields;

    @ArraySchema(schema = @Schema(example = "description"))
    private Set<String> searchableFields;

    @ArraySchema(schema = @Schema(example = "status"))
    private List<String> filterableQueryParams;

    public AnnonceMetaResponse() {
    }

    public AnnonceMetaResponse(Set<String> sortableFields, Set<String> searchableFields, List<String> filterableQueryParams) {
        this.sortableFields = sortableFields;
        this.searchableFields = searchableFields;
        this.filterableQueryParams = filterableQueryParams;
    }

    public Set<String> getSortableFields() {
        return sortableFields;
    }

    public void setSortableFields(Set<String> sortableFields) {
        this.sortableFields = sortableFields;
    }

    public Set<String> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(Set<String> searchableFields) {
        this.searchableFields = searchableFields;
    }

    public List<String> getFilterableQueryParams() {
        return filterableQueryParams;
    }

    public void setFilterableQueryParams(List<String> filterableQueryParams) {
        this.filterableQueryParams = filterableQueryParams;
    }
}
