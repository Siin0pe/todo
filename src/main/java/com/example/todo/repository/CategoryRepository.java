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
                "SELECT c FROM Category c ORDER BY c.label ASC",
                Category.class);
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

    private void applyPagination(TypedQuery<?> query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        query.setFirstResult(safePage * safeSize);
        query.setMaxResults(safeSize);
    }
}
