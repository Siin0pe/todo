package com.example.todo.api.security;

import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.auth.AuthSession;
import com.example.todo.auth.TokenStore;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

import java.util.Locale;

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = extractToken(requestContext);
        AuthSession session = TokenStore.getSession(token);
        if (session == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("Unauthorized"))
                    .build());
            return;
        }
        boolean secure = requestContext.getSecurityContext() != null
                && requestContext.getSecurityContext().isSecure();
        requestContext.setSecurityContext(new UserSecurityContext(session, secure));
    }

    private String extractToken(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            String trimmed = authHeader.trim();
            int spaceIdx = trimmed.indexOf(' ');
            if (spaceIdx > 0) {
                String scheme = trimmed.substring(0, spaceIdx).toLowerCase(Locale.ROOT);
                if ("bearer".equals(scheme) || "token".equals(scheme)) {
                    return trimmed.substring(spaceIdx + 1).trim();
                }
            }
            return trimmed;
        }
        return requestContext.getHeaderString("X-Auth-Token");
    }
}
