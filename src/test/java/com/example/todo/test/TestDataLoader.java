package com.example.todo.test;

import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import jakarta.persistence.EntityManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class TestDataLoader {
    private TestDataLoader() {
    }

    public static TestData load(EntityManager entityManager) {
        List<Category> categories = createCategories(entityManager);
        List<User> users = createUsers(entityManager);
        List<Annonce> annonces = createAnnonces(entityManager, users.get(0), categories.get(0));
        return new TestData(categories, users, annonces);
    }

    private static List<Category> createCategories(EntityManager entityManager) {
        List<Category> categories = new ArrayList<>();
        for (char label = 'A'; label <= 'E'; label++) {
            Category category = new Category();
            category.setLabel("Category " + label);
            entityManager.persist(category);
            categories.add(category);
        }
        return categories;
    }

    private static List<User> createUsers(EntityManager entityManager) {
        List<User> users = new ArrayList<>();
        User alice = new User();
        alice.setUsername("alice");
        alice.setEmail("alice@example.com");
        alice.setPassword("secret");
        entityManager.persist(alice);
        users.add(alice);

        User bob = new User();
        bob.setUsername("bob");
        bob.setEmail("bob@example.com");
        bob.setPassword("secret");
        entityManager.persist(bob);
        users.add(bob);

        return users;
    }

    private static List<Annonce> createAnnonces(EntityManager entityManager, User author, Category category) {
        List<Annonce> annonces = new ArrayList<>();
        Instant base = Instant.parse("2024-01-01T00:00:00Z");
        for (int i = 1; i <= 30; i++) {
            Annonce annonce = new Annonce();
            annonce.setTitle("Annonce " + i);
            annonce.setDescription("Description " + i);
            annonce.setAdress("Adresse " + i);
            annonce.setMail("contact" + i + "@example.com");
            annonce.setDate(Timestamp.from(base.plusSeconds(i * 60L)));
            annonce.setStatus(i % 2 == 0 ? Annonce.Status.PUBLISHED : Annonce.Status.DRAFT);
            annonce.setAuthor(author);
            annonce.setCategory(category);
            entityManager.persist(annonce);
            annonces.add(annonce);
        }
        return annonces;
    }

    public static final class TestData {
        private final List<Category> categories;
        private final List<User> users;
        private final List<Annonce> annonces;

        private TestData(List<Category> categories, List<User> users, List<Annonce> annonces) {
            this.categories = categories;
            this.users = users;
            this.annonces = annonces;
        }

        public List<Category> getCategories() {
            return categories;
        }

        public List<User> getUsers() {
            return users;
        }

        public List<Annonce> getAnnonces() {
            return annonces;
        }
    }
}
