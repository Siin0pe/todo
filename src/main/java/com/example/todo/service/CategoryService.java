package com.example.todo.service;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.mapper.CategoryMapper;
import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Category;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.exception.NotFoundServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryService {
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Category created = executeInTransaction(entityManager -> {
            Category category = new Category();
            category.setLabel(request.getLabel());
            return categoryRepository(entityManager).create(category);
        });
        return CategoryMapper.toResponse(created);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category updated = executeInTransaction(entityManager -> {
            CategoryRepository repository = categoryRepository(entityManager);
            Category category = repository.findById(categoryId);
            if (category == null) {
                throw new NotFoundServiceException("Category not found");
            }
            category.setLabel(request.getLabel());
            return repository.update(category);
        });
        return CategoryMapper.toResponse(updated);
    }

    public void deleteCategory(Long categoryId) {
        executeInTransaction(entityManager -> {
            CategoryRepository repository = categoryRepository(entityManager);
            Category category = repository.findById(categoryId);
            if (category == null) {
                throw new NotFoundServiceException("Category not found");
            }
            repository.delete(categoryId);
            return null;
        });
    }

    public CategoryResponse findById(Long categoryId) {
        EntityManager entityManager = getEntityManager();
        try {
            Category category = categoryRepository(entityManager).findById(categoryId);
            if (category == null) {
                throw new NotFoundServiceException("Category not found");
            }
            return CategoryMapper.toResponse(category);
        } finally {
            entityManager.close();
        }
    }

    public PaginatedResponse<CategoryResponse> listCategories(int page, int size) {
        EntityManager entityManager = getEntityManager();
        try {
            List<Category> categories = categoryRepository(entityManager).findAll(page, size);
            List<CategoryResponse> items = categories.stream()
                    .map(CategoryMapper::toResponse)
                    .collect(Collectors.toList());
            return new PaginatedResponse<>(page, size, items);
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
