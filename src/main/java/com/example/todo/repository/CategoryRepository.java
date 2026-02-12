package com.example.todo.repository;

import com.example.todo.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoryRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryRepository.class);

    private final EntityManager entityManager;

    public CategoryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Category create(Category category) {
        LOGGER.debug("category_repository_create");
        entityManager.persist(category);
        return category;
    }

    public Category findById(Long id) {
        Category category = entityManager.find(Category.class, id);
        LOGGER.debug("category_repository_find_by_id id={} found={}", id, category != null);
        return category;
    }

    public List<Category> findAll(int page, int size) {
        TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c ORDER BY c.label ASC",
                Category.class);
        applyPagination(query, page, size);
        List<Category> results = query.getResultList();
        LOGGER.debug("category_repository_find_all page={} size={} returned={}", page, size, results.size());
        return results;
    }

    public Category update(Category category) {
        LOGGER.debug("category_repository_update id={}", category.getId());
        return entityManager.merge(category);
    }

    public void delete(Long id) {
        Category category = entityManager.find(Category.class, id);
        if (category != null) {
            entityManager.remove(category);
            LOGGER.debug("category_repository_delete id={} deleted=true", id);
        } else {
            LOGGER.debug("category_repository_delete id={} deleted=false", id);
        }
    }

    private void applyPagination(TypedQuery<?> query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        query.setFirstResult(safePage * safeSize);
        query.setMaxResults(safeSize);
    }
}
