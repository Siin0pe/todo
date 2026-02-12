package com.example.todo.api.rest;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.model.Annonce;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EndToEndFlowIT extends RestIntegrationTestBase {

    @Test
    void fullAppFlow() {
        UserResponse user = registerUser("flow", "flow@example.com", "secret123");
        String token = loginToken("flow", "secret123");

        CategoryCreateRequest categoryRequest = new CategoryCreateRequest();
        categoryRequest.setLabel("FlowCategory");

        Response categoryResponse = target("categories")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(categoryRequest));

        assertEquals(Response.Status.CREATED.getStatusCode(), categoryResponse.getStatus());
        CategoryResponse category = categoryResponse.readEntity(CategoryResponse.class);
        assertNotNull(category.getId());

        AnnonceCreateRequest createRequest = new AnnonceCreateRequest();
        createRequest.setTitle("Flow annonce");
        createRequest.setDescription("Flow description");
        createRequest.setAdress("Flow address");
        createRequest.setMail("flow@example.com");
        createRequest.setAuthorId(user.getId());
        createRequest.setCategoryId(category.getId());

        Response createResponse = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(createRequest));

        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        AnnonceResponse created = createResponse.readEntity(AnnonceResponse.class);
        assertNotNull(created.getId());

        AnnonceUpdateRequest updateRequest = new AnnonceUpdateRequest();
        updateRequest.setTitle("Flow annonce updated");
        updateRequest.setDescription("Updated description");
        updateRequest.setAdress("Updated address");
        updateRequest.setMail("flow@example.com");
        updateRequest.setCategoryId(category.getId());
        updateRequest.setStatus(Annonce.Status.PUBLISHED);

        Response updateResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(updateRequest));

        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        Response getResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        AnnonceResponse fetched = getResponse.readEntity(AnnonceResponse.class);
        assertEquals("Flow annonce updated", fetched.getTitle());

        AnnonceUpdateRequest archiveRequest = new AnnonceUpdateRequest();
        archiveRequest.setTitle("Flow annonce updated");
        archiveRequest.setDescription("Updated description");
        archiveRequest.setAdress("Updated address");
        archiveRequest.setMail("flow@example.com");
        archiveRequest.setCategoryId(category.getId());
        archiveRequest.setStatus(Annonce.Status.ARCHIVED);

        Response archiveResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(archiveRequest));

        assertEquals(Response.Status.OK.getStatusCode(), archiveResponse.getStatus());

        Response deleteResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        Response listResponse = target("annonces")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), listResponse.getStatus());
        PaginatedResponse<AnnonceResponse> listPayload = listResponse.readEntity(
                new GenericType<PaginatedResponse<AnnonceResponse>>() {
                });
        assertNotNull(listPayload.getItems());
    }
}
