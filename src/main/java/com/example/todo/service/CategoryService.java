package com.example.todo.service;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Category;
import com.example.todo.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Function;

public class CategoryService {
    public Category createCategory(String label) {
        return executeInTransaction(entityManager -> {
            CategoryRepository repository = categoryRepository(entityManager);
            if (repository.findByLabel(label) != null) {
                return null;
            }
            Category category = new Category();
            category.setLabel(label);
            return repository.create(category);
        });
    }

    public List<Category> listCategories() {
        EntityManager entityManager = getEntityManager();
        try {
            CategoryRepository repository = categoryRepository(entityManager);
            return repository.findAll(0, 1000);
        } finally {
            entityManager.close();
        }
    }

    private <T> T executeInTransaction(Function<EntityManager, T> work) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            T result = work.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }

    protected EntityManager getEntityManager() {
        return EntityManagerUtil.getEntityManager();
    }

    protected CategoryRepository categoryRepository(EntityManager entityManager) {
        return new CategoryRepository(entityManager);
    }
}
