package com.example.todo.service;

import com.example.todo.model.Category;
import com.example.todo.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityTransaction transaction;
    @Mock
    private CategoryRepository repository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new TestCategoryService(entityManager, repository);
    }

    @Test
    void createCategoryReturnsNullWhenLabelExists() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(repository.findByLabel("Jobs")).thenReturn(new Category());

        Category result = service.createCategory("Jobs");

        assertNull(result);
        verify(repository).findByLabel("Jobs");
        verify(repository, never()).create(any(Category.class));
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void createCategoryCreatesWhenLabelIsFree() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(repository.findByLabel("Services")).thenReturn(null);
        when(repository.create(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = service.createCategory("Services");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(repository).create(captor.capture());
        assertEquals("Services", captor.getValue().getLabel());
        assertEquals("Services", result.getLabel());
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void listCategoriesDelegatesToRepository() {
        List<Category> categories = Collections.singletonList(new Category());
        when(repository.findAll(0, 1000)).thenReturn(categories);

        List<Category> result = service.listCategories();

        assertEquals(categories, result);
        verify(repository).findAll(0, 1000);
        verify(entityManager).close();
    }

    private static class TestCategoryService extends CategoryService {
        private final EntityManager entityManager;
        private final CategoryRepository repository;

        private TestCategoryService(EntityManager entityManager, CategoryRepository repository) {
            this.entityManager = entityManager;
            this.repository = repository;
        }

        @Override
        protected EntityManager getEntityManager() {
            return entityManager;
        }

        @Override
        protected CategoryRepository categoryRepository(EntityManager entityManager) {
            return repository;
        }
    }
}
