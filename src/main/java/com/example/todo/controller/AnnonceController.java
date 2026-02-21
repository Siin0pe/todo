package com.example.todo.controller;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceDTO;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.model.Annonce;
import com.example.todo.security.spring.CurrentUserService;
import com.example.todo.service.AnnonceSearchCriteria;
import com.example.todo.service.AnnonceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Sort;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/annonces")
@Tag(name = "Annonces", description = "Gestion des annonces")
public class AnnonceController {
    private final AnnonceService annonceService;
    private final CurrentUserService currentUserService;

    public AnnonceController(AnnonceService annonceService, CurrentUserService currentUserService) {
        this.annonceService = annonceService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les annonces",
            description = "Recherche multi-criteres. Exemple: /api/annonces?q=appartement&status=PUBLISHED&page=0&size=10"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste retournee",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtres invalides",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"fromDate must be before or equal to toDate\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifie",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Unauthorized\"}")
                    )
            )
    })
    public Page<AnnonceDTO> listAnnonces(
            @Parameter(description = "Texte libre recherche titre/description", example = "appartement")
            @RequestParam(required = false) String q,
            @Parameter(description = "Statut annonce", example = "PUBLISHED")
            @RequestParam(required = false) Annonce.Status status,
            @Parameter(description = "Identifiant categorie", example = "2")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Identifiant auteur", example = "1")
            @RequestParam(required = false) Long authorId,
            @Parameter(description = "Date minimale ISO-8601", example = "2026-02-01T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @Parameter(description = "Date maximale ISO-8601", example = "2026-02-28T23:59:59Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AnnonceSearchCriteria criteria = new AnnonceSearchCriteria(q, status, categoryId, authorId, fromDate, toDate);
        return annonceService.searchAnnonces(criteria, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une annonce")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Annonce trouvee",
                    content = @Content(schema = @Schema(implementation = AnnonceDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifie",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Unauthorized\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Annonce introuvable",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Annonce not found\"}")
                    )
            )
    })
    public AnnonceDTO getAnnonce(@PathVariable("id") Long id) {
        return annonceService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer une annonce")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Donnees de creation d'annonce",
            content = @Content(
                    schema = @Schema(implementation = AnnonceCreateRequest.class),
                    examples = @ExampleObject(
                            name = "createAnnonceBody",
                            value = "{\"title\":\"Appartement T2 centre ville\",\"description\":\"Bel appartement proche des commerces\",\"adress\":\"10 rue des Fleurs, Paris\",\"mail\":\"contact@example.com\",\"authorId\":1,\"categoryId\":2}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Annonce creee",
                    content = @Content(schema = @Schema(implementation = AnnonceDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide ou auteur incoherent",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Author does not match authenticated user\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifie",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Unauthorized\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categorie introuvable",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Category not found\"}")
                    )
            )
    })
    public ResponseEntity<AnnonceDTO> createAnnonce(@Valid @RequestBody AnnonceCreateRequest request) {
        Long currentUserId = currentUserService.requireUserId();
        AnnonceDTO created = annonceService.createAnnonce(request, currentUserId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour une annonce")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Donnees de mise a jour complete",
            content = @Content(
                    schema = @Schema(implementation = AnnonceUpdateRequest.class),
                    examples = @ExampleObject(
                            name = "updateAnnonceBody",
                            value = "{\"title\":\"Maison avec jardin\",\"description\":\"Maison 4 pieces, quartier calme\",\"adress\":\"5 avenue de la Republique, Lille\",\"mail\":\"owner@example.com\",\"categoryId\":2,\"status\":\"PUBLISHED\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Annonce mise a jour",
                    content = @Content(schema = @Schema(implementation = AnnonceDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"mail: must be a well-formed email address\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifie",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Unauthorized\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Action non autorisee",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Only the author can modify or delete this annonce\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Annonce ou categorie introuvable",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Annonce not found\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflit metier",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Published annonces cannot be modified\"}")
                    )
            )
    })
    public AnnonceDTO updateAnnonce(@PathVariable("id") Long id, @Valid @RequestBody AnnonceUpdateRequest request) {
        Long currentUserId = currentUserService.requireUserId();
        return annonceService.updateAnnonce(id, request, currentUserId);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Modifier partiellement une annonce",
            description = "Met a jour uniquement les champs renseignes; les champs absents restent inchanges."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Donnees de mise a jour partielle",
            content = @Content(
                    schema = @Schema(implementation = AnnoncePatchRequest.class),
                    examples = @ExampleObject(
                            name = "patchAnnonceBody",
                            value = "{\"status\":\"ARCHIVED\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Annonce mise a jour",
                    content = @Content(schema = @Schema(implementation = AnnonceDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"mail: must be a well-formed email address\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifie",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Unauthorized\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Action non autorisee",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Only an admin can archive an annonce\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Annonce ou categorie introuvable",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Annonce not found\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflit metier",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Archiving cannot change annonce fields\"}")
                    )
            )
    })
    public AnnonceDTO patchAnnonce(@PathVariable("id") Long id, @Valid @RequestBody AnnoncePatchRequest request) {
        Long currentUserId = currentUserService.requireUserId();
        return annonceService.patchAnnonce(id, request, currentUserId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une annonce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Annonce supprimee"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Utilisateur non authentifie",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Unauthorized\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Action non autorisee",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Only the author can modify or delete this annonce\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Annonce introuvable",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Annonce not found\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Suppression impossible si annonce non archivee",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"Annonce must be archived before deletion\"}")
                    )
            )
    })
    public ResponseEntity<Void> deleteAnnonce(@PathVariable("id") Long id) {
        Long currentUserId = currentUserService.requireUserId();
        annonceService.deleteAnnonce(id, currentUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
