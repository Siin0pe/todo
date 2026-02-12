package com.example.todo.test;

import com.example.todo.db.EntityManagerUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public final class TestDatabase {
    private static final String TEST_PERSISTENCE_UNIT = "test";
    private static volatile boolean initialized = false;
    private static TestDataLoader.TestData data;

    private TestDatabase() {
    }

    public static synchronized TestDataLoader.TestData init() {
        System.setProperty("todo.persistence.unit", TEST_PERSISTENCE_UNIT);
        if (initialized) {
            return data;
        }
        return reset();
    }

    public static synchronized TestDataLoader.TestData reset() {
        System.setProperty("todo.persistence.unit", TEST_PERSISTENCE_UNIT);
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            clear(entityManager);
            data = TestDataLoader.load(entityManager);
            transaction.commit();
            initialized = true;
            return data;
        } catch (RuntimeException ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    private static void clear(EntityManager entityManager) {
        entityManager.createNativeQuery("DELETE FROM annonce").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM app_user").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM category").executeUpdate();
    }

    public static void shutdown() {
        EntityManagerUtil.close();
        initialized = false;
        data = null;
        System.clearProperty("todo.persistence.unit");
    }
}
