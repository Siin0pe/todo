package com.example.todo.api;

import com.example.todo.api.annotations.PATCH;
import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.security.AuthUtil;
import com.example.todo.api.security.Secured;
import com.example.todo.service.AnnonceService;
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

import java.net.URI;

@Path("/annonces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnnonceResource {
    private final AnnonceService annonceService = new AnnonceService();

    @GET
    public Response listAnnonces(@DefaultValue("0") @QueryParam("page") int page,
                                 @DefaultValue("20") @QueryParam("size") int size) {
        PaginatedResponse<AnnonceResponse> response = annonceService.listAnnonces(page, size);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getAnnonce(@PathParam("id") Long id) {
        AnnonceResponse annonce = annonceService.findById(id);
        return Response.ok(annonce).build();
    }

    @POST
    @Secured
    public Response createAnnonce(@Valid AnnonceCreateRequest request,
                                  @Context UriInfo uriInfo,
                                  @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        AnnonceResponse created = annonceService.createAnnonce(request, currentUserId);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.getId()))
                .build();
        return Response.created(location).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Secured
    public Response updateAnnonce(@PathParam("id") Long id,
                                  @Valid AnnonceUpdateRequest request,
                                  @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        AnnonceResponse updated = annonceService.updateAnnonce(id, request, currentUserId);
        return Response.ok(updated).build();
    }

    @PATCH
    @Path("/{id}")
    @Secured
    public Response patchAnnonce(@PathParam("id") Long id,
                                 @Valid AnnoncePatchRequest request,
                                 @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        AnnonceResponse updated = annonceService.patchAnnonce(id, request, currentUserId);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured
    public Response deleteAnnonce(@PathParam("id") Long id,
                                  @Context SecurityContext securityContext) {
        Long currentUserId = AuthUtil.requireUserId(securityContext);
        annonceService.deleteAnnonce(id, currentUserId);
        return Response.noContent().build();
    }
}
