package com.example.todo.web;

import com.example.todo.model.User;
import com.example.todo.service.AuthService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher dispatcher;
    @Mock
    private AuthService authService;
    @Mock
    private HttpSession session;

    private LoginServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TestLoginServlet(authService);
    }

    @Test
    void doGetForwardsToLoginWithSuccessMessage() throws Exception {
        when(request.getParameter("success")).thenReturn("Ok");
        when(request.getRequestDispatcher("/WebContent/Login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("success", "Ok");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostRejectsMissingCredentials() throws Exception {
        when(request.getParameter("login")).thenReturn(" ");
        when(request.getParameter("password")).thenReturn("");
        when(request.getRequestDispatcher("/WebContent/Login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("error", "Identifiants obligatoires.");
        verify(request).setAttribute("formLogin", " ");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostCreatesSessionAndRedirectsOnSuccess() throws Exception {
        when(request.getParameter("login")).thenReturn("alice");
        when(request.getParameter("password")).thenReturn("secret");
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/todo");

        User user = new User();
        user.setId(5L);
        user.setUsername("alice");
        when(authService.authenticate("alice", "secret")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("userId", 5L);
        verify(session).setAttribute("username", "alice");
        verify(response).sendRedirect("/todo/home");
    }

    @Test
    void doPostShowsErrorOnInvalidCredentials() throws Exception {
        when(request.getParameter("login")).thenReturn("alice");
        when(request.getParameter("password")).thenReturn("bad");
        when(authService.authenticate("alice", "bad")).thenReturn(null);
        when(request.getRequestDispatcher("/WebContent/Login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("error", "Login ou mot de passe incorrect.");
        verify(request).setAttribute("formLogin", "alice");
        verify(dispatcher).forward(request, response);
    }

    private static class TestLoginServlet extends LoginServlet {
        private final AuthService authService;

        private TestLoginServlet(AuthService authService) {
            this.authService = authService;
        }

        @Override
        protected AuthService authService() {
            return authService;
        }
    }
}
