package com.example.todo.service;

import com.example.todo.auth.AuthSession;
import com.example.todo.auth.TokenStore;
import com.example.todo.security.jaas.JaasConfigurationResolver;
import com.example.todo.security.jaas.UserPrincipal;
import com.example.todo.service.exception.UnauthorizedServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Set;

public class JaasAuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaasAuthService.class);
    private static final String LOGIN_DOMAIN = "MasterAnnonceLogin";

    public AuthSession login(String username, String password) {
        LOGGER.info("jaas_auth_login_requested username={}", username);
        LoginContext loginContext = createLoginContext(username, password);
        try {
            loginContext.login();
            Subject subject = loginContext.getSubject();
            UserPrincipal principal = resolveUserPrincipal(subject);
            AuthSession session = TokenStore.createSession(principal.getUserId(), principal.getName());
            LOGGER.info("jaas_auth_login_succeeded userId={}", session.getUserId());
            return session;
        } catch (LoginException exception) {
            LOGGER.warn("jaas_auth_login_rejected username={}", username);
            throw new UnauthorizedServiceException("Invalid credentials");
        } finally {
            try {
                loginContext.logout();
            } catch (LoginException ignored) {
                LOGGER.debug("jaas_auth_logout_ignored");
            }
        }
    }

    protected LoginContext createLoginContext(String username, String password) {
        try {
            return new LoginContext(
                    LOGIN_DOMAIN,
                    null,
                    new UsernamePasswordCallbackHandler(username, password),
                    JaasConfigurationResolver.resolve(LOGIN_DOMAIN)
            );
        } catch (LoginException exception) {
            throw new IllegalStateException("Unable to initialize JAAS LoginContext", exception);
        }
    }

    private UserPrincipal resolveUserPrincipal(Subject subject) throws LoginException {
        if (subject == null) {
            throw new LoginException("Missing subject");
        }
        Set<UserPrincipal> principals = subject.getPrincipals(UserPrincipal.class);
        if (principals == null || principals.isEmpty()) {
            throw new LoginException("Missing user principal");
        }
        return principals.iterator().next();
    }

    private static final class UsernamePasswordCallbackHandler implements CallbackHandler {
        private final String username;
        private final String password;

        private UsernamePasswordCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    char[] passwordChars = password == null ? new char[0] : password.toCharArray();
                    ((PasswordCallback) callback).setPassword(passwordChars);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }
}
