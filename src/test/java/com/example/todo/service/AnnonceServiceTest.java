package com.example.todo.service;

import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {
    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityTransaction transaction;
    @Mock
    private AnnonceRepository repository;

    private AnnonceService service;

    @BeforeEach
    void setUp() {
        service = new TestAnnonceService(entityManager, repository);
    }

    @Test
    void createAnnonceWithDetailsSetsDefaults() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        User author = new User();
        Category category = new Category();
        when(entityManager.getReference(User.class, 10L)).thenReturn(author);
        when(entityManager.getReference(Category.class, 20L)).thenReturn(category);
        when(repository.create(any(Annonce.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Annonce result = service.createAnnonce("Title", "Desc", "Addr", "mail@example.com", 10L, 20L);

        ArgumentCaptor<Annonce> captor = ArgumentCaptor.forClass(Annonce.class);
        verify(repository).create(captor.capture());
        Annonce created = captor.getValue();
        assertEquals("Title", created.getTitle());
        assertEquals("Desc", created.getDescription());
        assertEquals("Addr", created.getAdress());
        assertEquals("mail@example.com", created.getMail());
        assertEquals(Annonce.Status.DRAFT, created.getStatus());
        assertEquals(author, created.getAuthor());
        assertEquals(category, created.getCategory());
        assertNotNull(created.getDate());
        assertEquals(Annonce.Status.DRAFT, result.getStatus());
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void updateAnnonceReturnsNullWhenMissing() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(repository.findById(5L)).thenReturn(null);

        Annonce result = service.updateAnnonce(5L, "Title", "Desc", "Addr", "mail@example.com", 2L);

        assertNull(result);
        verify(repository).findById(5L);
        verify(repository, never()).update(any(Annonce.class));
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void publishAnnonceUpdatesStatus() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        Annonce annonce = new Annonce();
        annonce.setStatus(Annonce.Status.DRAFT);
        when(repository.findById(8L)).thenReturn(annonce);
        when(repository.update(annonce)).thenReturn(annonce);

        Annonce result = service.publishAnnonce(8L);

        assertEquals(Annonce.Status.PUBLISHED, annonce.getStatus());
        assertEquals(Annonce.Status.PUBLISHED, result.getStatus());
        verify(repository).update(annonce);
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void deleteAnnonceDelegatesToRepository() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        service.deleteAnnonce(11L);

        verify(repository).delete(11L);
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager).close();
    }

    @Test
    void filterAnnoncesDelegatesBasedOnInputs() {
        List<Annonce> annonces = Collections.singletonList(new Annonce());
        when(repository.findByCategoryAndStatus(1L, Annonce.Status.PUBLISHED, 0, 5)).thenReturn(annonces);

        List<Annonce> result = service.filterAnnonces(1L, Annonce.Status.PUBLISHED, 0, 5);

        assertEquals(annonces, result);
        verify(repository).findByCategoryAndStatus(1L, Annonce.Status.PUBLISHED, 0, 5);
        verify(entityManager).close();
    }

    @Test
    void filterAnnoncesWithCategoryOnly() {
        List<Annonce> annonces = Collections.singletonList(new Annonce());
        when(repository.findByCategory(2L, 1, 10)).thenReturn(annonces);

        List<Annonce> result = service.filterAnnonces(2L, null, 1, 10);

        assertEquals(annonces, result);
        verify(repository).findByCategory(2L, 1, 10);
        verify(entityManager).close();
    }

    @Test
    void filterAnnoncesWithStatusOnly() {
        List<Annonce> annonces = Collections.singletonList(new Annonce());
        when(repository.findByStatus(Annonce.Status.ARCHIVED, 0, 3)).thenReturn(annonces);

        List<Annonce> result = service.filterAnnonces(null, Annonce.Status.ARCHIVED, 0, 3);

        assertEquals(annonces, result);
        verify(repository).findByStatus(Annonce.Status.ARCHIVED, 0, 3);
        verify(entityManager).close();
    }

    @Test
    void filterAnnoncesWithoutFiltersUsesFindAll() {
        List<Annonce> annonces = Collections.singletonList(new Annonce());
        when(repository.findAll(0, 2)).thenReturn(annonces);

        List<Annonce> result = service.filterAnnonces(null, null, 0, 2);

        assertEquals(annonces, result);
        verify(repository).findAll(0, 2);
        verify(entityManager).close();
    }

    private static class TestAnnonceService extends AnnonceService {
        private final EntityManager entityManager;
        private final AnnonceRepository repository;

        private TestAnnonceService(EntityManager entityManager, AnnonceRepository repository) {
            this.entityManager = entityManager;
            this.repository = repository;
        }

        @Override
        protected EntityManager getEntityManager() {
            return entityManager;
        }

        @Override
        protected AnnonceRepository annonceRepository(EntityManager entityManager) {
            return repository;
        }
    }
}
