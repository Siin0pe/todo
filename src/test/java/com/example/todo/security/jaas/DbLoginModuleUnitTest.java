package com.example.todo.security.jaas;

import com.example.todo.model.User;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbLoginModuleUnitTest {

    @Test
    void commit_addsUserAndRolePrincipalsOnSuccessfulLogin() throws Exception {
        Subject subject = new Subject();
        DbLoginModule module = new StubDbLoginModule(buildUser(7L, "alice"));
        module.initialize(
                subject,
                callbackHandler("alice", "secret"),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        module.login();
        module.commit();

        assertTrue(subject.getPrincipals().contains(new UserPrincipal("alice", 7L)));
        assertTrue(subject.getPrincipals().contains(new RolePrincipal("ROLE_USER")));
    }

    @Test
    void commit_addsAdminRoleWhenConfiguredInOptions() throws Exception {
        Subject subject = new Subject();
        Map<String, Object> options = new HashMap<>();
        options.put("adminUsers", "alice,bob");

        DbLoginModule module = new StubDbLoginModule(buildUser(8L, "alice"));
        module.initialize(
                subject,
                callbackHandler("alice", "secret"),
                Collections.emptyMap(),
                options
        );

        module.login();
        module.commit();

        assertTrue(subject.getPrincipals().contains(new RolePrincipal("ROLE_ADMIN")));
    }

    @Test
    void login_failsWhenCredentialsAreInvalid() {
        Subject subject = new Subject();
        DbLoginModule module = new StubDbLoginModule(null);
        module.initialize(
                subject,
                callbackHandler("alice", "bad"),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertThrows(FailedLoginException.class, module::login);
    }

    private static CallbackHandler callbackHandler(String username, String password) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                }
            }
        };
    }

    private static User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("ignored-in-stub");
        return user;
    }

    private static final class StubDbLoginModule extends DbLoginModule {
        private final User user;

        private StubDbLoginModule(User user) {
            this.user = user;
        }

        @Override
        protected User authenticate(String username, char[] password) {
            return user;
        }
    }
}
