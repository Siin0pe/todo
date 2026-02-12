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

import java.util.function.Function;

public class UserService {
    public UserResponse register(RegisterRequest request) {
        User created = executeInTransaction(entityManager -> {
            UserRepository repository = userRepository(entityManager);
            if (repository.findByUsername(request.getUsername()) != null
                    || repository.findByEmail(request.getEmail()) != null) {
                throw new ConflictServiceException("Username or email already exists");
            }
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            return repository.create(user);
        });
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
