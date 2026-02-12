package com.example.todo.api;

import com.example.todo.api.security.AuthUtil;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@Provider
@Priority(Priorities.USER)
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String START_TIME_PROPERTY = RequestLoggingFilter.class.getName() + ".startNanos";
    private static final String REQUEST_ID_PROPERTY = RequestLoggingFilter.class.getName() + ".requestId";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestId = resolveRequestId(requestContext);
        requestContext.setProperty(START_TIME_PROPERTY, System.nanoTime());
        requestContext.setProperty(REQUEST_ID_PROPERTY, requestId);

        MDC.put("requestId", requestId);
        MDC.put("httpMethod", requestContext.getMethod());
        MDC.put("path", requestContext.getUriInfo().getPath());

        String query = requestContext.getUriInfo().getRequestUri().getRawQuery();
        if (query != null && !query.isEmpty()) {
            MDC.put("query", query);
        }

        String clientIp = extractClientIp(requestContext);
        if (clientIp != null) {
            MDC.put("clientIp", clientIp);
        }

        LOGGER.info("request_started");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String requestId = (String) requestContext.getProperty(REQUEST_ID_PROPERTY);
        if (requestId != null && !requestId.isEmpty()) {
            MDC.put("requestId", requestId);
            responseContext.getHeaders().putSingle(REQUEST_ID_HEADER, requestId);
        }

        MDC.put("httpMethod", requestContext.getMethod());
        MDC.put("path", requestContext.getUriInfo().getPath());
        MDC.put("status", String.valueOf(responseContext.getStatus()));

        Long userId = resolveUserId(requestContext);
        if (userId != null) {
            MDC.put("userId", String.valueOf(userId));
        }

        Long durationMs = resolveDurationMs(requestContext);
        if (durationMs != null) {
            MDC.put("durationMs", String.valueOf(durationMs));
        }

        if (responseContext.getStatus() >= 500) {
            LOGGER.error("request_completed_with_server_error");
        } else {
            LOGGER.info("request_completed");
        }

        clearMdc();
    }

    private String resolveRequestId(ContainerRequestContext requestContext) {
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return requestId.trim();
    }

    private String extractClientIp(ContainerRequestContext requestContext) {
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.trim().isEmpty()) {
            return null;
        }
        int commaIndex = forwardedFor.indexOf(',');
        String firstIp = commaIndex >= 0 ? forwardedFor.substring(0, commaIndex) : forwardedFor;
        String trimmed = firstIp.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long resolveDurationMs(ContainerRequestContext requestContext) {
        Object start = requestContext.getProperty(START_TIME_PROPERTY);
        if (!(start instanceof Long)) {
            return null;
        }
        long durationNanos = System.nanoTime() - (Long) start;
        if (durationNanos < 0) {
            return 0L;
        }
        return durationNanos / 1_000_000;
    }

    private Long resolveUserId(ContainerRequestContext requestContext) {
        return AuthUtil.requireUserId(requestContext.getSecurityContext());
    }

    private void clearMdc() {
        MDC.remove("requestId");
        MDC.remove("httpMethod");
        MDC.remove("path");
        MDC.remove("query");
        MDC.remove("clientIp");
        MDC.remove("status");
        MDC.remove("userId");
        MDC.remove("durationMs");
    }
}
