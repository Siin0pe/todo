package com.example.todo.api;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.service.UserService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegisterResource {
    private final UserService userService = new UserService();

    @POST
    public Response register(@Valid RegisterRequest request) {
        UserResponse created = userService.register(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
