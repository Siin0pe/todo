package com.example.todo.repository;

import com.example.todo.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public User create(User user) {
        LOGGER.debug("user_repository_create");
        entityManager.persist(user);
        return user;
    }

    public User findById(Long id) {
        User user = entityManager.find(User.class, id);
        LOGGER.debug("user_repository_find_by_id id={} found={}", id, user != null);
        return user;
    }

    public User findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username",
                User.class);
        query.setParameter("username", username);
        List<User> results = query.setMaxResults(1).getResultList();
        LOGGER.debug("user_repository_find_by_username found={}", !results.isEmpty());
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class);
        query.setParameter("email", email);
        List<User> results = query.setMaxResults(1).getResultList();
        LOGGER.debug("user_repository_find_by_email found={}", !results.isEmpty());
        return results.isEmpty() ? null : results.get(0);
    }
}
