package com.example.todo.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private HttpSession session;

    private AuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthFilter();
    }

    @Test
    void allowsLoginPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/todo/login");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void redirectsWhenUnauthenticated() throws Exception {
        when(request.getRequestURI()).thenReturn("/todo/home");
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/todo");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/todo/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsAuthenticatedRequests() throws Exception {
        when(request.getRequestURI()).thenReturn("/todo/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(5L);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
