package com.example.todo.api.rest;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.api.dto.PaginatedResponse;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CategoryResourceIT extends RestIntegrationTestBase {

    @Test
    void listCategories_returnsPaginatedResponse() {
        Response response = target("categories")
                .queryParam("page", 0)
                .queryParam("size", 3)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        PaginatedResponse<CategoryResponse> payload = response.readEntity(
                new GenericType<PaginatedResponse<CategoryResponse>>() {
                });

        assertEquals(0, payload.getPage());
        assertEquals(3, payload.getSize());
        assertEquals(3, payload.getCount());
        assertEquals("Category A", payload.getItems().get(0).getLabel());
    }

    @Test
    void createCategory_validationError_returns400() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setLabel("");

        Response response = target("categories")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void createCategory_returnsCreated() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setLabel("Category Z");

        Response response = target("categories")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        CategoryResponse created = response.readEntity(CategoryResponse.class);
        assertNotNull(created.getId());
        assertEquals("Category Z", created.getLabel());
    }

    @Test
    void updateAndDeleteCategory_flow() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setLabel("Category Y");

        Response createResponse = target("categories")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        CategoryResponse created = createResponse.readEntity(CategoryResponse.class);

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setLabel("Category Y2");

        Response updateResponse = target("categories/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));

        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());
        CategoryResponse updated = updateResponse.readEntity(CategoryResponse.class);
        assertEquals("Category Y2", updated.getLabel());

        Response deleteResponse = target("categories/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        Response getResponse = target("categories/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
    }

    @Test
    void updateCategory_notFound_returns404() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setLabel("Missing");

        Response updateResponse = target("categories/999999")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), updateResponse.getStatus());
        ErrorResponse error = updateResponse.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }
}
