package com.example.todo.service;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
import com.example.todo.security.jaas.SubjectContextHolder;
import com.example.todo.security.jaas.UserPrincipal;
import com.example.todo.service.exception.BadRequestServiceException;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnonceServiceUnitTest {

    @AfterEach
    void cleanupSubjectContext() {
        SubjectContextHolder.clear();
    }

    @Test
    void createAnnonce_throwsUnauthorized_whenNoUser() {
        AnnonceService service = new AnnonceService();
        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setTitle("Title");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");

        assertThrows(UnauthorizedServiceException.class, () -> service.createAnnonce(request, null));
    }

    @Test
    void createAnnonce_throwsBadRequest_whenAuthorMismatch() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        AnnonceService service = new AnnonceService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }
        };
        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setAuthorId(2L);
        request.setCategoryId(1L);
        request.setTitle("Title");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");

        assertThrows(BadRequestServiceException.class, () -> service.createAnnonce(request, 1L));
    }

    @Test
    void createAnnonce_returnsResponse() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        User author = new User();
        author.setId(7L);
        Category category = new Category();
        category.setId(3L);
        when(entityManager.getReference(eq(User.class), eq(7L))).thenReturn(author);
        when(entityManager.getReference(eq(Category.class), eq(3L))).thenReturn(category);

        AnnonceRepository repository = mock(AnnonceRepository.class);
        when(repository.create(any(Annonce.class))).thenAnswer(invocation -> {
            Annonce annonce = invocation.getArgument(0);
            annonce.setId(55L);
            return annonce;
        });

        AnnonceService service = new AnnonceService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected AnnonceRepository annonceRepository(EntityManager em) {
                return repository;
            }
        };

        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setAuthorId(7L);
        request.setCategoryId(3L);
        request.setTitle("Title");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");

        AnnonceResponse response = service.createAnnonce(request, 7L);

        assertEquals(55L, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals(7L, response.getAuthorId());
        assertEquals(3L, response.getCategoryId());
    }

    @Test
    void createAnnonce_usesSubjectCurrentUserId_whenProvidedByJaasContext() {
        EntityManager entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(entityManager.getTransaction()).thenReturn(transaction);

        User author = new User();
        author.setId(7L);
        Category category = new Category();
        category.setId(3L);
        when(entityManager.getReference(eq(User.class), eq(7L))).thenReturn(author);
        when(entityManager.getReference(eq(Category.class), eq(3L))).thenReturn(category);

        AnnonceRepository repository = mock(AnnonceRepository.class);
        when(repository.create(any(Annonce.class))).thenAnswer(invocation -> {
            Annonce annonce = invocation.getArgument(0);
            annonce.setId(77L);
            return annonce;
        });

        Subject subject = new Subject();
        subject.getPrincipals().add(new UserPrincipal("alice", 7L));
        SubjectContextHolder.set(subject);

        AnnonceService service = new AnnonceService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected AnnonceRepository annonceRepository(EntityManager em) {
                return repository;
            }
        };

        AnnonceCreateRequest request = new AnnonceCreateRequest();
        request.setAuthorId(7L);
        request.setCategoryId(3L);
        request.setTitle("From subject");
        request.setDescription("Desc");
        request.setAdress("Addr");
        request.setMail("test@example.com");

        AnnonceResponse response = service.createAnnonce(request, null);

        assertEquals(77L, response.getId());
        assertEquals(7L, response.getAuthorId());
    }

    @Test
    void listAnnonces_returnsPaginatedResponse() {
        EntityManager entityManager = mock(EntityManager.class);
        AnnonceRepository repository = mock(AnnonceRepository.class);

        Annonce first = new Annonce();
        first.setId(1L);
        first.setTitle("A1");
        first.setDescription("D1");
        first.setAdress("Addr1");
        first.setMail("a1@example.com");
        first.setDate(Timestamp.from(Instant.parse("2024-01-01T00:00:00Z")));
        first.setStatus(Annonce.Status.DRAFT);
        User author = new User();
        author.setId(1L);
        Category category = new Category();
        category.setId(1L);
        first.setAuthor(author);
        first.setCategory(category);

        Annonce second = new Annonce();
        second.setId(2L);
        second.setTitle("A2");
        second.setDescription("D2");
        second.setAdress("Addr2");
        second.setMail("a2@example.com");
        second.setDate(Timestamp.from(Instant.parse("2024-01-02T00:00:00Z")));
        second.setStatus(Annonce.Status.PUBLISHED);
        second.setAuthor(author);
        second.setCategory(category);

        when(repository.findAllWithRelations(0, 2)).thenReturn(Arrays.asList(first, second));

        AnnonceService service = new AnnonceService() {
            @Override
            protected EntityManager getEntityManager() {
                return entityManager;
            }

            @Override
            protected AnnonceRepository annonceRepository(EntityManager em) {
                return repository;
            }
        };

        PaginatedResponse<AnnonceResponse> response = service.listAnnonces(0, 2);

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(2, response.getCount());
        assertNotNull(response.getItems().get(0).getId());
    }
}
