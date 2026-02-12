package com.example.todo.api;

import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.LoginResponse;
import com.example.todo.auth.AuthSession;
import com.example.todo.service.JaasAuthService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthResourceUnitTest {

    @Test
    void login_returnsToken() {
        JaasAuthService jaasAuthService = mock(JaasAuthService.class);
        AuthSession session = new AuthSession("token", 1L, "alice", Instant.now());
        when(jaasAuthService.login("alice", "secret")).thenReturn(session);

        AuthResource resource = new AuthResource(jaasAuthService);
        LoginRequest request = new LoginRequest();
        request.setLogin("alice");
        request.setPassword("secret");

        Response response = resource.login(request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        LoginResponse payload = (LoginResponse) response.getEntity();
        assertNotNull(payload.getToken());
    }
}
