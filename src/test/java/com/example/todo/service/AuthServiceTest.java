package com.example.todo.service;

import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private UserRepository repository;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new TestAuthService(entityManager, repository);
    }

    @Test
    void authenticateReturnsUserWhenPasswordMatches() {
        User expected = new User();
        expected.setPassword(BCrypt.hashpw("secret", BCrypt.gensalt()));
        when(repository.findByLogin("alice")).thenReturn(expected);

        User result = service.authenticate("alice", "secret");

        assertEquals(expected, result);
        verify(repository).findByLogin("alice");
        verify(entityManager).close();
    }

    @Test
    void authenticateReturnsNullWhenPasswordDoesNotMatch() {
        User expected = new User();
        expected.setPassword(BCrypt.hashpw("secret", BCrypt.gensalt()));
        when(repository.findByLogin("alice")).thenReturn(expected);

        User result = service.authenticate("alice", "bad");

        assertNull(result);
        verify(repository).findByLogin("alice");
        verify(entityManager).close();
    }

    private static class TestAuthService extends AuthService {
        private final EntityManager entityManager;
        private final UserRepository repository;

        private TestAuthService(EntityManager entityManager, UserRepository repository) {
            this.entityManager = entityManager;
            this.repository = repository;
        }

        @Override
        protected EntityManager getEntityManager() {
            return entityManager;
        }

        @Override
        protected UserRepository userRepository(EntityManager entityManager) {
            return repository;
        }
    }
}
