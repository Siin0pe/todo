package com.example.todo.api;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestLoggingFilterUnitTest {

    @Test
    void filter_request_usesIncomingRequestIdWhenProvided() {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(requestContext.getHeaderString("X-Request-Id")).thenReturn("req-123");
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("annonces");
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost/api/annonces"));

        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.filter(requestContext);

        verify(requestContext).setProperty(contains("requestId"), eq("req-123"));
    }

    @Test
    void filter_response_addsRequestIdHeader() {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("login");
        when(requestContext.getProperty(anyString())).thenReturn("req-456");
        when(responseContext.getStatus()).thenReturn(200);
        when(responseContext.getHeaders()).thenReturn(headers);

        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.filter(requestContext, responseContext);

        assertEquals("req-456", headers.getFirst("X-Request-Id"));
    }
}
