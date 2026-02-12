package com.example.todo.service;

import com.example.todo.auth.AuthSession;
import com.example.todo.auth.TokenStore;
import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.service.exception.UnauthorizedServiceException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class AuthService {
    public AuthSession login(String login, String password) {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            User user = findUser(entityManager, login);
            if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                throw new UnauthorizedServiceException("Invalid credentials");
            }
            return TokenStore.createSession(user);
        } finally {
            entityManager.close();
        }
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
