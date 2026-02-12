package com.example.todo.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JaasBootstrapListenerUnitTest {

    private String originalJaasProperty;

    @AfterEach
    void restoreSystemProperty() {
        if (originalJaasProperty == null) {
            System.clearProperty(JaasBootstrapListener.JAAS_PROPERTY);
            return;
        }
        System.setProperty(JaasBootstrapListener.JAAS_PROPERTY, originalJaasProperty);
    }

    @Test
    void verifyJaasConfiguration_loadsBothMasterAnnonceDomains() throws IOException {
        originalJaasProperty = System.getProperty(JaasBootstrapListener.JAAS_PROPERTY);

        Path file = Files.createTempFile("jaas-test-", ".conf");
        Files.write(file, (
                "MasterAnnonceLogin {\n" +
                "  com.example.todo.security.jaas.DbLoginModule required debug=true;\n" +
                "};\n" +
                "MasterAnnonceToken {\n" +
                "  com.example.todo.security.jaas.TokenLoginModule required debug=true;\n" +
                "};\n").getBytes(StandardCharsets.UTF_8));

        System.setProperty(JaasBootstrapListener.JAAS_PROPERTY, file.toAbsolutePath().toString());

        JaasBootstrapListener.JaasStatus status = JaasBootstrapListener.verifyJaasConfiguration();
        assertTrue(status.isPropertySet());
        assertEquals(1, status.getLoginEntryCount());
        assertEquals(1, status.getTokenEntryCount());
        assertTrue(status.isLoaded());
    }
}
