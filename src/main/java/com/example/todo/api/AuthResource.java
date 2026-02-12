package com.example.todo.api;

import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.LoginResponse;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.auth.AuthSession;
import com.example.todo.service.JaasAuthService;
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

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Authentification")
public class AuthResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

    private final JaasAuthService jaasAuthService;

    public AuthResource() {
        this(new JaasAuthService());
    }

    AuthResource(JaasAuthService jaasAuthService) {
        this.jaasAuthService = jaasAuthService;
    }

    @POST
    @Operation(summary = "Se connecter", description = "Retourne un token d'authentification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connexion reussie",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response login(@Valid LoginRequest request) {
        LOGGER.info("auth_login_requested");
        // JAAS authenticates credentials; token keeps client-server exchanges stateless.
        AuthSession session = jaasAuthService.login(request.getLogin(), request.getPassword());
        LOGGER.info("auth_login_succeeded userId={}", session.getUserId());
        return Response.ok(new LoginResponse(session.getToken())).build();
    }

}
