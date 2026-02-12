package com.example.todo.api.rest;

import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.LoginResponse;
import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthResourceIT extends RestIntegrationTestBase {

    @Test
    void register_createsUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("secret123");

        Response response = target("register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        UserResponse created = response.readEntity(UserResponse.class);
        assertNotNull(created.getId());
        assertEquals("newuser", created.getUsername());
        assertEquals("newuser@example.com", created.getEmail());
    }

    @Test
    void register_validationError_returns400() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setEmail("invalid");
        request.setPassword("123");

        Response response = target("register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void login_returnsToken() {
        registerUser("alice2", "alice2@example.com", "secret123");
        LoginRequest request = new LoginRequest();
        request.setLogin("alice2");
        request.setPassword("secret123");

        Response response = target("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        LoginResponse login = response.readEntity(LoginResponse.class);
        assertNotNull(login.getToken());
    }

    @Test
    void login_invalidCredentials_returns401() {
        registerUser("bob2", "bob2@example.com", "secret123");
        LoginRequest request = new LoginRequest();
        request.setLogin("bob2");
        request.setPassword("wrong");

        Response response = target("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void register_duplicate_returns409() {
        registerUser("dup", "dup@example.com", "secret123");
        RegisterRequest request = new RegisterRequest();
        request.setUsername("dup");
        request.setEmail("dup@example.com");
        request.setPassword("secret123");

        Response response = target("register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void login_validationError_returns400() {
        LoginRequest request = new LoginRequest();
        request.setLogin("");
        request.setPassword("");

        Response response = target("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorResponse error = response.readEntity(ErrorResponse.class);
        assertNotNull(error.getMessage());
    }
}
