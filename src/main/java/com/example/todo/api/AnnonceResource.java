package com.example.todo.api;

import com.example.todo.api.annotations.PATCH;
import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnoncePageResponse;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.security.AuthUtil;
import com.example.todo.api.security.Secured;
import com.example.todo.service.AnnonceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Path("/annonces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Annonces", description = "Gestion des annonces")
public class AnnonceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnonceResource.class);

    private final AnnonceService annonceService;

    public AnnonceResource() {
        this(new AnnonceService());
    }

    AnnonceResource(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @GET
    @Operation(summary = "Lister les annonces", description = "Retourne une page d'annonces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste retournee",
                    content = @Content(schema = @Schema(implementation = AnnoncePageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parametres invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response listAnnonces(@Parameter(description = "Numero de page (base 0)", in = ParameterIn.QUERY, example = "0")
                                 @DefaultValue("0") @QueryParam("page") int page,
                                 @Parameter(description = "Taille de page", in = ParameterIn.QUERY, example = "20")
                                 @DefaultValue("20") @QueryParam("size") int size) {
        LOGGER.info("annonce_list_requested page={} size={}", page, size);
        PaginatedResponse<AnnonceResponse> response = annonceService.listAnnonces(page, size);
        LOGGER.info("annonce_list_succeeded page={} size={} returned={}", page, size, response.getItems().size());
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Recuperer une annonce", description = "Retourne le detail d'une annonce par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce trouvee",
                    content = @Content(schema = @Schema(implementation = AnnonceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getAnnonce(@Parameter(description = "Identifiant annonce", in = ParameterIn.PATH, example = "10")
                               @PathParam("id") Long id) {
        LOGGER.info("annonce_get_requested annonceId={}", id);
        AnnonceResponse annonce = annonceService.findById(id);
        LOGGER.info("annonce_get_succeeded annonceId={}", id);
        return Response.ok(annonce).build();
    }

    @POST
    @Secured
    @Operation(summary = "Creer une annonce", description = "Creer une annonce pour l'utilisateur authentifie")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Annonce creee",
                    content = @Content(schema = @Schema(implementation = AnnonceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifie",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categorie ou utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createAnnonce(@Valid AnnonceCreateRequest request,
                                  @Context UriInfo uriInfo,
                                  @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        LOGGER.info("annonce_create_requested currentUserId={}", currentUserId);
        AnnonceResponse created = annonceService.createAnnonce(request, currentUserId);
        LOGGER.info("annonce_create_succeeded annonceId={} authorId={}", created.getId(), currentUserId);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.getId()))
                .build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Secured
    @Operation(summary = "Mettre a jour une annonce", description = "Remplace completement une annonce")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce mise a jour",
                    content = @Content(schema = @Schema(implementation = AnnonceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifie",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Action interdite",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Annonce ou categorie introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateAnnonce(@Parameter(description = "Identifiant annonce", in = ParameterIn.PATH, example = "10")
                                  @PathParam("id") Long id,
                                  @Valid AnnonceUpdateRequest request,
                                  @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        LOGGER.info("annonce_update_requested annonceId={} currentUserId={}", id, currentUserId);
        AnnonceResponse updated = annonceService.updateAnnonce(id, request, currentUserId);
        LOGGER.info("annonce_update_succeeded annonceId={} currentUserId={}", id, currentUserId);
        return Response.ok(updated).build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
    @Operation(summary = "Modifier partiellement une annonce", description = "Met a jour uniquement les champs fournis")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annonce mise a jour",
                    content = @Content(schema = @Schema(implementation = AnnonceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifie",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Action interdite",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Annonce ou categorie introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response patchAnnonce(@Parameter(description = "Identifiant annonce", in = ParameterIn.PATH, example = "10")
                                 @PathParam("id") Long id,
                                 @Valid AnnoncePatchRequest request,
                                 @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        LOGGER.info("annonce_patch_requested annonceId={} currentUserId={}", id, currentUserId);
        AnnonceResponse updated = annonceService.patchAnnonce(id, request, currentUserId);
        LOGGER.info("annonce_patch_succeeded annonceId={} currentUserId={}", id, currentUserId);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured
    @Operation(summary = "Supprimer une annonce", description = "Supprime une annonce par identifiant")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Annonce supprimee"),
            @ApiResponse(responseCode = "401", description = "Non authentifie",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Action interdite",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Annonce introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteAnnonce(@Parameter(description = "Identifiant annonce", in = ParameterIn.PATH, example = "10")
                                  @PathParam("id") Long id,
                                  @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        LOGGER.info("annonce_delete_requested annonceId={} currentUserId={}", id, currentUserId);
        annonceService.deleteAnnonce(id, currentUserId);
        LOGGER.info("annonce_delete_succeeded annonceId={} currentUserId={}", id, currentUserId);
        return Response.noContent().build();
    }
}
