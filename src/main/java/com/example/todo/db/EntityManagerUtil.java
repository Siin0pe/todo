package com.example.todo.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EntityManagerUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerUtil.class);
    private static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
    private static final String PERSISTENCE_UNIT_PROPERTY = "todo.persistence.unit";
    private static volatile EntityManagerFactory entityManagerFactory;

    private EntityManagerUtil() {
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            LOGGER.info("entity_manager_factory_closing");
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }

    private static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory localFactory = entityManagerFactory;
        if (localFactory == null) {
            synchronized (EntityManagerUtil.class) {
                localFactory = entityManagerFactory;
                if (localFactory == null) {
                    String persistenceUnitName = System.getProperty(
                            PERSISTENCE_UNIT_PROPERTY,
                            DEFAULT_PERSISTENCE_UNIT_NAME
                    );
                    LOGGER.info("entity_manager_factory_initializing persistenceUnit={}", persistenceUnitName);
                    localFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
                    entityManagerFactory = localFactory;
                    LOGGER.info("entity_manager_factory_initialized persistenceUnit={}", persistenceUnitName);
                }
            }
        }
        return localFactory;
    }
}
