package com.example.todo.security.jaas;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.User;
import com.example.todo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbLoginModule implements LoginModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbLoginModule.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> options;
    private boolean loginSucceeded;
    private User authenticatedUser;
    private Set<java.security.Principal> addedPrincipals = new HashSet<>();

    @Override
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
        this.loginSucceeded = false;
        this.authenticatedUser = null;
        this.addedPrincipals = new HashSet<>();
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler missing");
        }

        NameCallback nameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (IOException | UnsupportedCallbackException exception) {
            throw new LoginException("Unable to read authentication callbacks");
        }

        String username = trimToNull(nameCallback.getName());
        char[] password = passwordCallback.getPassword();

        User user;
        try {
            if (username == null || password == null || password.length == 0) {
                throw new FailedLoginException("Invalid credentials");
            }
            user = authenticate(username, password);
        } finally {
            passwordCallback.clearPassword();
        }

        if (user == null) {
            LOGGER.warn("jaas_db_login_failed username={}", username);
            throw new FailedLoginException("Invalid credentials");
        }

        this.authenticatedUser = user;
        this.loginSucceeded = true;
        LOGGER.info("jaas_db_login_succeeded userId={} username={}", user.getId(), user.getUsername());
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded || authenticatedUser == null) {
            return false;
        }
        if (subject.isReadOnly()) {
            throw new LoginException("Subject is read-only");
        }

        UserPrincipal userPrincipal = new UserPrincipal(authenticatedUser.getUsername(), authenticatedUser.getId());
        addPrincipal(userPrincipal);
        addPrincipal(new RolePrincipal(DEFAULT_ROLE));
        if (isAdminUser(authenticatedUser.getUsername())) {
            addPrincipal(new RolePrincipal(ADMIN_ROLE));
        }
        return true;
    }

    @Override
    public boolean abort() {
        if (!loginSucceeded) {
            return false;
        }
        clearPrincipals();
        this.loginSucceeded = false;
        this.authenticatedUser = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        clearPrincipals();
        this.loginSucceeded = false;
        this.authenticatedUser = null;
        return true;
    }

    protected User authenticate(String username, char[] password) {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            UserRepository repository = new UserRepository(entityManager);
            User user = repository.findByUsername(username);
            if (user == null || user.getPassword() == null) {
                return null;
            }
            return BCrypt.checkpw(new String(password), user.getPassword()) ? user : null;
        } finally {
            entityManager.close();
        }
    }

    private void addPrincipal(java.security.Principal principal) {
        if (subject.getPrincipals().add(principal)) {
            addedPrincipals.add(principal);
        }
    }

    private boolean isAdminUser(String username) {
        Object raw = options == null ? null : options.get("adminUsers");
        if (!(raw instanceof String)) {
            return false;
        }
        String[] values = ((String) raw).split(",");
        for (String value : values) {
            if (username.equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private void clearPrincipals() {
        if (subject == null || subject.isReadOnly() || addedPrincipals.isEmpty()) {
            return;
        }
        subject.getPrincipals().removeAll(addedPrincipals);
        addedPrincipals.clear();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
