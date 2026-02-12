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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        LOGGER.info("category_service_create_requested label={}", request.getLabel());
        Category created = executeInTransaction(entityManager -> {
            Category category = new Category();
            category.setLabel(request.getLabel());
            return categoryRepository(entityManager).create(category);
        });
        LOGGER.info("category_service_create_succeeded categoryId={}", created.getId());
        return CategoryMapper.toResponse(created);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        LOGGER.info("category_service_update_requested categoryId={}", categoryId);
        Category updated = executeInTransaction(entityManager -> {
            CategoryRepository repository = categoryRepository(entityManager);
            Category category = repository.findById(categoryId);
            if (category == null) {
                LOGGER.warn("category_service_update_not_found categoryId={}", categoryId);
                throw new NotFoundServiceException("Category not found");
            }
            category.setLabel(request.getLabel());
            return repository.update(category);
        });
        LOGGER.info("category_service_update_succeeded categoryId={}", categoryId);
        return CategoryMapper.toResponse(updated);
    }

    public void deleteCategory(Long categoryId) {
        LOGGER.info("category_service_delete_requested categoryId={}", categoryId);
        executeInTransaction(entityManager -> {
            CategoryRepository repository = categoryRepository(entityManager);
            Category category = repository.findById(categoryId);
            if (category == null) {
                LOGGER.warn("category_service_delete_not_found categoryId={}", categoryId);
                throw new NotFoundServiceException("Category not found");
            }
            repository.delete(categoryId);
            return null;
        });
        LOGGER.info("category_service_delete_succeeded categoryId={}", categoryId);
    }

    public CategoryResponse findById(Long categoryId) {
        LOGGER.info("category_service_find_by_id_requested categoryId={}", categoryId);
        EntityManager entityManager = getEntityManager();
        try {
            Category category = categoryRepository(entityManager).findById(categoryId);
            if (category == null) {
                LOGGER.warn("category_service_find_by_id_not_found categoryId={}", categoryId);
                throw new NotFoundServiceException("Category not found");
            }
            LOGGER.info("category_service_find_by_id_succeeded categoryId={}", categoryId);
            return CategoryMapper.toResponse(category);
        } finally {
            entityManager.close();
        }
    }

    public PaginatedResponse<CategoryResponse> listCategories(int page, int size) {
        LOGGER.info("category_service_list_requested page={} size={}", page, size);
        EntityManager entityManager = getEntityManager();
        try {
            List<Category> categories = categoryRepository(entityManager).findAll(page, size);
            List<CategoryResponse> items = categories.stream()
                    .map(CategoryMapper::toResponse)
                    .collect(Collectors.toList());
            LOGGER.info("category_service_list_succeeded page={} size={} returned={}", page, size, items.size());
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
                LOGGER.warn("category_service_transaction_rollback reason={}", e.getClass().getSimpleName());
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
