package com.example.todo.service;

import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityTransaction transaction;
    @Mock
    private UserRepository repository;

    private UserService service;

    @BeforeEach
    void setUp() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        service = new TestUserService(entityManager, repository);
    }

    @Test
    void registerUserReturnsNullWhenUserExists() {
        when(repository.findByUsernameOrEmail("alice", "alice@example.com")).thenReturn(new User());

        User result = service.registerUser("alice", "alice@example.com", "secret");

        assertNull(result);
        verify(repository).findByUsernameOrEmail("alice", "alice@example.com");
        verify(repository, never()).create(any(User.class));
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void registerUserCreatesWhenUserDoesNotExist() {
        when(repository.findByUsernameOrEmail("bob", "bob@example.com")).thenReturn(null);
        when(repository.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.registerUser("bob", "bob@example.com", "secret");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).create(captor.capture());
        assertEquals("bob", captor.getValue().getUsername());
        assertEquals("bob@example.com", captor.getValue().getEmail());
        assertTrue(BCrypt.checkpw("secret", captor.getValue().getPassword()));
        assertEquals("bob", result.getUsername());
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    private static class TestUserService extends UserService {
        private final EntityManager entityManager;
        private final UserRepository repository;

        private TestUserService(EntityManager entityManager, UserRepository repository) {
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
