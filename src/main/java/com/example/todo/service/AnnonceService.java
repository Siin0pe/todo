package com.example.todo.service;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnonceDTO;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.mapper.AnnonceMapper;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
import com.example.todo.repository.AnnonceSpecifications;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.exception.BadRequestServiceException;
import com.example.todo.service.exception.ConflictServiceException;
import com.example.todo.service.exception.ForbiddenServiceException;
import com.example.todo.service.exception.NotFoundServiceException;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnnonceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnonceService.class);
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final AnnonceRepository annonceRepository;
    private final CategoryRepository categoryRepository;
    private final AnnonceMapper annonceMapper;
    private final AnnonceMetadataService annonceMetadataService;

    @PersistenceContext
    private EntityManager entityManager;

    public AnnonceService(AnnonceRepository annonceRepository,
                          CategoryRepository categoryRepository,
                          AnnonceMapper annonceMapper,
                          AnnonceMetadataService annonceMetadataService) {
        this.annonceRepository = annonceRepository;
        this.categoryRepository = categoryRepository;
        this.annonceMapper = annonceMapper;
        this.annonceMetadataService = annonceMetadataService;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public AnnonceDTO createAnnonce(AnnonceCreateRequest request, Long currentUserId) {
        LOGGER.info("annonce_service_create_requested currentUserId={} authorId={} categoryId={}",
                currentUserId, request.getAuthorId(), request.getCategoryId());
        ensureAuthenticated(currentUserId);
        if (!currentUserId.equals(request.getAuthorId())) {
            LOGGER.warn("annonce_service_create_rejected_author_mismatch currentUserId={} authorId={}",
                    currentUserId, request.getAuthorId());
            throw new BadRequestServiceException("Author does not match authenticated user");
        }

        ensureCategoryExists(request.getCategoryId());
        Annonce annonce = annonceMapper.toEntity(request);
        annonce.setAuthor(entityManager.getReference(User.class, request.getAuthorId()));
        annonce.setCategory(entityManager.getReference(Category.class, request.getCategoryId()));
        Annonce created = annonceRepository.save(annonce);

        LOGGER.info("annonce_service_create_succeeded annonceId={} authorId={}", created.getId(), currentUserId);
        return annonceMapper.toDto(created);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public AnnonceDTO updateAnnonce(Long annonceId, AnnonceUpdateRequest request, Long currentUserId) {
        LOGGER.info("annonce_service_update_requested annonceId={} currentUserId={}", annonceId, currentUserId);
        ensureAuthenticated(currentUserId);
        Annonce annonce = findAnnonceOrThrow(annonceId);
        if (request.getStatus() == Annonce.Status.ARCHIVED) {
            ensureAdminForArchive();
            ensureArchiveOnlyStatusChange(annonce, request);
        } else {
            ensureAuthor(annonce, currentUserId);
        }
        enforcePublishedRules(annonce, request);
        ensureCategoryExists(request.getCategoryId());

        annonceMapper.updateFromRequest(request, annonce);
        annonce.setCategory(entityManager.getReference(Category.class, request.getCategoryId()));
        Annonce updated = annonceRepository.save(annonce);

        LOGGER.info("annonce_service_update_succeeded annonceId={} currentUserId={}", annonceId, currentUserId);
        return annonceMapper.toDto(updated);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void deleteAnnonce(Long annonceId, Long currentUserId) {
        LOGGER.info("annonce_service_delete_requested annonceId={} currentUserId={}", annonceId, currentUserId);
        ensureAuthenticated(currentUserId);
        Annonce annonce = findAnnonceOrThrow(annonceId);
        ensureAuthor(annonce, currentUserId);
        if (annonce.getStatus() != Annonce.Status.ARCHIVED) {
            LOGGER.warn("annonce_service_delete_rejected_status annonceId={} status={}",
                    annonceId, annonce.getStatus());
            throw new ConflictServiceException("Annonce must be archived before deletion");
        }
        annonceRepository.delete(annonce);
        LOGGER.info("annonce_service_delete_succeeded annonceId={} currentUserId={}", annonceId, currentUserId);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public AnnonceDTO patchAnnonce(Long annonceId, AnnoncePatchRequest request, Long currentUserId) {
        LOGGER.info("annonce_service_patch_requested annonceId={} currentUserId={}", annonceId, currentUserId);
        ensureAuthenticated(currentUserId);
        Annonce annonce = findAnnonceOrThrow(annonceId);
        if (request.getStatus() == Annonce.Status.ARCHIVED) {
            ensureAdminForArchive();
            ensureArchiveOnlyStatusChange(request);
        } else {
            ensureAuthor(annonce, currentUserId);
        }
        enforcePublishedRules(annonce, request);
        if (request.getCategoryId() != null) {
            ensureCategoryExists(request.getCategoryId());
        }

        annonceMapper.patchFromRequest(request, annonce);
        if (request.getCategoryId() != null) {
            annonce.setCategory(entityManager.getReference(Category.class, request.getCategoryId()));
        }
        Annonce updated = annonceRepository.save(annonce);

        LOGGER.info("annonce_service_patch_succeeded annonceId={} currentUserId={}", annonceId, currentUserId);
        return annonceMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public AnnonceDTO findById(Long annonceId) {
        LOGGER.info("annonce_service_find_by_id_requested annonceId={}", annonceId);
        Annonce annonce = annonceRepository.findDetailedById(annonceId)
                .orElseThrow(() -> {
                    LOGGER.warn("annonce_service_find_by_id_not_found annonceId={}", annonceId);
                    return new NotFoundServiceException("Annonce not found");
                });
        LOGGER.info("annonce_service_find_by_id_succeeded annonceId={}", annonceId);
        return annonceMapper.toDto(annonce);
    }

    @Transactional(readOnly = true)
    public Page<AnnonceDTO> searchAnnonces(AnnonceSearchCriteria criteria, Pageable pageable) {
        LOGGER.info(
                "annonce_service_search_requested q={} status={} categoryId={} authorId={} fromDate={} toDate={} page={} size={}",
                criteria.q(),
                criteria.status(),
                criteria.categoryId(),
                criteria.authorId(),
                criteria.fromDate(),
                criteria.toDate(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        validateDateRange(criteria);
        Pageable normalized = annonceMetadataService.validateAndNormalize(pageable);

        Specification<Annonce> specification = Specification.allOf(
                AnnonceSpecifications.keywordInFields(criteria.q(), annonceMetadataService.getSearchableStringFields()),
                AnnonceSpecifications.hasStatus(criteria.status()),
                AnnonceSpecifications.hasCategoryId(criteria.categoryId()),
                AnnonceSpecifications.hasAuthorId(criteria.authorId()),
                AnnonceSpecifications.fromDate(criteria.fromDate()),
                AnnonceSpecifications.toDate(criteria.toDate())
        );

        Page<AnnonceDTO> page = annonceRepository.findAll(specification, normalized).map(annonceMapper::toDto);
        LOGGER.info("annonce_service_search_succeeded page={} size={} returned={}",
                page.getNumber(), page.getSize(), page.getNumberOfElements());
        return page;
    }

    private Annonce findAnnonceOrThrow(Long annonceId) {
        return annonceRepository.findById(annonceId)
                .orElseThrow(() -> {
                    LOGGER.warn("annonce_service_not_found annonceId={}", annonceId);
                    return new NotFoundServiceException("Annonce not found");
                });
    }

    private void ensureCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            LOGGER.warn("annonce_service_category_not_found categoryId={}", categoryId);
            throw new NotFoundServiceException("Category not found");
        }
    }

    private void ensureAuthenticated(Long currentUserId) {
        if (currentUserId == null) {
            LOGGER.warn("annonce_service_authentication_missing");
            throw new UnauthorizedServiceException("Unauthorized");
        }
    }

    private void ensureAuthor(Annonce annonce, Long currentUserId) {
        if (annonce.getAuthor() == null || annonce.getAuthor().getId() == null) {
            LOGGER.warn("annonce_service_author_missing annonceId={}", annonce.getId());
            throw new ForbiddenServiceException("Author not available");
        }
        if (!annonce.getAuthor().getId().equals(currentUserId)) {
            LOGGER.warn("annonce_service_author_mismatch annonceId={} expectedAuthorId={} currentUserId={}",
                    annonce.getId(), annonce.getAuthor().getId(), currentUserId);
            throw new ForbiddenServiceException("Only the author can modify or delete this annonce");
        }
    }

    private void enforcePublishedRules(Annonce annonce, AnnonceUpdateRequest request) {
        if (annonce.getStatus() != Annonce.Status.PUBLISHED) {
            return;
        }
        boolean statusToArchived = request.getStatus() == Annonce.Status.ARCHIVED;
        boolean fieldsUnchanged = safeEquals(annonce.getTitle(), request.getTitle())
                && safeEquals(annonce.getDescription(), request.getDescription())
                && safeEquals(annonce.getAdress(), request.getAdress())
                && safeEquals(annonce.getMail(), request.getMail())
                && safeEquals(annonce.getCategory() == null ? null : annonce.getCategory().getId(), request.getCategoryId());
        if (!statusToArchived || !fieldsUnchanged) {
            LOGGER.warn("annonce_service_update_rejected_published annonceId={}", annonce.getId());
            throw new ConflictServiceException("Published annonces cannot be modified");
        }
    }

    private void enforcePublishedRules(Annonce annonce, AnnoncePatchRequest request) {
        if (annonce.getStatus() != Annonce.Status.PUBLISHED) {
            return;
        }
        boolean hasFieldChanges = request.getTitle() != null
                || request.getDescription() != null
                || request.getAdress() != null
                || request.getMail() != null
                || request.getCategoryId() != null;
        boolean statusToArchived = request.getStatus() == Annonce.Status.ARCHIVED;
        if (hasFieldChanges || !statusToArchived) {
            LOGGER.warn("annonce_service_patch_rejected_published annonceId={}", annonce.getId());
            throw new ConflictServiceException("Published annonces cannot be modified");
        }
    }

    private void ensureAdminForArchive() {
        if (!currentUserHasRole(ADMIN_ROLE)) {
            LOGGER.warn("annonce_service_archive_forbidden requiredRole={}", ADMIN_ROLE);
            throw new ForbiddenServiceException("Only an admin can archive an annonce");
        }
    }

    private void ensureArchiveOnlyStatusChange(Annonce annonce, AnnonceUpdateRequest request) {
        boolean fieldsUnchanged = safeEquals(annonce.getTitle(), request.getTitle())
                && safeEquals(annonce.getDescription(), request.getDescription())
                && safeEquals(annonce.getAdress(), request.getAdress())
                && safeEquals(annonce.getMail(), request.getMail())
                && safeEquals(annonce.getCategory() == null ? null : annonce.getCategory().getId(), request.getCategoryId());
        if (!fieldsUnchanged) {
            LOGGER.warn("annonce_service_archive_rejected_non_status_change annonceId={}", annonce.getId());
            throw new ConflictServiceException("Archiving cannot change annonce fields");
        }
    }

    private void ensureArchiveOnlyStatusChange(AnnoncePatchRequest request) {
        boolean containsFieldChange = request.getTitle() != null
                || request.getDescription() != null
                || request.getAdress() != null
                || request.getMail() != null
                || request.getCategoryId() != null;
        if (containsFieldChange) {
            LOGGER.warn("annonce_service_archive_rejected_non_status_patch");
            throw new ConflictServiceException("Archiving cannot change annonce fields");
        }
    }

    private boolean safeEquals(Object left, Object right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private boolean currentUserHasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private void validateDateRange(AnnonceSearchCriteria criteria) {
        if (criteria.fromDate() != null && criteria.toDate() != null && criteria.fromDate().isAfter(criteria.toDate())) {
            throw new BadRequestServiceException("fromDate must be before or equal to toDate");
        }
    }
}
