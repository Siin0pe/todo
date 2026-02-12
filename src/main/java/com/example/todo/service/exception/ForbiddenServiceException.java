package com.example.todo.service.exception;

import jakarta.ws.rs.core.Response;

public class ForbiddenServiceException extends ServiceException {
    public ForbiddenServiceException(String message) {
        super(Response.Status.FORBIDDEN, message);
    }
}
