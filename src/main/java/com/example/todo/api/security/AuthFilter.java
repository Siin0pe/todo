package com.example.todo.api.security;

import com.example.todo.api.dto.ErrorResponse;
import com.example.todo.security.jaas.JaasConfigurationResolver;
import com.example.todo.security.jaas.SubjectContextHolder;
import com.example.todo.security.jaas.TokenCallback;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Locale;

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);
    private static final String TOKEN_DOMAIN = "MasterAnnonceToken";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        SubjectContextHolder.clear();
        String token = extractToken(requestContext);
        String path = requestPath(requestContext);
        if (token == null || token.trim().isEmpty()) {
            LOGGER.warn("auth_rejected path={} method={}", path, requestContext.getMethod());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("Unauthorized"))
                    .build());
            return;
        }

        Subject subject = authenticateToken(token.trim());
        if (subject == null) {
            LOGGER.warn("auth_rejected path={} method={}", path, requestContext.getMethod());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("Unauthorized"))
                    .build());
            return;
        }

        boolean secure = isSecureRequest(requestContext.getSecurityContext());
        JaasSubjectSecurityContext securityContext = new JaasSubjectSecurityContext(subject, secure);
        if (securityContext.getUserPrincipal() == null) {
            LOGGER.warn("auth_rejected_missing_principal path={} method={}", path, requestContext.getMethod());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("Unauthorized"))
                    .build());
            return;
        }

        requestContext.setSecurityContext(securityContext);
        SubjectContextHolder.set(subject);
        LOGGER.info("auth_accepted userId={} path={} method={}", securityContext.getUserId(), path, requestContext.getMethod());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        SubjectContextHolder.clear();
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

    private String requestPath(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo == null ? null : uriInfo.getPath();
        if (path == null || path.trim().isEmpty()) {
            return "<unknown>";
        }
        return path;
    }

    protected Subject authenticateToken(String token) {
        LoginContext loginContext = createLoginContext(token);
        try {
            loginContext.login();
            return loginContext.getSubject();
        } catch (LoginException exception) {
            return null;
        }
    }

    private LoginContext createLoginContext(String token) {
        try {
            return new LoginContext(
                    TOKEN_DOMAIN,
                    null,
                    new TokenCallbackHandler(token),
                    JaasConfigurationResolver.resolve(TOKEN_DOMAIN)
            );
        } catch (LoginException exception) {
            throw new IllegalStateException("Unable to initialize JAAS token login context", exception);
        }
    }

    private boolean isSecureRequest(SecurityContext securityContext) {
        return securityContext != null && securityContext.isSecure();
    }

    private static final class TokenCallbackHandler implements CallbackHandler {
        private final String token;

        private TokenCallbackHandler(String token) {
            this.token = token;
        }

        @Override
        public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof TokenCallback) {
                    ((TokenCallback) callback).setToken(token);
                } else if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(token);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }
}
