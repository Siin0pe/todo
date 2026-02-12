package com.example.todo.api;

import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServiceExceptionMapperUnitTest {

    @Test
    void mapsServiceExceptionToResponse() {
        ServiceExceptionMapper mapper = new ServiceExceptionMapper();
        UnauthorizedServiceException ex = new UnauthorizedServiceException("Nope");

        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErrorResponse payload = (ErrorResponse) response.getEntity();
        assertNotNull(payload.getMessage());
        assertEquals("Nope", payload.getMessage());
    }
}
