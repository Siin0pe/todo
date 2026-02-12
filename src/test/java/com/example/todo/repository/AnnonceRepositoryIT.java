package com.example.todo.repository;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Annonce;
import com.example.todo.test.TestDatabase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnnonceRepositoryIT {

    @BeforeEach
    void setUp() {
        TestDatabase.reset();
    }

    @Test
    void findAllWithRelations_appliesPaginationAndOrdering() {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            AnnonceRepository repository = new AnnonceRepository(entityManager);
            List<Annonce> page = repository.findAllWithRelations(0, 5);

            assertEquals(5, page.size());
            assertNotNull(page.get(0).getAuthor());
            assertNotNull(page.get(0).getCategory());
            assertEquals("Annonce 30", page.get(0).getTitle());
            assertEquals("Annonce 26", page.get(4).getTitle());
        } finally {
            entityManager.close();
        }
    }

    @Test
    void findByStatus_appliesPagination() {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            AnnonceRepository repository = new AnnonceRepository(entityManager);
            List<Annonce> page = repository.findByStatus(Annonce.Status.PUBLISHED, 1, 4);

            assertEquals(4, page.size());
            assertEquals("Annonce 22", page.get(0).getTitle());
            assertEquals("Annonce 16", page.get(3).getTitle());
        } finally {
            entityManager.close();
        }
    }
}
