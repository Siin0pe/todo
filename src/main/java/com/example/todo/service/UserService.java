package com.example.todo.service;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.api.mapper.UserMapper;
import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.exception.ConflictServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserResponse register(RegisterRequest request) {
        LOGGER.info("user_service_register_requested");
        User created = executeInTransaction(entityManager -> {
            UserRepository repository = userRepository(entityManager);
            if (repository.findByUsername(request.getUsername()) != null
                    || repository.findByEmail(request.getEmail()) != null) {
                LOGGER.warn("user_service_register_conflict");
                throw new ConflictServiceException("Username or email already exists");
            }
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            return repository.create(user);
        });
        LOGGER.info("user_service_register_succeeded userId={}", created.getId());
        return UserMapper.toResponse(created);
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
                LOGGER.warn("user_service_transaction_rollback reason={}", e.getClass().getSimpleName());
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

    protected UserRepository userRepository(EntityManager entityManager) {
        return new UserRepository(entityManager);
    }
}
