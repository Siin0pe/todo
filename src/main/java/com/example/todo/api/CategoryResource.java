package com.example.todo.api;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

@Path("/categories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {
    private final CategoryService categoryService = new CategoryService();

    @GET
    public Response listCategories(@DefaultValue("0") @QueryParam("page") int page,
                                   @DefaultValue("20") @QueryParam("size") int size) {
        PaginatedResponse<CategoryResponse> response = categoryService.listCategories(page, size);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getCategory(@PathParam("id") Long id) {
        CategoryResponse category = categoryService.findById(id);
        return Response.ok(category).build();
    }

    @POST
    public Response createCategory(@Valid CategoryCreateRequest request, @Context UriInfo uriInfo) {
        CategoryResponse created = categoryService.createCategory(request);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.getId()))
                .build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCategory(@PathParam("id") Long id, @Valid CategoryUpdateRequest request) {
        CategoryResponse updated = categoryService.updateCategory(id, request);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCategory(@PathParam("id") Long id) {
        categoryService.deleteCategory(id);
        return Response.noContent().build();
    }
}
