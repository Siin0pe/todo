package com.example.todo.repository;

import com.example.todo.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryRepositoryUnitTest {

    @Test
    void findAll_appliesPagination() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Category> query = mock(TypedQuery.class);
        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Category.class)))
                .thenReturn(query);
        when(query.setFirstResult(6)).thenReturn(query);
        when(query.setMaxResults(3)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        CategoryRepository repository = new CategoryRepository(entityManager);
        repository.findAll(2, 3);

        verify(query).setFirstResult(6);
        verify(query).setMaxResults(3);
    }
}
