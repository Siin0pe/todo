package com.example.todo.service;

import com.example.todo.auth.AuthSession;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.security.spring.JwtTokenService;
import com.example.todo.security.spring.UserRoleService;
import com.example.todo.service.exception.UnauthorizedServiceException;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRoleService userRoleService;

    public AuthService(UserRepository userRepository, JwtTokenService jwtTokenService, UserRoleService userRoleService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.userRoleService = userRoleService;
    }

    public AuthSession login(String usernameOrEmail, String password) {
        LOGGER.info("auth_service_login_requested");
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail).orElse(null);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            LOGGER.warn("auth_service_login_rejected");
            throw new UnauthorizedServiceException("Invalid credentials");
        }
        AuthSession session = jwtTokenService.createSession(
                user.getId(),
                user.getUsername(),
                userRoleService.resolveRoles(user.getUsername())
        );
        LOGGER.info("auth_service_login_succeeded userId={}", user.getId());
        return session;
    }
}
