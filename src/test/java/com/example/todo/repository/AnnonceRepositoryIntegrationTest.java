package com.example.todo.repository;

import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnonceRepositoryIntegrationTest extends RepositoryIntegrationTestBase {

    @Test
    void crudOperationsPersistAndDeleteAnnonce() {
        UserRepository userRepository = new UserRepository(entityManager);
        CategoryRepository categoryRepository = new CategoryRepository(entityManager);
        AnnonceRepository annonceRepository = new AnnonceRepository(entityManager);

        User author = callInTransaction(() -> userRepository.create(buildUser("alice", "alice@example.com")));
        Category category = callInTransaction(() -> categoryRepository.create(buildCategory("Jobs")));

        Annonce annonce = buildAnnonce("Title A", "Desc A", author, category, new Timestamp(System.currentTimeMillis()));

        Annonce persisted = callInTransaction(() -> annonceRepository.create(annonce));
        assertNotNull(persisted.getId());

        Annonce found = annonceRepository.findById(persisted.getId());
        assertNotNull(found);
        assertEquals("Title A", found.getTitle());

        found.setTitle("Title Updated");
        callInTransaction(() -> annonceRepository.update(found));

        Annonce updated = annonceRepository.findById(persisted.getId());
        assertEquals("Title Updated", updated.getTitle());

        callInTransaction(() -> {
            annonceRepository.delete(persisted.getId());
            return null;
        });

        assertNull(annonceRepository.findById(persisted.getId()));
    }

    @Test
    void searchFiltersAndPaginationReturnExpectedAnnonces() {
        UserRepository userRepository = new UserRepository(entityManager);
        CategoryRepository categoryRepository = new CategoryRepository(entityManager);
        AnnonceRepository annonceRepository = new AnnonceRepository(entityManager);

        User author = callInTransaction(() -> userRepository.create(buildUser("bob", "bob@example.com")));
        Category jobs = callInTransaction(() -> categoryRepository.create(buildCategory("Jobs")));
        Category services = callInTransaction(() -> categoryRepository.create(buildCategory("Services")));

        long baseTime = System.currentTimeMillis();
        callInTransaction(() -> {
            annonceRepository.create(buildAnnonce("Alpha", "First offer", author, jobs, new Timestamp(baseTime - 1000), Annonce.Status.PUBLISHED));
            annonceRepository.create(buildAnnonce("Beta", "Second offer", author, jobs, new Timestamp(baseTime - 2000), Annonce.Status.DRAFT));
            annonceRepository.create(buildAnnonce("Gamma", "Service offer", author, services, new Timestamp(baseTime - 3000), Annonce.Status.PUBLISHED));
            return null;
        });

        List<Annonce> page0 = annonceRepository.findAll(0, 2);
        assertEquals(2, page0.size());
        assertEquals("Alpha", page0.get(0).getTitle());
        assertEquals("Beta", page0.get(1).getTitle());

        List<Annonce> page1 = annonceRepository.findAll(1, 2);
        assertEquals(1, page1.size());
        assertEquals("Gamma", page1.get(0).getTitle());

        List<Annonce> search = annonceRepository.searchByKeyword("service", 0, 10);
        assertEquals(1, search.size());
        assertEquals("Gamma", search.get(0).getTitle());

        List<Annonce> byCategory = annonceRepository.findByCategory(jobs.getId(), 0, 10);
        assertEquals(2, byCategory.size());

        List<Annonce> byStatus = annonceRepository.findByStatus(Annonce.Status.PUBLISHED, 0, 10);
        assertEquals(2, byStatus.size());

        List<Annonce> byCategoryAndStatus = annonceRepository.findByCategoryAndStatus(jobs.getId(), Annonce.Status.PUBLISHED, 0, 10);
        assertEquals(1, byCategoryAndStatus.size());
        assertEquals("Alpha", byCategoryAndStatus.get(0).getTitle());

        assertTrue(annonceRepository.searchByKeyword("missing", 0, 10).isEmpty());
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secret");
        return user;
    }

    private Category buildCategory(String label) {
        Category category = new Category();
        category.setLabel(label);
        return category;
    }

    private Annonce buildAnnonce(String title, String description, User author, Category category, Timestamp date) {
        return buildAnnonce(title, description, author, category, date, Annonce.Status.DRAFT);
    }

    private Annonce buildAnnonce(String title, String description, User author, Category category, Timestamp date, Annonce.Status status) {
        Annonce annonce = new Annonce();
        annonce.setTitle(title);
        annonce.setDescription(description);
        annonce.setAdress("Street 1");
        annonce.setMail("contact@example.com");
        annonce.setDate(date);
        annonce.setStatus(status);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        return annonce;
    }
}
