package com.example.todo.api;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.service.UserService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterResourceUnitTest {

    @Test
    void register_returnsCreated() {
        UserService userService = mock(UserService.class);
        UserResponse created = new UserResponse();
        created.setId(3L);
        created.setUsername("alice");
        created.setEmail("alice@example.com");
        when(userService.register(org.mockito.ArgumentMatchers.any(RegisterRequest.class))).thenReturn(created);

        RegisterResource resource = new RegisterResource(userService);
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("secret");

        Response response = resource.register(request);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        UserResponse payload = (UserResponse) response.getEntity();
        assertNotNull(payload.getId());
    }
}
