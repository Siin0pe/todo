package com.example.todo.service.exception;

import jakarta.ws.rs.core.Response;

public class NotFoundServiceException extends ServiceException {
    public NotFoundServiceException(String message) {
        super(Response.Status.NOT_FOUND, message);
    }
}
