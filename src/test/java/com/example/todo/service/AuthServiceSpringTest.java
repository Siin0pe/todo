package com.example.todo.service;

import com.example.todo.auth.AuthSession;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.security.spring.JwtTokenService;
import com.example.todo.security.spring.UserRoleService;
import com.example.todo.service.exception.UnauthorizedServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceSpringTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsSessionWhenCredentialsAreValid() {
        User user = user(1L, "john", "john@example.com", "secret123");
        List<String> roles = List.of("ROLE_USER");
        AuthSession expectedSession = new AuthSession(
                "token-value",
                1L,
                "john",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        when(userRepository.findByUsernameOrEmail("john")).thenReturn(Optional.of(user));
        when(userRoleService.resolveRoles("john")).thenReturn(roles);
        when(jwtTokenService.createSession(1L, "john", roles)).thenReturn(expectedSession);

        AuthSession actual = authService.login("john", "secret123");

        assertSame(expectedSession, actual);
        verify(userRoleService).resolveRoles("john");
        verify(jwtTokenService).createSession(1L, "john", roles);
    }

    @Test
    void loginThrowsUnauthorizedWhenUserDoesNotExist() {
        when(userRepository.findByUsernameOrEmail("missing")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedServiceException.class, () -> authService.login("missing", "secret123"));

        verify(jwtTokenService, never()).createSession(any(), any(), any());
    }

    @Test
    void loginThrowsUnauthorizedWhenPasswordIsInvalid() {
        User user = user(1L, "john", "john@example.com", "secret123");
        when(userRepository.findByUsernameOrEmail(eq("john"))).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedServiceException.class, () -> authService.login("john", "bad-password"));

        verify(jwtTokenService, never()).createSession(any(), any(), any());
    }

    private User user(Long id, String username, String email, String rawPassword) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        return user;
    }
}
