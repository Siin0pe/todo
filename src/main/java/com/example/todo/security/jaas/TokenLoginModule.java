package com.example.todo.security.jaas;

import com.example.todo.auth.AuthSession;
import com.example.todo.auth.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenLoginModule implements LoginModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenLoginModule.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> options;
    private boolean loginSucceeded;
    private AuthSession session;
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
        this.session = null;
        this.addedPrincipals = new HashSet<>();
    }

    @Override
    public boolean login() throws LoginException {
        String token = readTokenFromCallbacks();
        if (token == null || token.trim().isEmpty()) {
            throw new FailedLoginException("Invalid token");
        }
        AuthSession resolved = resolveSession(token.trim());
        if (resolved == null) {
            LOGGER.warn("jaas_token_login_failed");
            throw new FailedLoginException("Invalid token");
        }
        this.session = resolved;
        this.loginSucceeded = true;
        LOGGER.info("jaas_token_login_succeeded userId={}", resolved.getUserId());
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded || session == null) {
            return false;
        }
        if (subject.isReadOnly()) {
            throw new LoginException("Subject is read-only");
        }
        addPrincipal(new UserPrincipal(session.getUsername(), session.getUserId()));
        addPrincipal(new RolePrincipal(DEFAULT_ROLE));
        if (isAdminUser(session.getUsername())) {
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
        loginSucceeded = false;
        session = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        clearPrincipals();
        loginSucceeded = false;
        session = null;
        return true;
    }

    protected AuthSession resolveSession(String token) {
        return TokenStore.getSession(token);
    }

    private String readTokenFromCallbacks() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler missing");
        }
        TokenCallback tokenCallback = new TokenCallback();
        try {
            callbackHandler.handle(new Callback[]{tokenCallback});
            String token = tokenCallback.getToken();
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
        } catch (UnsupportedCallbackException ignored) {
            // Fallback for handlers that only support NameCallback.
        } catch (IOException exception) {
            throw new LoginException("Unable to read token callback");
        }

        NameCallback nameCallback = new NameCallback("token");
        try {
            callbackHandler.handle(new Callback[]{nameCallback});
        } catch (IOException | UnsupportedCallbackException exception) {
            throw new LoginException("Unable to read token callback");
        }
        return nameCallback.getName();
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
}
