package com.example.todo.security;

import com.example.todo.security.jaas.JaasConfigurationResolver;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class JaasBootstrapListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaasBootstrapListener.class);

    static final String JAAS_PROPERTY = "java.security.auth.login.config";
    static final String LOGIN_DOMAIN = "MasterAnnonceLogin";
    static final String TOKEN_DOMAIN = "MasterAnnonceToken";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JaasStatus status = verifyJaasConfiguration();
        if (!status.propertySet) {
            LOGGER.warn("jaas_config_property_missing property={}", JAAS_PROPERTY);
            return;
        }
        if (status.loaded) {
            LOGGER.info("jaas_config_loaded property={} location={} loginEntries={} tokenEntries={}",
                    JAAS_PROPERTY,
                    status.location,
                    status.loginEntryCount,
                    status.tokenEntryCount);
            return;
        }
        LOGGER.warn("jaas_config_incomplete property={} location={} loginEntries={} tokenEntries={} error={}",
                JAAS_PROPERTY,
                status.location,
                status.loginEntryCount,
                status.tokenEntryCount,
                status.error);
    }

    static JaasStatus verifyJaasConfiguration() {
        String location = System.getProperty(JAAS_PROPERTY);
        boolean propertySet = location != null && !location.trim().isEmpty();
        int loginEntries = 0;
        int tokenEntries = 0;
        String error = null;

        try {
            Configuration configuration = JaasConfigurationResolver.resolve(LOGIN_DOMAIN, TOKEN_DOMAIN);
            configuration.refresh();
            loginEntries = count(configuration.getAppConfigurationEntry(LOGIN_DOMAIN));
            tokenEntries = count(configuration.getAppConfigurationEntry(TOKEN_DOMAIN));
        } catch (RuntimeException exception) {
            error = exception.getMessage();
        }

        boolean loaded = propertySet && loginEntries > 0 && tokenEntries > 0;
        return new JaasStatus(propertySet, location, loginEntries, tokenEntries, loaded, error);
    }

    private static int count(AppConfigurationEntry[] entries) {
        return entries == null ? 0 : entries.length;
    }

    static final class JaasStatus {
        private final boolean propertySet;
        private final String location;
        private final int loginEntryCount;
        private final int tokenEntryCount;
        private final boolean loaded;
        private final String error;

        JaasStatus(boolean propertySet,
                   String location,
                   int loginEntryCount,
                   int tokenEntryCount,
                   boolean loaded,
                   String error) {
            this.propertySet = propertySet;
            this.location = location;
            this.loginEntryCount = loginEntryCount;
            this.tokenEntryCount = tokenEntryCount;
            this.loaded = loaded;
            this.error = error;
        }

        boolean isPropertySet() {
            return propertySet;
        }

        int getLoginEntryCount() {
            return loginEntryCount;
        }

        int getTokenEntryCount() {
            return tokenEntryCount;
        }

        boolean isLoaded() {
            return loaded;
        }
    }
}
