package com.example.todo.service;

import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.api.mapper.UserMapper;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.exception.ConflictServiceException;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse register(RegisterRequest request) {
        LOGGER.info("user_service_register_requested");
        if (userRepository.findByUsername(request.getUsername()).isPresent()
                || userRepository.findByEmail(request.getEmail()).isPresent()) {
            LOGGER.warn("user_service_register_conflict");
            throw new ConflictServiceException("Username or email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        User created = userRepository.save(user);

        LOGGER.info("user_service_register_succeeded userId={}", created.getId());
        return userMapper.toResponse(created);
    }
}
