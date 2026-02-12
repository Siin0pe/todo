package com.example.todo.service;

import com.example.todo.auth.AuthSession;
import com.example.todo.model.User;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceUnitTest {

    @Test
    void login_returnsSession_whenCredentialsValid() {
        User user = new User();
        user.setId(9L);
        user.setUsername("alice");
        user.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw("secret", org.mindrot.jbcrypt.BCrypt.gensalt()));

        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<User> query = mock(TypedQuery.class);
        when(entityManager.createQuery(eq("SELECT u FROM User u WHERE u.username = :login OR u.email = :login"), eq(User.class)))
                .thenReturn(query);
        when(query.setParameter(eq("login"), anyString())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(user));

        AuthService service = new AuthService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }
        };

        AuthSession session = service.login("alice", "secret");

        assertNotNull(session);
        assertEquals("alice", session.getUsername());
    }

    @Test
    void login_throwsUnauthorized_whenInvalidPassword() {
        User user = new User();
        user.setId(3L);
        user.setUsername("bob");
        user.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw("secret", org.mindrot.jbcrypt.BCrypt.gensalt()));

        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<User> query = mock(TypedQuery.class);
        when(entityManager.createQuery(eq("SELECT u FROM User u WHERE u.username = :login OR u.email = :login"), eq(User.class)))
                .thenReturn(query);
        when(query.setParameter(eq("login"), anyString())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(user));

        AuthService service = new AuthService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }
        };

        assertThrows(UnauthorizedServiceException.class, () -> service.login("bob", "wrong"));
    }

    @Test
    void login_throwsUnauthorized_whenUserMissing() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<User> query = mock(TypedQuery.class);
        when(entityManager.createQuery(eq("SELECT u FROM User u WHERE u.username = :login OR u.email = :login"), eq(User.class)))
                .thenReturn(query);
        when(query.setParameter(eq("login"), anyString())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        AuthService service = new AuthService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }
        };

        assertThrows(UnauthorizedServiceException.class, () -> service.login("missing", "secret"));
    }
}
