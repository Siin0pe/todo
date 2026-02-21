package com.example.todo.controller;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Gestion des categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(
            summary = "Lister les categories",
            description = "Pagination simple des categories. Exemple: /api/categories?page=0&size=20"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste retournee",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
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
    public PaginatedResponse<CategoryResponse> listCategories(
            @Parameter(description = "Index de page (>= 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page (>= 1)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return categoryService.listCategories(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une categorie")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorie trouvee",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
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
    public CategoryResponse getCategory(@PathVariable("id") Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Creer une categorie")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Donnees de creation de categorie",
            content = @Content(
                    schema = @Schema(implementation = CategoryCreateRequest.class),
                    examples = @ExampleObject(
                            name = "createCategoryBody",
                            value = "{\"label\":\"Immobilier\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Categorie creee",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"label: must not be blank\"}")
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
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse created = categoryService.createCategory(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour une categorie")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Donnees de mise a jour",
            content = @Content(
                    schema = @Schema(implementation = CategoryUpdateRequest.class),
                    examples = @ExampleObject(
                            name = "updateCategoryBody",
                            value = "{\"label\":\"Services\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorie mise a jour",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload invalide",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"label: must not be blank\"}")
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
    public CategoryResponse updateCategory(@PathVariable("id") Long id,
                                           @Valid @RequestBody CategoryUpdateRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une categorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categorie supprimee"),
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
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
