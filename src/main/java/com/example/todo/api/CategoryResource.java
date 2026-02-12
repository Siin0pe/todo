package com.example.todo.api;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryPageResponse;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Path("/categories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Categories", description = "Gestion des categories")
public class CategoryResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryResource.class);

    private final CategoryService categoryService;

    public CategoryResource() {
        this(new CategoryService());
    }

    CategoryResource(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GET
    @Operation(summary = "Lister les categories", description = "Retourne une page de categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste retournee",
                    content = @Content(schema = @Schema(implementation = CategoryPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parametres invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response listCategories(@Parameter(description = "Numero de page (base 0)", in = ParameterIn.QUERY, example = "0")
                                   @DefaultValue("0") @QueryParam("page") int page,
                                   @Parameter(description = "Taille de page", in = ParameterIn.QUERY, example = "20")
                                   @DefaultValue("20") @QueryParam("size") int size) {
        LOGGER.info("category_list_requested page={} size={}", page, size);
        PaginatedResponse<CategoryResponse> response = categoryService.listCategories(page, size);
        LOGGER.info("category_list_succeeded page={} size={} returned={}", page, size, response.getItems().size());
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Recuperer une categorie", description = "Retourne le detail d'une categorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorie trouvee",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categorie introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response getCategory(@Parameter(description = "Identifiant categorie", in = ParameterIn.PATH, example = "2")
                                @PathParam("id") Long id) {
        LOGGER.info("category_get_requested categoryId={}", id);
        CategoryResponse category = categoryService.findById(id);
        LOGGER.info("category_get_succeeded categoryId={}", id);
        return Response.ok(category).build();
    }

    @POST
    @Operation(summary = "Creer une categorie", description = "Cree une nouvelle categorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categorie creee",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Categorie deja existante",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response createCategory(@Valid CategoryCreateRequest request, @Context UriInfo uriInfo) {
        LOGGER.info("category_create_requested label={}", request.getLabel());
        CategoryResponse created = categoryService.createCategory(request);
        LOGGER.info("category_create_succeeded categoryId={}", created.getId());
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.getId()))
                .build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Mettre a jour une categorie", description = "Remplace le libelle d'une categorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorie mise a jour",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categorie introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflit de libelle",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response updateCategory(@Parameter(description = "Identifiant categorie", in = ParameterIn.PATH, example = "2")
                                   @PathParam("id") Long id, @Valid CategoryUpdateRequest request) {
        LOGGER.info("category_update_requested categoryId={}", id);
        CategoryResponse updated = categoryService.updateCategory(id, request);
        LOGGER.info("category_update_succeeded categoryId={}", id);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Supprimer une categorie", description = "Supprime une categorie par identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categorie supprimee"),
            @ApiResponse(responseCode = "404", description = "Categorie introuvable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response deleteCategory(@Parameter(description = "Identifiant categorie", in = ParameterIn.PATH, example = "2")
                                   @PathParam("id") Long id) {
        LOGGER.info("category_delete_requested categoryId={}", id);
        categoryService.deleteCategory(id);
        LOGGER.info("category_delete_succeeded categoryId={}", id);
        return Response.noContent().build();
    }
}
