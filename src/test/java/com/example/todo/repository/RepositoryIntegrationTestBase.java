package com.example.todo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.function.Supplier;

public abstract class RepositoryIntegrationTestBase {
    protected static EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;

    @BeforeAll
    static void setUpEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
    }

    @AfterAll
    static void tearDownEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void setUpEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();
        clearDatabase();
    }

    @AfterEach
    void tearDownEntityManager() {
        if (entityManager != null) {
            entityManager.close();
        }
    }

    protected void runInTransaction(Runnable work) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        work.run();
        transaction.commit();
    }

    protected <T> T callInTransaction(Supplier<T> work) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        T result = work.get();
        transaction.commit();
        return result;
    }

    private void clearDatabase() {
        runInTransaction(() -> {
            entityManager.createQuery("DELETE FROM Annonce").executeUpdate();
            entityManager.createQuery("DELETE FROM Category").executeUpdate();
            entityManager.createQuery("DELETE FROM User").executeUpdate();
        });
    }
}
