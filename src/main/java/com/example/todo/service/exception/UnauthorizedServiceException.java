package com.example.todo.service.exception;

import jakarta.ws.rs.core.Response;

public class UnauthorizedServiceException extends ServiceException {
    public UnauthorizedServiceException(String message) {
        super(Response.Status.UNAUTHORIZED, message);
    }
}
