package com.example.todo.controller;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
@Tag(name = "Auth", description = "Authentification")
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "S'inscrire", description = "Cree un nouveau compte utilisateur.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Donnees d'inscription",
            content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                            name = "registerBody",
                            summary = "Inscription standard",
                            value = "{\"username\":\"john.doe\",\"email\":\"john.doe@example.com\",\"password\":\"secret123\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Utilisateur cree",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"email: must be a well-formed email address\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Nom d'utilisateur ou email deja utilise",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"User already exists\"}")
                    )
            )
    })
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }
}
