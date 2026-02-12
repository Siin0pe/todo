package com.example.todo.api;

import com.example.todo.api.dto.ErrorResponse;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {
    @Override
    public Response toResponse(NotAllowedException exception) {
        return Response.status(Response.Status.METHOD_NOT_ALLOWED)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse("Method not allowed"))
                .build();
    }
}
