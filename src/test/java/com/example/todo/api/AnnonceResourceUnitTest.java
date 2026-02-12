package com.example.todo.api;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.security.JaasSubjectSecurityContext;
import com.example.todo.security.jaas.UserPrincipal;
import com.example.todo.service.AnnonceService;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnonceResourceUnitTest {

    @Test
    void listAnnonces_returnsOk() {
        AnnonceService service = mock(AnnonceService.class);
        PaginatedResponse<AnnonceResponse> payload = new PaginatedResponse<>(0, 2, java.util.Collections.emptyList());
        when(service.listAnnonces(0, 2)).thenReturn(payload);

        AnnonceResource resource = new AnnonceResource(service);
        Response response = resource.listAnnonces(0, 2);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(payload, response.getEntity());
    }

    @Test
    void createAnnonce_returnsCreated() {
        AnnonceService service = mock(AnnonceService.class);
        AnnonceResponse created = new AnnonceResponse();
        created.setId(12L);
        created.setTitle("Title");
        when(service.createAnnonce(org.mockito.ArgumentMatchers.any(AnnonceCreateRequest.class), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(created);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/api/annonces"));

        Subject subject = new Subject();
        subject.getPrincipals().add(new UserPrincipal("alice", 7L));
        SecurityContext securityContext = new JaasSubjectSecurityContext(subject, false);

        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setTitle("Title");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");
        request.setAuthorId(7L);
        request.setCategoryId(3L);

        AnnonceResource resource = new AnnonceResource(service);
        Response response = resource.createAnnonce(request, uriInfo, securityContext);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(created, response.getEntity());
    }
}
