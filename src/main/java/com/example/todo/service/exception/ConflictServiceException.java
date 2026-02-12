package com.example.todo.service.exception;

import jakarta.ws.rs.core.Response;

public class ConflictServiceException extends ServiceException {
    public ConflictServiceException(String message) {
        super(Response.Status.CONFLICT, message);
    }
}
