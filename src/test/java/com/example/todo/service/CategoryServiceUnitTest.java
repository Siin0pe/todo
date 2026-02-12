package com.example.todo.service;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.model.Category;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.exception.NotFoundServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceUnitTest {

    @Test
    void createCategory_returnsResponse() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        CategoryRepository repository = mock(CategoryRepository.class);
        Category persisted = new Category();
        persisted.setId(4L);
        persisted.setLabel("Work");
        when(repository.create(org.mockito.ArgumentMatchers.any(Category.class))).thenReturn(persisted);

        CategoryService service = new CategoryService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected CategoryRepository categoryRepository(EntityManager em) {
                return repository;
            }
        };

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setLabel("Work");

        CategoryResponse response = service.createCategory(request);

        assertNotNull(response.getId());
        assertEquals("Work", response.getLabel());
    }

    @Test
    void updateCategory_throwsNotFound() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        CategoryRepository repository = mock(CategoryRepository.class);
        when(repository.findById(99L)).thenReturn(null);

        CategoryService service = new CategoryService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected CategoryRepository categoryRepository(EntityManager em) {
                return repository;
            }
        };

        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setLabel("New");

        assertThrows(NotFoundServiceException.class, () -> service.updateCategory(99L, request));
    }
}
