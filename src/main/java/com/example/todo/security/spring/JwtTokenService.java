package com.example.todo.security.spring;

import com.example.todo.auth.AuthSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secretBytes;
    private final long expirationSeconds;

    public JwtTokenService(ObjectMapper objectMapper,
                           @Value("${todo.security.jwt.secret:change-me-in-production}") String secret,
                           @Value("${todo.security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public AuthSession createSession(Long userId, String username, List<String> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        String token = generateToken(userId, username, roles, issuedAt, expiresAt);
        return new AuthSession(token, userId, username, issuedAt, expiresAt);
    }

    public Optional<JwtClaims> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        String signedData = parts[0] + "." + parts[1];
        if (!isValidSignature(signedData, parts[2])) {
            return Optional.empty();
        }
        try {
            JsonNode header = decodeJson(parts[0]);
            if (!"HS256".equals(header.path("alg").asText())) {
                return Optional.empty();
            }
            JsonNode payload = decodeJson(parts[1]);
            long expEpochSeconds = payload.path("exp").asLong(0L);
            if (expEpochSeconds <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }

            long userId = payload.path("userId").asLong(Long.MIN_VALUE);
            if (userId == Long.MIN_VALUE) {
                return Optional.empty();
            }
            String username = payload.path("sub").asText(null);
            if (username == null || username.isBlank()) {
                return Optional.empty();
            }
            List<String> roles = parseRoles(payload.path("roles"));
            long iatEpochSeconds = payload.path("iat").asLong(0L);
            Instant issuedAt = iatEpochSeconds > 0 ? Instant.ofEpochSecond(iatEpochSeconds) : null;
            Instant expiresAt = Instant.ofEpochSecond(expEpochSeconds);
            return Optional.of(new JwtClaims(userId, username, roles, issuedAt, expiresAt));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String generateToken(Long userId,
                                 String username,
                                 List<String> roles,
                                 Instant issuedAt,
                                 Instant expiresAt) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", username);
            payload.put("userId", userId);
            payload.put("roles", roles);
            payload.put("iat", issuedAt.getEpochSecond());
            payload.put("exp", expiresAt.getEpochSecond());

            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signedData = headerPart + "." + payloadPart;
            String signaturePart = sign(signedData);
            return signedData + "." + signaturePart;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate JWT token", exception);
        }
    }

    private String encodeJson(Map<String, Object> payload) throws Exception {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);
    }

    private JsonNode decodeJson(String encoded) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(encoded);
        return objectMapper.readTree(bytes);
    }

    private boolean isValidSignature(String signedData, String signature) {
        try {
            String expected = sign(signedData);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception exception) {
            return false;
        }
    }

    private String sign(String signedData) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretBytes, HMAC_ALGORITHM));
        byte[] signature = mac.doFinal(signedData.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }

    private List<String> parseRoles(JsonNode node) {
        List<String> roles = new ArrayList<>();
        if (!node.isArray()) {
            return roles;
        }
        for (JsonNode role : node) {
            String value = role.asText(null);
            if (value != null && !value.isBlank()) {
                roles.add(value);
            }
        }
        return roles;
    }

    public record JwtClaims(Long userId, String username, List<String> roles, Instant issuedAt, Instant expiresAt) {
    }
}
