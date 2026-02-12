package com.example.todo.api;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.service.CategoryService;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryResourceUnitTest {

    @Test
    void listCategories_returnsOk() {
        CategoryService service = mock(CategoryService.class);
        PaginatedResponse<CategoryResponse> payload = new PaginatedResponse<>(0, 1, Collections.emptyList());
        when(service.listCategories(0, 1)).thenReturn(payload);

        CategoryResource resource = new CategoryResource(service);
        Response response = resource.listCategories(0, 1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(payload, response.getEntity());
    }

    @Test
    void createCategory_returnsCreated() {
        CategoryService service = mock(CategoryService.class);
        CategoryResponse created = new CategoryResponse();
        created.setId(5L);
        created.setLabel("Work");
        when(service.createCategory(org.mockito.ArgumentMatchers.any(CategoryCreateRequest.class))).thenReturn(created);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/api/categories"));

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setLabel("Work");

        CategoryResource resource = new CategoryResource(service);
        Response response = resource.createCategory(request, uriInfo);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(created, response.getEntity());
    }
}
