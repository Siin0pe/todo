package com.example.todo.controller;

import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.LoginResponse;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.api.mapper.AuthMapper;
import com.example.todo.auth.AuthSession;
import com.example.todo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification")
public class AuthController {
    private final AuthService authService;
    private final AuthMapper authMapper;

    public AuthController(AuthService authService, AuthMapper authMapper) {
        this.authService = authService;
        this.authMapper = authMapper;
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter", description = "Authentifie un utilisateur et renvoie un token JWT.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Identifiants de connexion",
            content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "loginBody",
                            summary = "Connexion standard",
                            value = "{\"username\":\"user\",\"password\":\"secret123\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion reussie",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"username: must not be blank\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Identifiants invalides",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Invalid credentials\"}")
                    )
            )
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = authService.login(request.getUsername(), request.getPassword());
        return authMapper.toLoginResponse(session);
    }
}
