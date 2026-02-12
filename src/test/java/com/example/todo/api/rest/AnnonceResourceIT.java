package com.example.todo.api.rest;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.dto.ErrorResponse;
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

class AnnonceResourceIT extends RestIntegrationTestBase {

    @Test
    void listAnnonces_returnsPaginatedResponse() {
        Response response = target("annonces")
                .queryParam("page", 0)
                .queryParam("size", 4)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        PaginatedResponse<AnnonceResponse> payload = response.readEntity(
                new GenericType<PaginatedResponse<AnnonceResponse>>() {
                });

        assertEquals(0, payload.getPage());
        assertEquals(4, payload.getSize());
        assertEquals(4, payload.getCount());
        assertEquals("Annonce 30", payload.getItems().get(0).getTitle());
    }

    @Test
    void createAnnonce_requiresAuth() {
        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setTitle("Test");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");
        request.setAuthorId(1L);
        request.setCategoryId(1L);

        Response response = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void createAnnonce_validationError_returns400() {
        UserResponse user = registerUser("carol", "carol@example.com", "secret123");
        String token = loginToken("carol", "secret123");
        Long categoryId = findCategoryId("Category A");

        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setTitle("");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");
        request.setAuthorId(user.getId());
        request.setCategoryId(categoryId);

        Response response = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void createAnnonce_returnsCreated() {
        UserResponse user = registerUser("david", "david@example.com", "secret123");
        String token = loginToken("david", "secret123");
        Long categoryId = findCategoryId("Category B");

        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setTitle("New annonce");
        request.setDescription("Some description");
        request.setAdress("Some address");
        request.setMail("david@example.com");
        request.setAuthorId(user.getId());
        request.setCategoryId(categoryId);

        Response response = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(request));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        AnnonceResponse created = response.readEntity(AnnonceResponse.class);
        assertNotNull(created.getId());
        assertEquals("New annonce", created.getTitle());
        assertEquals("DRAFT", created.getStatus());
    }

    @Test
    void getAnnonce_notFound_returns404() {
        Response response = target("annonces/999999")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void updateAnnonce_forbiddenWhenNotAuthor() {
        UserResponse owner = registerUser("owner", "owner@example.com", "secret123");
        String ownerToken = loginToken("owner", "secret123");
        Long categoryId = findCategoryId("Category A");

        AnnonceCreateRequest createRequest = new AnnonceCreateRequest();
        createRequest.setTitle("Owned annonce");
        createRequest.setDescription("Desc");
        createRequest.setAdress("Addr");
        createRequest.setMail("owner@example.com");
        createRequest.setAuthorId(owner.getId());
        createRequest.setCategoryId(categoryId);

        Response createResponse = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .post(Entity.json(createRequest));

        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        AnnonceResponse created = createResponse.readEntity(AnnonceResponse.class);

        registerUser("other", "other@example.com", "secret123");
        String otherToken = loginToken("other", "secret123");

        AnnonceUpdateRequest updateRequest = new AnnonceUpdateRequest();
        updateRequest.setTitle("Updated");
        updateRequest.setDescription("Desc");
        updateRequest.setAdress("Addr");
        updateRequest.setMail("owner@example.com");
        updateRequest.setCategoryId(categoryId);

        Response updateResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .put(Entity.json(updateRequest));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), updateResponse.getStatus());
    }

    @Test
    void updateAnnonce_requiresAuth() {
        UserResponse user = registerUser("auth", "auth@example.com", "secret123");
        String token = loginToken("auth", "secret123");
        Long categoryId = findCategoryId("Category A");

        AnnonceCreateRequest createRequest = new AnnonceCreateRequest();
        createRequest.setTitle("Auth annonce");
        createRequest.setDescription("Desc");
        createRequest.setAdress("Addr");
        createRequest.setMail("auth@example.com");
        createRequest.setAuthorId(user.getId());
        createRequest.setCategoryId(categoryId);

        Response createResponse = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(createRequest));

        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        AnnonceResponse created = createResponse.readEntity(AnnonceResponse.class);

        AnnonceUpdateRequest updateRequest = new AnnonceUpdateRequest();
        updateRequest.setTitle("Updated");
        updateRequest.setDescription("Desc");
        updateRequest.setAdress("Addr");
        updateRequest.setMail("auth@example.com");
        updateRequest.setCategoryId(categoryId);

        Response updateResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), updateResponse.getStatus());
        ErrorResponse error = updateResponse.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void deleteAnnonce_requiresArchivedStatus() {
        UserResponse user = registerUser("ed", "ed@example.com", "secret123");
        String token = loginToken("ed", "secret123");
        Long categoryId = findCategoryId("Category A");

        AnnonceCreateRequest createRequest = new AnnonceCreateRequest();
        createRequest.setTitle("Delete check");
        createRequest.setDescription("Desc");
        createRequest.setAdress("Addr");
        createRequest.setMail("ed@example.com");
        createRequest.setAuthorId(user.getId());
        createRequest.setCategoryId(categoryId);

        Response createResponse = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(createRequest));

        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        AnnonceResponse created = createResponse.readEntity(AnnonceResponse.class);

        Response deleteResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(Response.Status.CONFLICT.getStatusCode(), deleteResponse.getStatus());
    }

    @Test
    void publishAnnonce_thenRejectFieldUpdate_thenArchiveAndDelete() {
        UserResponse user = registerUser("eva", "eva@example.com", "secret123");
        String token = loginToken("eva", "secret123");
        Long categoryId = findCategoryId("Category B");

        AnnonceCreateRequest createRequest = new AnnonceCreateRequest();
        createRequest.setTitle("Flow annonce");
        createRequest.setDescription("Desc");
        createRequest.setAdress("Addr");
        createRequest.setMail("eva@example.com");
        createRequest.setAuthorId(user.getId());
        createRequest.setCategoryId(categoryId);

        Response createResponse = target("annonces")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(createRequest));
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        AnnonceResponse created = createResponse.readEntity(AnnonceResponse.class);

        AnnonceUpdateRequest publishRequest = new AnnonceUpdateRequest();
        publishRequest.setTitle("Flow annonce");
        publishRequest.setDescription("Desc");
        publishRequest.setAdress("Addr");
        publishRequest.setMail("eva@example.com");
        publishRequest.setCategoryId(categoryId);
        publishRequest.setStatus(Annonce.Status.PUBLISHED);

        Response publishResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(publishRequest));

        assertEquals(Response.Status.OK.getStatusCode(), publishResponse.getStatus());

        AnnonceUpdateRequest invalidUpdate = new AnnonceUpdateRequest();
        invalidUpdate.setTitle("Should fail");
        invalidUpdate.setDescription("Desc");
        invalidUpdate.setAdress("Addr");
        invalidUpdate.setMail("eva@example.com");
        invalidUpdate.setCategoryId(categoryId);
        invalidUpdate.setStatus(Annonce.Status.PUBLISHED);

        Response invalidUpdateResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(invalidUpdate));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), invalidUpdateResponse.getStatus());

        AnnonceUpdateRequest archiveUpdate = new AnnonceUpdateRequest();
        archiveUpdate.setTitle("Flow annonce");
        archiveUpdate.setDescription("Desc");
        archiveUpdate.setAdress("Addr");
        archiveUpdate.setMail("eva@example.com");
        archiveUpdate.setCategoryId(categoryId);
        archiveUpdate.setStatus(Annonce.Status.ARCHIVED);

        Response archiveUpdateResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .put(Entity.json(archiveUpdate));

        assertEquals(Response.Status.OK.getStatusCode(), archiveUpdateResponse.getStatus());

        Response deleteResponse = target("annonces/" + created.getId())
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .delete();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }
}
