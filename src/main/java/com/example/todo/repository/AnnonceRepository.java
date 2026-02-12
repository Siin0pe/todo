package com.example.todo.repository;

import com.example.todo.model.Annonce;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class AnnonceRepository {
    private final EntityManager entityManager;

    public AnnonceRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Annonce create(Annonce annonce) {
        entityManager.persist(annonce);
        return annonce;
    }

    public Annonce findById(Long id) {
        return entityManager.find(Annonce.class, id);
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
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Annonce> findAll(int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a ORDER BY a.date DESC", Annonce.class);
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public List<Annonce> findAllWithRelations(int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a " +
                        "LEFT JOIN FETCH a.author " +
                        "LEFT JOIN FETCH a.category " +
                        "ORDER BY a.date DESC",
                Annonce.class);
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public Annonce update(Annonce annonce) {
        return entityManager.merge(annonce);
    }

    public void delete(Long id) {
        Annonce annonce = entityManager.find(Annonce.class, id);
        if (annonce != null) {
            entityManager.remove(annonce);
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
        return query.getResultList();
    }

    public List<Annonce> findByCategory(Long categoryId, int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a WHERE a.category.id = :categoryId ORDER BY a.date DESC",
                Annonce.class);
        query.setParameter("categoryId", categoryId);
        applyPagination(query, page, size);
        return query.getResultList();
    }

    public List<Annonce> findByStatus(Annonce.Status status, int page, int size) {
        TypedQuery<Annonce> query = entityManager.createQuery(
                "SELECT a FROM Annonce a WHERE a.status = :status ORDER BY a.date DESC",
                Annonce.class);
        query.setParameter("status", status);
        applyPagination(query, page, size);
        return query.getResultList();
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
        return query.getResultList();
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
