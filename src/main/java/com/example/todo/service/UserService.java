package com.example.todo.service;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.mindrot.jbcrypt.BCrypt;

import java.util.function.Function;

public class UserService {
    public User registerUser(String username, String email, String password) {
        return executeInTransaction(entityManager -> {
            UserRepository repository = userRepository(entityManager);
            if (repository.findByUsernameOrEmail(username, email) != null) {
                return null;
            }
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            return repository.create(user);
        });
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
