package com.example.todo.api.security;

import com.example.todo.api.dto.ErrorResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.security.auth.Subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFilterUnitTest {

    @Test
    void filter_rejectsMissingToken() {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Authorization")).thenReturn(null);
        when(context.getHeaderString("X-Auth-Token")).thenReturn(null);

        AuthFilter filter = new AuthFilter();
        filter.filter(context);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(context).abortWith(captor.capture());
        Response response = captor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        ErrorResponse payload = (ErrorResponse) response.getEntity();
        assertNotNull(payload.getMessage());
    }

    @Test
    void filter_setsSecurityContextForValidToken() {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Authorization")).thenReturn("Bearer valid-token");

        Subject subject = new Subject();
        subject.getPrincipals().add(new com.example.todo.security.jaas.UserPrincipal("alice", 1L));
        subject.getPrincipals().add(new com.example.todo.security.jaas.RolePrincipal("ROLE_USER"));
        AuthFilter filter = new StubAuthFilter(subject);
        filter.filter(context);

        verify(context).setSecurityContext(org.mockito.ArgumentMatchers.any(JaasSubjectSecurityContext.class));
    }

    @Test
    void filter_rejectsInvalidToken() {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Authorization")).thenReturn("Bearer invalid-token");

        AuthFilter filter = new StubAuthFilter(null);
        filter.filter(context);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(context).abortWith(captor.capture());
        Response response = captor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    private static final class StubAuthFilter extends AuthFilter {
        private final Subject subject;

        private StubAuthFilter(Subject subject) {
            this.subject = subject;
        }

        @Override
        protected Subject authenticateToken(String token) {
            return subject;
        }
    }
}
