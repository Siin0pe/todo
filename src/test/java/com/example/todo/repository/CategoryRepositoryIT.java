package com.example.todo.repository;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Category;
import com.example.todo.test.TestDatabase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryRepositoryIT {

    @BeforeEach
    void setUp() {
        TestDatabase.reset();
    }

    @Test
    void findAll_appliesPaginationAndOrdering() {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            CategoryRepository repository = new CategoryRepository(entityManager);
            List<Category> page = repository.findAll(1, 2);

            assertEquals(2, page.size());
            assertEquals("Category C", page.get(0).getLabel());
            assertEquals("Category D", page.get(1).getLabel());
        } finally {
            entityManager.close();
        }
    }
}
