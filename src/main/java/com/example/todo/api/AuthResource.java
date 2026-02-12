package com.example.todo.api;

import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.LoginResponse;
import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.auth.AuthSession;
import com.example.todo.service.AuthService;
import com.example.todo.service.UserService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    @POST
    public Response login(@Valid LoginRequest request) {
        AuthSession session = authService.login(request.getLogin(), request.getPassword());
        return Response.ok(new LoginResponse(session.getToken())).build();
    }

}
