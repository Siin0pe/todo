package com.example.todo.api.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginatedResponseUnitTest {

    @Test
    void constructor_setsCountFromItems() {
        PaginatedResponse<String> response = new PaginatedResponse<>(1, 3, Arrays.asList("a", "b"));

        assertEquals(1, response.getPage());
        assertEquals(3, response.getSize());
        assertEquals(2, response.getCount());
    }

    @Test
    void setItems_updatesCount() {
        PaginatedResponse<String> response = new PaginatedResponse<>();
        response.setItems(Collections.singletonList("value"));

        assertEquals(1, response.getCount());
    }
}
