package com.example.todo.repository;

import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.service.AnnonceService;
import com.example.todo.service.CategoryService;
import com.example.todo.service.UserService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Persistence;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessFlowIntegrationTest extends RepositoryIntegrationTestBase {

    @Test
    void creationPublicationSearchFlowWorks() {
        UserService userService = new UserService();
        CategoryService categoryService = new CategoryService();
        AnnonceService annonceService = new AnnonceService();

        User author = userService.registerUser("flow-user", "flow@example.com", "secret");
        Category category = categoryService.createCategory("FlowCategory");

        assertNotNull(author);
        assertNotNull(category);

        Annonce created = annonceService.createAnnonce(
                "Flow Title",
                "Flow Description",
                "Street 1",
                "mail@example.com",
                author.getId(),
                category.getId());
        assertNotNull(created);
        assertNotNull(created.getId());

        Annonce published = annonceService.publishAnnonce(created.getId());
        assertNotNull(published);
        assertEquals(Annonce.Status.PUBLISHED, published.getStatus());

        List<Annonce> results = annonceService.searchAnnonces("flow title", 0, 10);
        assertTrue(results.stream().anyMatch(a -> a.getId().equals(created.getId())));
    }

    @Test
    void lazyLoadingTriggersExtraQuery() {
        UserRepository userRepository = new UserRepository(entityManager);
        CategoryRepository categoryRepository = new CategoryRepository(entityManager);
        AnnonceRepository annonceRepository = new AnnonceRepository(entityManager);

        User author = callInTransaction(() -> userRepository.create(buildUser("lazy-user", "lazy@example.com")));
        Category category = callInTransaction(() -> categoryRepository.create(buildCategory("LazyCategory")));
        callInTransaction(() -> {
            annonceRepository.create(buildAnnonce("Lazy", "Lazy desc", author, category));
            return null;
        });

        jakarta.persistence.EntityManager freshEntityManager = entityManagerFactory.createEntityManager();
        try {
            CategoryRepository freshRepository = new CategoryRepository(freshEntityManager);
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics statistics = sessionFactory.getStatistics();
            statistics.setStatisticsEnabled(true);
            statistics.clear();

            Category loaded = freshRepository.findById(category.getId());
            assertTrue(!Persistence.getPersistenceUtil().isLoaded(loaded, "annonces"));

            long before = statistics.getPrepareStatementCount();
            loaded.getAnnonces().size();
            long after = statistics.getPrepareStatementCount();

            assertTrue(Persistence.getPersistenceUtil().isLoaded(loaded, "annonces"));
            assertTrue(after > before);
        } finally {
            freshEntityManager.close();
        }
    }

    @Test
    void nPlusOneOccursWhenAccessingAnnoncesPerCategory() {
        UserRepository userRepository = new UserRepository(entityManager);
        CategoryRepository categoryRepository = new CategoryRepository(entityManager);
        AnnonceRepository annonceRepository = new AnnonceRepository(entityManager);

        User author = callInTransaction(() -> userRepository.create(buildUser("nplus-user", "nplus@example.com")));
        Category categoryA = callInTransaction(() -> categoryRepository.create(buildCategory("NPlusA")));
        Category categoryB = callInTransaction(() -> categoryRepository.create(buildCategory("NPlusB")));
        callInTransaction(() -> {
            annonceRepository.create(buildAnnonce("A1", "A1 desc", author, categoryA));
            annonceRepository.create(buildAnnonce("A2", "A2 desc", author, categoryA));
            annonceRepository.create(buildAnnonce("B1", "B1 desc", author, categoryB));
            return null;
        });

        entityManager.clear();

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        List<Category> categories = categoryRepository.findAll(0, 10);
        categories.forEach(category -> category.getAnnonces().size());

        long statements = statistics.getPrepareStatementCount();
        assertTrue(statements >= 3);
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

    private Annonce buildAnnonce(String title, String description, User author, Category category) {
        Annonce annonce = new Annonce();
        annonce.setTitle(title);
        annonce.setDescription(description);
        annonce.setAdress("Street 1");
        annonce.setMail("mail@example.com");
        annonce.setAuthor(author);
        annonce.setCategory(category);
        return annonce;
    }
}
