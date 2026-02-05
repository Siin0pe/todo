package com.example.todo.service;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    public User authenticate(String login, String password) {
        EntityManager entityManager = getEntityManager();
        try {
            UserRepository repository = userRepository(entityManager);
            User user = repository.findByLogin(login);
            if (user == null || password == null) {
                return null;
            }
            return BCrypt.checkpw(password, user.getPassword()) ? user : null;
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
