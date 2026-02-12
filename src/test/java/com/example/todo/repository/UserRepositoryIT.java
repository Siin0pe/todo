package com.example.todo.repository;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.test.TestDatabase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserRepositoryIT {

    @BeforeEach
    void setUp() {
        TestDatabase.reset();
    }

    @Test
    void findByUsername_returnsUser() {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            UserRepository repository = new UserRepository(entityManager);
            User user = repository.findByUsername("alice");

            assertNotNull(user);
            assertEquals("alice@example.com", user.getEmail());
        } finally {
            entityManager.close();
        }
    }

    @Test
    void findByEmail_returnsUser() {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            UserRepository repository = new UserRepository(entityManager);
            User user = repository.findByEmail("bob@example.com");

            assertNotNull(user);
            assertEquals("bob", user.getUsername());
        } finally {
            entityManager.close();
        }
    }
}
