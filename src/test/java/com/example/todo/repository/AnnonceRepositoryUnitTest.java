package com.example.todo.repository;

import com.example.todo.model.Annonce;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnnonceRepositoryUnitTest {

    @Test
    void findAllWithRelations_appliesPagination() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Annonce> query = mock(TypedQuery.class);
        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Annonce.class)))
                .thenReturn(query);
        when(query.setFirstResult(5)).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        AnnonceRepository repository = new AnnonceRepository(entityManager);
        repository.findAllWithRelations(1, 5);

        verify(query).setFirstResult(5);
        verify(query).setMaxResults(5);
    }

    @Test
    void findByIdWithRelations_returnsResult() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Annonce> query = mock(TypedQuery.class);
        Annonce annonce = new Annonce();
        annonce.setId(1L);
        when(entityManager.createQuery(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(Annonce.class)))
                .thenReturn(query);
        when(query.setParameter(org.mockito.ArgumentMatchers.eq("id"), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(annonce));

        AnnonceRepository repository = new AnnonceRepository(entityManager);
        Annonce result = repository.findByIdWithRelations(1L);

        assertSame(annonce, result);
    }
}
