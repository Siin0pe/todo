package com.example.todo.repository;

import com.example.todo.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserRepository {
    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public User create(User user) {
        entityManager.persist(user);
        return user;
    }

    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    public User findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username",
                User.class);
        query.setParameter("username", username);
        List<User> results = query.setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class);
        query.setParameter("email", email);
        List<User> results = query.setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
