package com.example.todo.service;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.exception.ConflictServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceUnitTest {

    @Test
    void register_createsUser() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsername("alice")).thenReturn(null);
        when(repository.findByEmail("alice@example.com")).thenReturn(null);

        User persisted = new User();
        persisted.setId(10L);
        persisted.setUsername("alice");
        persisted.setEmail("alice@example.com");
        when(repository.create(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(persisted);

        UserService service = new UserService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected UserRepository userRepository(EntityManager em) {
                return repository;
            }
        };

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        UserResponse response = service.register(request);

        assertNotNull(response.getId());
        assertEquals("alice", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).create(captor.capture());
        User created = captor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("secret123", created.getPassword()));
    }

    @Test
    void register_throwsConflict_whenUsernameExists() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsername("alice")).thenReturn(new User());

        UserService service = new UserService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected UserRepository userRepository(EntityManager em) {
                return repository;
            }
        };

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("secret123");

        assertThrows(ConflictServiceException.class, () -> service.register(request));
        verify(repository, never()).create(org.mockito.ArgumentMatchers.any(User.class));
    }
}
