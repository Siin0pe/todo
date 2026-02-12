package com.example.todo.api;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/register")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Authentification")
public class RegisterResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterResource.class);

    private final UserService userService;

    public RegisterResource() {
        this(new UserService());
    }

    RegisterResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Operation(summary = "S'inscrire", description = "Cree un nouvel utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur cree",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email ou username deja utilise",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response register(@Valid RegisterRequest request) {
        LOGGER.info("user_register_requested");
        UserResponse created = userService.register(request);
        LOGGER.info("user_register_succeeded userId={}", created.getId());
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
