package com.example.todo.service;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.AnnoncePatchRequest;
import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.AnnonceUpdateRequest;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.mapper.AnnonceMapper;
import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
import com.example.todo.security.jaas.SubjectIdentity;
import com.example.todo.service.exception.BadRequestServiceException;
import com.example.todo.service.exception.ConflictServiceException;
import com.example.todo.service.exception.ForbiddenServiceException;
import com.example.todo.service.exception.NotFoundServiceException;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnonceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnonceService.class);
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String STATUS_ROLE_GUARD_PROPERTY = "todo.security.restrict.status.actions";

    public AnnonceResponse createAnnonce(AnnonceCreateRequest request, Long currentUserId) {
        currentUserId = resolveCurrentUserId(currentUserId);
        LOGGER.info("annonce_service_create_requested currentUserId={} authorId={} categoryId={}",
                currentUserId, request.getAuthorId(), request.getCategoryId());
        ensureAuthenticated(currentUserId);
        final Long resolvedUserId = currentUserId;
        Annonce created = executeInTransaction(entityManager -> {
            if (!resolvedUserId.equals(request.getAuthorId())) {
                LOGGER.warn("annonce_service_create_rejected_author_mismatch currentUserId={} authorId={}",
                        resolvedUserId, request.getAuthorId());
                throw new BadRequestServiceException("Author does not match authenticated user");
            }
            Annonce annonce = new Annonce();
            annonce.setTitle(request.getTitle());
            annonce.setDescription(request.getDescription());
            annonce.setAdress(request.getAdress());
            annonce.setMail(request.getMail());
            annonce.setAuthor(entityManager.getReference(User.class, request.getAuthorId()));
            annonce.setCategory(entityManager.getReference(Category.class, request.getCategoryId()));
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.create(annonce);
        });
        LOGGER.info("annonce_service_create_succeeded annonceId={} authorId={}", created.getId(), resolvedUserId);
        return AnnonceMapper.toResponse(created);
    }

    public AnnonceResponse updateAnnonce(Long annonceId, AnnonceUpdateRequest request, Long currentUserId) {
        currentUserId = resolveCurrentUserId(currentUserId);
        LOGGER.info("annonce_service_update_requested annonceId={} currentUserId={}", annonceId, currentUserId);
        ensureAuthenticated(currentUserId);
        final Long resolvedUserId = currentUserId;
        Annonce updated = executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                LOGGER.warn("annonce_service_update_not_found annonceId={}", annonceId);
                throw new NotFoundServiceException("Annonce not found");
            }
            ensureAuthor(annonce, resolvedUserId);
            enforcePublishedRules(annonce, request);
            enforceStatusTransitionRole(request.getStatus());
            annonce.setTitle(request.getTitle());
            annonce.setDescription(request.getDescription());
            annonce.setAdress(request.getAdress());
            annonce.setMail(request.getMail());
            annonce.setCategory(entityManager.getReference(Category.class, request.getCategoryId()));
            if (request.getStatus() != null) {
                annonce.setStatus(request.getStatus());
            }
            return repository.update(annonce);
        });
        LOGGER.info("annonce_service_update_succeeded annonceId={} currentUserId={}", annonceId, resolvedUserId);
        return AnnonceMapper.toResponse(updated);
    }

    public void deleteAnnonce(Long annonceId, Long currentUserId) {
        currentUserId = resolveCurrentUserId(currentUserId);
        LOGGER.info("annonce_service_delete_requested annonceId={} currentUserId={}", annonceId, currentUserId);
        ensureAuthenticated(currentUserId);
        final Long resolvedUserId = currentUserId;
        executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                LOGGER.warn("annonce_service_delete_not_found annonceId={}", annonceId);
                throw new NotFoundServiceException("Annonce not found");
            }
            ensureAuthor(annonce, resolvedUserId);
            if (annonce.getStatus() != Annonce.Status.ARCHIVED) {
                LOGGER.warn("annonce_service_delete_rejected_status annonceId={} status={}",
                        annonceId, annonce.getStatus());
                throw new ConflictServiceException("Annonce must be archived before deletion");
            }
            repository.delete(annonceId);
            return null;
        });
        LOGGER.info("annonce_service_delete_succeeded annonceId={} currentUserId={}", annonceId, resolvedUserId);
    }

    public AnnonceResponse patchAnnonce(Long annonceId, AnnoncePatchRequest request, Long currentUserId) {
        currentUserId = resolveCurrentUserId(currentUserId);
        LOGGER.info("annonce_service_patch_requested annonceId={} currentUserId={}", annonceId, currentUserId);
        ensureAuthenticated(currentUserId);
        final Long resolvedUserId = currentUserId;
        Annonce updated = executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                LOGGER.warn("annonce_service_patch_not_found annonceId={}", annonceId);
                throw new NotFoundServiceException("Annonce not found");
            }
            ensureAuthor(annonce, resolvedUserId);
            enforcePublishedRules(annonce, request);
            enforceStatusTransitionRole(request.getStatus());
            if (request.getTitle() != null) {
                annonce.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                annonce.setDescription(request.getDescription());
            }
            if (request.getAdress() != null) {
                annonce.setAdress(request.getAdress());
            }
            if (request.getMail() != null) {
                annonce.setMail(request.getMail());
            }
            if (request.getCategoryId() != null) {
                annonce.setCategory(entityManager.getReference(Category.class, request.getCategoryId()));
            }
            if (request.getStatus() != null) {
                annonce.setStatus(request.getStatus());
            }
            return repository.update(annonce);
        });
        LOGGER.info("annonce_service_patch_succeeded annonceId={} currentUserId={}", annonceId, resolvedUserId);
        return AnnonceMapper.toResponse(updated);
    }

    public AnnonceResponse findById(Long annonceId) {
        LOGGER.info("annonce_service_find_by_id_requested annonceId={}", annonceId);
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findByIdWithRelations(annonceId);
            if (annonce == null) {
                LOGGER.warn("annonce_service_find_by_id_not_found annonceId={}", annonceId);
                throw new NotFoundServiceException("Annonce not found");
            }
            LOGGER.info("annonce_service_find_by_id_succeeded annonceId={}", annonceId);
            return AnnonceMapper.toResponse(annonce);
        } finally {
            entityManager.close();
        }
    }

    public PaginatedResponse<AnnonceResponse> listAnnonces(int page, int size) {
        LOGGER.info("annonce_service_list_requested page={} size={}", page, size);
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            List<Annonce> annonces = repository.findAllWithRelations(page, size);
            List<AnnonceResponse> items = annonces.stream()
                    .map(AnnonceMapper::toResponse)
                    .collect(Collectors.toList());
            LOGGER.info("annonce_service_list_succeeded page={} size={} returned={}", page, size, items.size());
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
                LOGGER.warn("annonce_service_transaction_rollback reason={}", e.getClass().getSimpleName());
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

    protected AnnonceRepository annonceRepository(EntityManager entityManager) {
        return new AnnonceRepository(entityManager);
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

    private Long resolveCurrentUserId(Long fallbackUserId) {
        Long fromSubject = SubjectIdentity.currentUserId();
        return fromSubject != null ? fromSubject : fallbackUserId;
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
                && safeEquals(annonce.getCategory() == null ? null : annonce.getCategory().getId(),
                request.getCategoryId());
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

    private void enforceStatusTransitionRole(Annonce.Status requestedStatus) {
        if (!isStatusRoleGuardEnabled()) {
            return;
        }
        if (requestedStatus != Annonce.Status.PUBLISHED && requestedStatus != Annonce.Status.ARCHIVED) {
            return;
        }
        if (!SubjectIdentity.hasRole(ADMIN_ROLE)) {
            LOGGER.warn("annonce_service_status_role_rejected status={} requiredRole={}", requestedStatus, ADMIN_ROLE);
            throw new ForbiddenServiceException("This status transition requires admin role");
        }
    }

    private boolean isStatusRoleGuardEnabled() {
        return Boolean.parseBoolean(System.getProperty(STATUS_ROLE_GUARD_PROPERTY, "false"));
    }

    private boolean safeEquals(Object left, Object right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
