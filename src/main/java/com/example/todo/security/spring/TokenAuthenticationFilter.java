package com.example.todo.security.spring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final JwtTokenService jwtTokenService;

    public TokenAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && !token.isBlank()) {
            JwtTokenService.JwtClaims claims = jwtTokenService.parse(token.trim()).orElse(null);
            if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AuthPrincipal principal = new AuthPrincipal(claims.userId(), claims.username());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        resolveAuthorities(claims.roles())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> resolveAuthorities(List<String> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles == null || roles.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(DEFAULT_ROLE));
            return authorities;
        }
        for (String role : roles) {
            if (role != null && !role.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(DEFAULT_ROLE));
        }
        return authorities;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            String trimmed = authHeader.trim();
            int separator = trimmed.indexOf(' ');
            if (separator > 0) {
                String scheme = trimmed.substring(0, separator).toLowerCase(Locale.ROOT);
                if ("bearer".equals(scheme) || "token".equals(scheme)) {
                    return trimmed.substring(separator + 1).trim();
                }
            }
            return trimmed;
        }
        return request.getHeader("X-Auth-Token");
    }
}
