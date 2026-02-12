package com.example.todo.service.exception;

import jakarta.ws.rs.core.Response;

public class BadRequestServiceException extends ServiceException {
    public BadRequestServiceException(String message) {
        super(Response.Status.BAD_REQUEST, message);
    }
}
