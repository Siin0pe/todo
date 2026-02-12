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
import com.example.todo.service.exception.BadRequestServiceException;
import com.example.todo.service.exception.ConflictServiceException;
import com.example.todo.service.exception.ForbiddenServiceException;
import com.example.todo.service.exception.NotFoundServiceException;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnonceService {
    public AnnonceResponse createAnnonce(AnnonceCreateRequest request, Long currentUserId) {
        ensureAuthenticated(currentUserId);
        Annonce created = executeInTransaction(entityManager -> {
            if (!currentUserId.equals(request.getAuthorId())) {
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
        return AnnonceMapper.toResponse(created);
    }

    public AnnonceResponse updateAnnonce(Long annonceId, AnnonceUpdateRequest request, Long currentUserId) {
        ensureAuthenticated(currentUserId);
        Annonce updated = executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                throw new NotFoundServiceException("Annonce not found");
            }
            ensureAuthor(annonce, currentUserId);
            enforcePublishedRules(annonce, request);
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
        return AnnonceMapper.toResponse(updated);
    }

    public void deleteAnnonce(Long annonceId, Long currentUserId) {
        ensureAuthenticated(currentUserId);
        executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                throw new NotFoundServiceException("Annonce not found");
            }
            ensureAuthor(annonce, currentUserId);
            if (annonce.getStatus() != Annonce.Status.ARCHIVED) {
                throw new ConflictServiceException("Annonce must be archived before deletion");
            }
            repository.delete(annonceId);
            return null;
        });
    }

    public AnnonceResponse patchAnnonce(Long annonceId, AnnoncePatchRequest request, Long currentUserId) {
        ensureAuthenticated(currentUserId);
        Annonce updated = executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                throw new NotFoundServiceException("Annonce not found");
            }
            ensureAuthor(annonce, currentUserId);
            enforcePublishedRules(annonce, request);
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
        return AnnonceMapper.toResponse(updated);
    }

    public AnnonceResponse findById(Long annonceId) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findByIdWithRelations(annonceId);
            if (annonce == null) {
                throw new NotFoundServiceException("Annonce not found");
            }
            return AnnonceMapper.toResponse(annonce);
        } finally {
            entityManager.close();
        }
    }

    public PaginatedResponse<AnnonceResponse> listAnnonces(int page, int size) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            List<Annonce> annonces = repository.findAllWithRelations(page, size);
            List<AnnonceResponse> items = annonces.stream()
                    .map(AnnonceMapper::toResponse)
                    .collect(Collectors.toList());
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
            throw new UnauthorizedServiceException("Unauthorized");
        }
    }

    private void ensureAuthor(Annonce annonce, Long currentUserId) {
        if (annonce.getAuthor() == null || annonce.getAuthor().getId() == null) {
            throw new ForbiddenServiceException("Author not available");
        }
        if (!annonce.getAuthor().getId().equals(currentUserId)) {
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
                && safeEquals(annonce.getCategory() == null ? null : annonce.getCategory().getId(),
                request.getCategoryId());
        if (!statusToArchived || !fieldsUnchanged) {
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
            throw new ConflictServiceException("Published annonces cannot be modified");
        }
    }

    private boolean safeEquals(Object left, Object right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
