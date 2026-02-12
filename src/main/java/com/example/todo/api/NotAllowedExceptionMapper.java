package com.example.todo.api;

import com.example.todo.api.dto.ErrorResponse;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotAllowedExceptionMapper.class);

    @Override
    public Response toResponse(NotAllowedException exception) {
        LOGGER.warn("method_not_allowed");
        return Response.status(Response.Status.METHOD_NOT_ALLOWED)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse("Method not allowed"))
                .build();
    }
}
