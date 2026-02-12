package com.example.todo.api.rest;

import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.LoginResponse;
import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.api.security.AuthFilter;
import com.example.todo.model.Category;
import com.example.todo.test.TestDatabase;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeEach;

import java.net.URL;
import java.nio.file.Paths;

public abstract class RestIntegrationTestBase extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig()
                .packages("com.example.todo.api")
                .register(AuthFilter.class)
                .register(JsonBindingFeature.class)
                .register(ValidationFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    }

    @BeforeEach
    void resetDatabase() {
        ensureJaasConfigProperty();
        TestDatabase.reset();
    }

    private void ensureJaasConfigProperty() {
        String property = System.getProperty("java.security.auth.login.config");
        if (property != null && !property.trim().isEmpty()) {
            return;
        }
        URL resource = Thread.currentThread().getContextClassLoader().getResource("jaas.conf");
        if (resource != null) {
            try {
                System.setProperty("java.security.auth.login.config", Paths.get(resource.toURI()).toString());
            } catch (Exception ignored) {
                System.setProperty("java.security.auth.login.config", resource.getPath());
            }
        }
    }

    protected UserResponse registerUser(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);

        Response response = target("register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new IllegalStateException("Registration failed with status " + response.getStatus());
        }
        return response.readEntity(UserResponse.class);
    }

    protected String loginToken(String login, String password) {
        LoginRequest request = new LoginRequest();
        request.setLogin(login);
        request.setPassword(password);

        Response response = target("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new IllegalStateException("Login failed with status " + response.getStatus());
        }
        LoginResponse loginResponse = response.readEntity(LoginResponse.class);
        if (loginResponse == null || loginResponse.getToken() == null || loginResponse.getToken().trim().isEmpty()) {
            throw new IllegalStateException("Login returned an empty token");
        }
        return loginResponse.getToken();
    }

    protected Long findCategoryId(String label) {
        EntityManager entityManager = com.example.todo.db.EntityManagerUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "SELECT c FROM Category c WHERE c.label = :label", Category.class)
                    .setParameter("label", label)
                    .setMaxResults(1)
                    .getSingleResult()
                    .getId();
        } finally {
            entityManager.close();
        }
    }
}
