package com.example.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "PaginatedResponse", description = "Structure standard des reponses paginees")
public class PaginatedResponse<T> {
    @Schema(description = "Numero de page (base 0)", example = "0")
    private int page;
    @Schema(description = "Taille demandee pour la page", example = "20")
    private int size;
    @Schema(description = "Nombre d'elements renvoyes", example = "2")
    private int count;
    @Schema(description = "Elements de la page courante")
    private List<T> items;

    public PaginatedResponse() {
    }

    public PaginatedResponse(int page, int size, List<T> items) {
        this.page = page;
        this.size = size;
        this.items = items;
        this.count = items == null ? 0 : items.size();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
        this.count = items == null ? 0 : items.size();
    }
}
