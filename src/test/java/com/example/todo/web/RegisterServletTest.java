package com.example.todo.web;

import com.example.todo.model.User;
import com.example.todo.service.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher dispatcher;
    @Mock
    private UserService userService;

    private RegisterServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TestRegisterServlet(userService);
    }

    @Test
    void doPostRejectsInvalidPayload() throws Exception {
        when(request.getParameter("username")).thenReturn("");
        when(request.getParameter("email")).thenReturn("invalid");
        when(request.getParameter("password")).thenReturn("123");
        when(request.getParameter("confirm")).thenReturn("321");
        when(request.getRequestDispatcher("/WebContent/Register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(request).setAttribute(eq("errors"), captor.capture());
        Map<String, String> errors = captor.getValue();
        assertTrue(errors.containsKey("username"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("confirm"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostShowsErrorWhenUserExists() throws Exception {
        when(request.getParameter("username")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("alice@example.com");
        when(request.getParameter("password")).thenReturn("secret1");
        when(request.getParameter("confirm")).thenReturn("secret1");
        when(request.getRequestDispatcher("/WebContent/Register.jsp")).thenReturn(dispatcher);
        when(userService.registerUser("alice", "alice@example.com", "secret1")).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).setAttribute("error", "Username ou email deja utilise.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void doPostRedirectsOnSuccess() throws Exception {
        when(request.getParameter("username")).thenReturn("bob");
        when(request.getParameter("email")).thenReturn("bob@example.com");
        when(request.getParameter("password")).thenReturn("secret1");
        when(request.getParameter("confirm")).thenReturn("secret1");
        when(request.getContextPath()).thenReturn("/todo");
        when(userService.registerUser("bob", "bob@example.com", "secret1")).thenReturn(new User());

        servlet.doPost(request, response);

        verify(response).sendRedirect("/todo/login?success=Compte cree.");
    }

    private static class TestRegisterServlet extends RegisterServlet {
        private final UserService userService;

        private TestRegisterServlet(UserService userService) {
            this.userService = userService;
        }

        @Override
        protected UserService userService() {
            return userService;
        }
    }
}
