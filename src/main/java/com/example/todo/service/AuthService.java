package com.example.todo.service;

import com.example.todo.auth.AuthSession;
import com.example.todo.auth.TokenStore;
import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthSession login(String login, String password) {
        LOGGER.info("auth_service_login_requested");
        EntityManager entityManager = getEntityManager();
        try {
            User user = findUser(entityManager, login);
            if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                LOGGER.warn("auth_service_login_rejected");
                throw new UnauthorizedServiceException("Invalid credentials");
            }
            LOGGER.info("auth_service_login_succeeded userId={}", user.getId());
            return TokenStore.createSession(user);
        } finally {
            entityManager.close();
        }
    }

    protected EntityManager getEntityManager() {
        return EntityManagerUtil.getEntityManager();
    }

    private User findUser(EntityManager entityManager, String login) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :login OR u.email = :login",
                User.class);
        query.setParameter("login", login);
        List<User> results = query.setMaxResults(1).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
