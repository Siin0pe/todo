package com.example.todo.repository;

import com.example.todo.model.Annonce;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AnnonceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnonceRepository.class);

    private final EntityManager entityManager;

    public AnnonceRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Annonce create(Annonce annonce) {
        LOGGER.debug("annonce_repository_create");
        entityManager.persist(annonce);
        return annonce;
    }

    public Annonce findById(Long id) {
        Annonce annonce = entityManager.find(Annonce.class, id);
        LOGGER.debug("annonce_repository_find_by_id id={} found={}", id, annonce != null);
        return annonce;
    }

    public Annonce findByIdWithRelations(Long id) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a " +
                        "LEFT JOIN FETCH a.author " +
                        "LEFT JOIN FETCH a.category " +
                        "WHERE a.id = :id",
                Annonce.class);
        query.setParameter("id", id);
        List<Annonce> results = query.setMaxResults(1).getResultList();
        LOGGER.debug("annonce_repository_find_by_id_with_relations id={} found={}", id, !results.isEmpty());
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Annonce> findAll(int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a ORDER BY a.date DESC", Annonce.class);
        applyPagination(query, page, size);
        List<Annonce> results = query.getResultList();
        LOGGER.debug("annonce_repository_find_all page={} size={} returned={}", page, size, results.size());
        return results;
    }

    public List<Annonce> findAllWithRelations(int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a " +
                        "LEFT JOIN FETCH a.author " +
                        "LEFT JOIN FETCH a.category " +
                        "ORDER BY a.date DESC",
                Annonce.class);
        applyPagination(query, page, size);
        List<Annonce> results = query.getResultList();
        LOGGER.debug("annonce_repository_find_all_with_relations page={} size={} returned={}", page, size, results.size());
        return results;
    }

    public Annonce update(Annonce annonce) {
        LOGGER.debug("annonce_repository_update id={}", annonce.getId());
        return entityManager.merge(annonce);
    }

    public void delete(Long id) {
        Annonce annonce = entityManager.find(Annonce.class, id);
        if (annonce != null) {
            entityManager.remove(annonce);
            LOGGER.debug("annonce_repository_delete id={} deleted=true", id);
        } else {
            LOGGER.debug("annonce_repository_delete id={} deleted=false", id);
        }
    }

    public List<Annonce> searchByKeyword(String keyword, int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a " +
                        "WHERE LOWER(a.title) LIKE :kw OR LOWER(a.description) LIKE :kw " +
                        "ORDER BY a.date DESC",
                Annonce.class);
        query.setParameter("kw", normalizeKeyword(keyword));
        applyPagination(query, page, size);
        List<Annonce> results = query.getResultList();
        LOGGER.debug("annonce_repository_search_by_keyword page={} size={} returned={}", page, size, results.size());
        return results;
    }

    public List<Annonce> findByCategory(Long categoryId, int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a WHERE a.category.id = :categoryId ORDER BY a.date DESC",
                Annonce.class);
        query.setParameter("categoryId", categoryId);
        applyPagination(query, page, size);
        List<Annonce> results = query.getResultList();
        LOGGER.debug("annonce_repository_find_by_category categoryId={} page={} size={} returned={}",
                categoryId, page, size, results.size());
        return results;
    }

    public List<Annonce> findByStatus(Annonce.Status status, int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a WHERE a.status = :status ORDER BY a.date DESC",
                Annonce.class);
        query.setParameter("status", status);
        applyPagination(query, page, size);
        List<Annonce> results = query.getResultList();
        LOGGER.debug("annonce_repository_find_by_status status={} page={} size={} returned={}",
                status, page, size, results.size());
        return results;
    }

    public List<Annonce> findByCategoryAndStatus(Long categoryId, Annonce.Status status, int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a " +
                        "WHERE a.category.id = :categoryId AND a.status = :status " +
                        "ORDER BY a.date DESC",
                Annonce.class);
        query.setParameter("categoryId", categoryId);
        query.setParameter("status", status);
        applyPagination(query, page, size);
        List<Annonce> results = query.getResultList();
        LOGGER.debug("annonce_repository_find_by_category_and_status categoryId={} status={} page={} size={} returned={}",
                categoryId, status, page, size, results.size());
        return results;
    }

    private void applyPagination(TypedQuery<?> query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        query.setFirstResult(safePage * safeSize);
        query.setMaxResults(safeSize);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "%";
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }
}
