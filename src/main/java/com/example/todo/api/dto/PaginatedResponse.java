package com.example.todo.api.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private int page;
    private int size;
    private int count;
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
