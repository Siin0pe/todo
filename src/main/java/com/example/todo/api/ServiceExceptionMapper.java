package com.example.todo.api;

import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.service.exception.ServiceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExceptionMapper.class);

    @Override
    public Response toResponse(ServiceException exception) {
        int status = exception.getStatus().getStatusCode();
        if (status >= 500) {
            LOGGER.error("service_exception status={}", status, exception);
        } else {
            LOGGER.warn("service_exception status={} message={}", status, exception.getMessage());
        }
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}
