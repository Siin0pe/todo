package com.example.todo.service;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

public class AnnonceService {
    public Annonce createAnnonce(Annonce annonce) {
        return executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.create(annonce);
        });
    }

    public Annonce createAnnonce(String title,
                                 String description,
                                 String adress,
                                 String mail,
                                 Long authorId,
                                 Long categoryId) {
        return executeInTransaction(entityManager -> {
            Annonce annonce = new Annonce();
            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);
            annonce.setDate(new Timestamp(System.currentTimeMillis()));
            annonce.setStatus(Annonce.Status.DRAFT);
            annonce.setAuthor(entityManager.getReference(User.class, authorId));
            annonce.setCategory(entityManager.getReference(Category.class, categoryId));
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.create(annonce);
        });
    }

    public Annonce updateAnnonce(Annonce annonce) {
        return executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.update(annonce);
        });
    }

    public Annonce updateAnnonce(Long annonceId,
                                 String title,
                                 String description,
                                 String adress,
                                 String mail,
                                 Long categoryId) {
        return executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                return null;
            }
            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);
            annonce.setCategory(entityManager.getReference(Category.class, categoryId));
            return repository.update(annonce);
        });
    }

    public Annonce publishAnnonce(Long annonceId) {
        return executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                return null;
            }
            annonce.setStatus(Annonce.Status.PUBLISHED);
            return repository.update(annonce);
        });
    }

    public Annonce archiveAnnonce(Long annonceId) {
        return executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            Annonce annonce = repository.findById(annonceId);
            if (annonce == null) {
                return null;
            }
            annonce.setStatus(Annonce.Status.ARCHIVED);
            return repository.update(annonce);
        });
    }

    public void deleteAnnonce(Long annonceId) {
        executeInTransaction(entityManager -> {
            AnnonceRepository repository = annonceRepository(entityManager);
            repository.delete(annonceId);
            return null;
        });
    }

    public Annonce findById(Long annonceId) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.findById(annonceId);
        } finally {
            entityManager.close();
        }
    }

    public Annonce findByIdWithRelations(Long annonceId) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.findByIdWithRelations(annonceId);
        } finally {
            entityManager.close();
        }
    }

    public List<Annonce> listAnnonces(int page, int size) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.findAll(page, size);
        } finally {
            entityManager.close();
        }
    }

    public List<Annonce> searchAnnonces(String keyword, int page, int size) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            return repository.searchByKeyword(keyword, page, size);
        } finally {
            entityManager.close();
        }
    }

    public List<Annonce> filterAnnonces(Long categoryId, Annonce.Status status, int page, int size) {
        EntityManager entityManager = getEntityManager();
        try {
            AnnonceRepository repository = annonceRepository(entityManager);
            if (categoryId != null && status != null) {
                return repository.findByCategoryAndStatus(categoryId, status, page, size);
            }
            if (categoryId != null) {
                return repository.findByCategory(categoryId, page, size);
            }
            if (status != null) {
                return repository.findByStatus(status, page, size);
            }
            return repository.findAll(page, size);
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
}
