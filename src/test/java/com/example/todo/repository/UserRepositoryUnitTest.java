package com.example.todo.repository;

import com.example.todo.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRepositoryUnitTest {

    @Test
    void findByUsername_returnsUser() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<User> query = mock(TypedQuery.class);
        User user = new User();
        user.setUsername("alice");

        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(User.class)))
                .thenReturn(query);
        when(query.setParameter(org.mockito.ArgumentMatchers.eq("username"), org.mockito.ArgumentMatchers.eq("alice")))
                .thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(user));

        UserRepository repository = new UserRepository(entityManager);
        User result = repository.findByUsername("alice");

        assertEquals("alice", result.getUsername());
    }

    @Test
    void findByEmail_returnsNull_whenMissing() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<User> query = mock(TypedQuery.class);

        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(User.class)))
                .thenReturn(query);
        when(query.setParameter(org.mockito.ArgumentMatchers.eq("email"), org.mockito.ArgumentMatchers.eq("missing@example.com")))
                .thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        UserRepository repository = new UserRepository(entityManager);
        User result = repository.findByEmail("missing@example.com");

        assertNull(result);
    }
}
