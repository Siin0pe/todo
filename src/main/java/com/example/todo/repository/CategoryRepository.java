package com.example.todo.repository;

import com.example.todo.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CategoryRepository {
    private final EntityManager entityManager;

    public CategoryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Category create(Category category) {
        entityManager.persist(category);
        return category;
    }

    public Category findById(Long id) {
        return entityManager.find(Category.class, id);
    }

    public List<Category> findAll(int page, int size) {
        TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c ORDER BY c.label ASC", Category.class);
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public Category update(Category category) {
        return entityManager.merge(category);
    }

    public void delete(Long id) {
        Category category = entityManager.find(Category.class, id);
        if (category != null) {
            entityManager.remove(category);
        }
    }

    public List<Category> searchByKeyword(String keyword, int page, int size) {
        TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c WHERE LOWER(c.label) LIKE :kw ORDER BY c.label ASC",
                Category.class);
        query.setParameter("kw", normalizeKeyword(keyword));
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public Category findByLabel(String label) {
        TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c WHERE LOWER(c.label) = :label",
                Category.class);
        query.setParameter("label", label == null ? "" : label.trim().toLowerCase());
        List<Category> results = query.setMaxResults(1).getResultList();
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
