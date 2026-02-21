package com.example.todo.security.spring;

import com.example.todo.service.exception.UnauthorizedServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedServiceException("Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthPrincipal)) {
            throw new UnauthorizedServiceException("Unauthorized");
        }
        return ((AuthPrincipal) principal).getUserId();
    }
}
