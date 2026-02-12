package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "AnnoncePageResponse", description = "Reponse paginee des annonces")
public class AnnoncePageResponse extends PaginatedResponse<AnnonceResponse> {

    @Override
    @ArraySchema(arraySchema = @Schema(description = "Liste d'annonces"), schema = @Schema(implementation = AnnonceResponse.class))
    public List<AnnonceResponse> getItems() {
        return super.getItems();
    }
}
