package com.example.todo.security.jaas;

import com.example.todo.auth.AuthSession;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.FailedLoginException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenLoginModuleUnitTest {

    @Test
    void commit_addsIdentityAndRolesForValidToken() throws Exception {
        Subject subject = new Subject();
        Map<String, Object> options = new HashMap<>();
        options.put("adminUsers", "alice,bob");
        AuthSession session = new AuthSession("token-1", 4L, "alice", Instant.now(), Instant.now().plusSeconds(3600));

        TokenLoginModule module = new StubTokenLoginModule(session);
        module.initialize(
                subject,
                tokenCallbackHandler("token-1"),
                Collections.emptyMap(),
                options
        );

        module.login();
        module.commit();

        assertTrue(subject.getPrincipals().contains(new UserPrincipal("alice", 4L)));
        assertTrue(subject.getPrincipals().contains(new RolePrincipal("ROLE_USER")));
        assertTrue(subject.getPrincipals().contains(new RolePrincipal("ROLE_ADMIN")));
    }

    @Test
    void login_failsForInvalidToken() {
        Subject subject = new Subject();
        TokenLoginModule module = new StubTokenLoginModule(null);
        module.initialize(
                subject,
                tokenCallbackHandler("invalid"),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertThrows(FailedLoginException.class, module::login);
    }

    @Test
    void login_failsForExpiredToken() {
        Subject subject = new Subject();
        AuthSession expired = new AuthSession("token-2", 7L, "eve", Instant.now().minusSeconds(3600), Instant.now().minusSeconds(1));
        TokenLoginModule module = new StubTokenLoginModule(expired);
        module.initialize(
                subject,
                tokenCallbackHandler("token-2"),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertThrows(FailedLoginException.class, module::login);
    }

    private static CallbackHandler tokenCallbackHandler(String token) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof TokenCallback) {
                    ((TokenCallback) callback).setToken(token);
                } else if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(token);
                }
            }
        };
    }

    private static final class StubTokenLoginModule extends TokenLoginModule {
        private final AuthSession session;

        private StubTokenLoginModule(AuthSession session) {
            this.session = session;
        }

        @Override
        protected AuthSession resolveSession(String token) {
            if (session == null) {
                return null;
            }
            return session.isExpired(Instant.now()) ? null : session;
        }
    }
}
