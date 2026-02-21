package com.example.todo.service;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceDTO;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.mapper.AnnonceMapper;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.exception.BadRequestServiceException;
import com.example.todo.service.exception.ConflictServiceException;
import com.example.todo.service.exception.ForbiddenServiceException;
import com.example.todo.service.exception.NotFoundServiceException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnonceServiceSpringTest {

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AnnonceMapper annonceMapper;

    @Mock
    private AnnonceMetadataService annonceMetadataService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AnnonceService annonceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(annonceService, "entityManager", entityManager);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAnnonceThrowsBadRequestWhenAuthorDoesNotMatchAuthenticatedUser() {
        AnnonceCreateRequest request = AnnonceCreateRequest.builder()
                .title("Titre")
                .description("Description")
                .adress("Adresse")
                .mail("mail@example.com")
                .authorId(2L)
                .categoryId(10L)
                .build();

        assertThrows(BadRequestServiceException.class, () -> annonceService.createAnnonce(request, 1L));
        verify(categoryRepository, never()).existsById(anyLong());
    }

    @Test
    void createAnnonceThrowsNotFoundWhenCategoryDoesNotExist() {
        AnnonceCreateRequest request = AnnonceCreateRequest.builder()
                .title("Titre")
                .description("Description")
                .adress("Adresse")
                .mail("mail@example.com")
                .authorId(1L)
                .categoryId(10L)
                .build();
        when(categoryRepository.existsById(10L)).thenReturn(false);

        assertThrows(NotFoundServiceException.class, () -> annonceService.createAnnonce(request, 1L));
        verify(annonceRepository, never()).save(any(Annonce.class));
    }

    @Test
    void createAnnonceReturnsDtoWhenRequestIsValid() {
        AnnonceCreateRequest request = AnnonceCreateRequest.builder()
                .title("Titre")
                .description("Description")
                .adress("Adresse")
                .mail("mail@example.com")
                .authorId(1L)
                .categoryId(10L)
                .build();
        when(categoryRepository.existsById(10L)).thenReturn(true);

        Annonce entity = new Annonce();
        when(annonceMapper.toEntity(request)).thenReturn(entity);

        User authorRef = new User();
        authorRef.setId(1L);
        Category categoryRef = new Category();
        categoryRef.setId(10L);
        when(entityManager.getReference(User.class, 1L)).thenReturn(authorRef);
        when(entityManager.getReference(Category.class, 10L)).thenReturn(categoryRef);

        Annonce persisted = new Annonce();
        persisted.setId(100L);
        when(annonceRepository.save(entity)).thenReturn(persisted);

        AnnonceDTO expected = new AnnonceDTO();
        expected.setId(100L);
        when(annonceMapper.toDto(persisted)).thenReturn(expected);

        AnnonceDTO actual = annonceService.createAnnonce(request, 1L);

        assertSame(expected, actual);
        verify(annonceRepository).save(entity);
    }

    @Test
    void patchAnnonceThrowsForbiddenWhenArchivingWithoutAdminRole() {
        setAuthenticatedRole("ROLE_USER");
        Annonce existing = annonce(5L, 1L, 10L, Annonce.Status.DRAFT);
        when(annonceRepository.findById(5L)).thenReturn(Optional.of(existing));

        AnnoncePatchRequest request = AnnoncePatchRequest.builder()
                .status(Annonce.Status.ARCHIVED)
                .build();

        assertThrows(ForbiddenServiceException.class, () -> annonceService.patchAnnonce(5L, request, 1L));
    }

    @Test
    void patchAnnonceThrowsConflictWhenPublishedAnnonceIsModified() {
        Annonce existing = annonce(5L, 1L, 10L, Annonce.Status.PUBLISHED);
        when(annonceRepository.findById(5L)).thenReturn(Optional.of(existing));

        AnnoncePatchRequest request = AnnoncePatchRequest.builder()
                .title("Nouveau titre")
                .build();

        assertThrows(ConflictServiceException.class, () -> annonceService.patchAnnonce(5L, request, 1L));
    }

    @Test
    void deleteAnnonceThrowsConflictWhenAnnonceIsNotArchived() {
        Annonce existing = annonce(5L, 1L, 10L, Annonce.Status.DRAFT);
        when(annonceRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(ConflictServiceException.class, () -> annonceService.deleteAnnonce(5L, 1L));
    }

    @Test
    void searchAnnoncesThrowsBadRequestWhenDateRangeIsInvalid() {
        AnnonceSearchCriteria criteria = new AnnonceSearchCriteria(
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-10T10:00:00Z"),
                Instant.parse("2026-02-09T10:00:00Z")
        );

        assertThrows(BadRequestServiceException.class,
                () -> annonceService.searchAnnonces(criteria, PageRequest.of(0, 20)));

        verify(annonceMetadataService, never()).validateAndNormalize(any());
    }

    private Annonce annonce(Long annonceId, Long authorId, Long categoryId, Annonce.Status status) {
        User author = new User();
        author.setId(authorId);
        Category category = new Category();
        category.setId(categoryId);

        Annonce annonce = new Annonce();
        annonce.setId(annonceId);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        annonce.setStatus(status);
        return annonce;
    }

    private void setAuthenticatedRole(String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "principal",
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
