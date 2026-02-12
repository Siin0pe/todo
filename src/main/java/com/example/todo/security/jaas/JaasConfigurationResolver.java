package com.example.todo.security.jaas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.URIParameter;

public final class JaasConfigurationResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaasConfigurationResolver.class);
    private static final String JAAS_RESOURCE = "jaas.conf";

    private JaasConfigurationResolver() {
    }

    public static Configuration resolve(String... requiredDomains) {
        Configuration global = Configuration.getConfiguration();
        if (containsDomains(global, requiredDomains)) {
            return global;
        }
        Configuration classpath = loadFromClasspath();
        if (classpath != null && containsDomains(classpath, requiredDomains)) {
            LOGGER.info("jaas_config_resolved_from_classpath resource={}", JAAS_RESOURCE);
            return classpath;
        }
        return global;
    }

    private static boolean containsDomains(Configuration configuration, String... domains) {
        if (configuration == null || domains == null) {
            return false;
        }
        for (String domain : domains) {
            AppConfigurationEntry[] entries = configuration.getAppConfigurationEntry(domain);
            if (entries == null || entries.length == 0) {
                return false;
            }
        }
        return true;
    }

    private static Configuration loadFromClasspath() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(JAAS_RESOURCE);
        if (resource == null) {
            return null;
        }
        try {
            URI uri = resource.toURI();
            return Configuration.getInstance("JavaLoginConfig", new URIParameter(uri));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JavaLoginConfig algorithm not available", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load JAAS config from classpath", exception);
        }
    }
}
