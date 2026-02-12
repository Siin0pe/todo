package com.example.todo.api;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiDocumentationUnitTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig()
                .packages("com.example.todo.api", "io.swagger.v3.jaxrs2.integration.resources")
                .register(JsonBindingFeature.class);
    }

    @Test
    void openApiJson_containsAllPublicPaths() {
        String content = target("openapi.json")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertTrue(content.contains("\"/annonces\""));
        assertTrue(content.contains("\"/categories\""));
        assertTrue(content.contains("\"/login\""));
        assertTrue(content.contains("\"/register\""));
    }

    @Test
    void openApiEndpoint_isAccessible() {
        int status = target("openapi")
                .request(MediaType.APPLICATION_JSON)
                .get()
                .getStatus();

        assertEquals(200, status);
    }
}
