package com.example.todo.repository;

import com.example.todo.model.Category;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryRepositoryIntegrationTest extends RepositoryIntegrationTestBase {

    @Test
    void crudOperationsPersistAndDeleteCategory() {
        CategoryRepository repository = new CategoryRepository(entityManager);

        Category category = new Category();
        category.setLabel("Jobs");

        Category persisted = callInTransaction(() -> repository.create(category));
        assertNotNull(persisted.getId());

        Category found = repository.findById(persisted.getId());
        assertNotNull(found);
        assertEquals("Jobs", found.getLabel());

        found.setLabel("Services");
        callInTransaction(() -> repository.update(found));

        Category updated = repository.findById(persisted.getId());
        assertEquals("Services", updated.getLabel());

        callInTransaction(() -> {
            repository.delete(persisted.getId());
            return null;
        });

        assertNull(repository.findById(persisted.getId()));
    }

    @Test
    void searchAndPaginationReturnExpectedCategories() {
        CategoryRepository repository = new CategoryRepository(entityManager);

        callInTransaction(() -> {
            repository.create(buildCategory("Alpha"));
            repository.create(buildCategory("Beta"));
            repository.create(buildCategory("Gamma"));
            return null;
        });

        List<Category> page0 = repository.findAll(0, 2);
        assertEquals(2, page0.size());
        assertEquals("Alpha", page0.get(0).getLabel());
        assertEquals("Beta", page0.get(1).getLabel());

        List<Category> page1 = repository.findAll(1, 2);
        assertEquals(1, page1.size());
        assertEquals("Gamma", page1.get(0).getLabel());

        List<Category> search = repository.searchByKeyword("bet", 0, 10);
        assertEquals(1, search.size());
        assertEquals("Beta", search.get(0).getLabel());

        assertNotNull(repository.findByLabel("alpha"));
        assertTrue(repository.searchByKeyword("missing", 0, 10).isEmpty());
    }

    private Category buildCategory(String label) {
        Category category = new Category();
        category.setLabel(label);
        return category;
    }
}
