package com.example.todo.repository;

import com.example.todo.model.User;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryIntegrationTest extends RepositoryIntegrationTestBase {

    @Test
    void crudOperationsPersistAndDeleteUser() {
        UserRepository repository = new UserRepository(entityManager);

        User created = new User();
        created.setUsername("alice");
        created.setEmail("alice@example.com");
        created.setPassword("secret");

        User persisted = callInTransaction(() -> repository.create(created));
        assertNotNull(persisted.getId());

        User found = repository.findById(persisted.getId());
        assertNotNull(found);
        assertEquals("alice", found.getUsername());

        found.setEmail("alice.updated@example.com");
        callInTransaction(() -> repository.update(found));

        User updated = repository.findById(persisted.getId());
        assertEquals("alice.updated@example.com", updated.getEmail());

        callInTransaction(() -> {
            repository.delete(persisted.getId());
            return null;
        });

        assertNull(repository.findById(persisted.getId()));
    }

    @Test
    void searchAndPaginationReturnExpectedUsers() {
        UserRepository repository = new UserRepository(entityManager);
        long baseTime = System.currentTimeMillis();

        callInTransaction(() -> {
            repository.create(buildUser("zoe", "zoe@example.com", baseTime - 1000));
            repository.create(buildUser("bob", "bob@example.com", baseTime - 2000));
            repository.create(buildUser("alice", "alice@example.com", baseTime - 3000));
            return null;
        });

        List<User> page0 = repository.findAll(0, 2);
        assertEquals(2, page0.size());
        assertEquals("zoe", page0.get(0).getUsername());
        assertEquals("bob", page0.get(1).getUsername());

        List<User> page1 = repository.findAll(1, 2);
        assertEquals(1, page1.size());
        assertEquals("alice", page1.get(0).getUsername());

        List<User> search = repository.searchByKeyword("bob", 0, 10);
        assertEquals(1, search.size());
        assertEquals("bob", search.get(0).getUsername());

        User byLogin = repository.findByLogin("ALICE");
        assertNotNull(byLogin);
        assertEquals("alice", byLogin.getUsername());

        User byUsernameOrEmail = repository.findByUsernameOrEmail("unknown", "zoe@example.com");
        assertNotNull(byUsernameOrEmail);
        assertEquals("zoe", byUsernameOrEmail.getUsername());

        assertTrue(repository.searchByKeyword("missing", 0, 10).isEmpty());
    }

    private User buildUser(String username, String email, long createdAtMillis) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secret");
        user.setCreatedAt(new Timestamp(createdAtMillis));
        return user;
    }
}
