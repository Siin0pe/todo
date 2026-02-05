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

    public List<User> findAll(int page, int size) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u ORDER BY u.createdAt DESC", User.class);
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public User update(User user) {
        return entityManager.merge(user);
    }

    public void delete(Long id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    public List<User> searchByKeyword(String keyword, int page, int size) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u " +
                        "WHERE LOWER(u.username) LIKE :kw OR LOWER(u.email) LIKE :kw " +
                        "ORDER BY u.createdAt DESC",
                User.class);
        query.setParameter("kw", normalizeKeyword(keyword));
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public User findByLogin(String login) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u " +
                        "WHERE LOWER(u.username) = :login OR LOWER(u.email) = :login",
                User.class);
        query.setParameter("login", login == null ? "" : login.trim().toLowerCase());
        List<User> results = query.setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByUsernameOrEmail(String username, String email) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u " +
                        "WHERE LOWER(u.username) = :username OR LOWER(u.email) = :email",
                User.class);
        query.setParameter("username", username == null ? "" : username.trim().toLowerCase());
        query.setParameter("email", email == null ? "" : email.trim().toLowerCase());
        List<User> results = query.setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private void applyPagination(TypedQuery<?> query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        query.setFirstResult(safePage * safeSize);
        query.setMaxResults(safeSize);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "%";
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }
}
