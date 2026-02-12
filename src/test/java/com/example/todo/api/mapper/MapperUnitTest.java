package com.example.todo.api.mapper;

import com.example.todo.api.dto.AnnonceResponse;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.UserResponse;
import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperUnitTest {

    @Test
    void annonceMapper_mapsAllFields() {
        User author = new User();
        author.setId(5L);
        Category category = new Category();
        category.setId(9L);
        Annonce annonce = new Annonce();
        annonce.setId(3L);
        annonce.setTitle("Title");
        annonce.setDescription("Desc");
        annonce.setAdress("Addr");
        annonce.setMail("mail@example.com");
        annonce.setDate(Timestamp.from(Instant.parse("2024-02-01T10:15:30Z")));
        annonce.setStatus(Annonce.Status.PUBLISHED);
        annonce.setAuthor(author);
        annonce.setCategory(category);

        AnnonceResponse response = AnnonceMapper.toResponse(annonce);

        assertEquals(3L, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals("Desc", response.getDescription());
        assertEquals("Addr", response.getAdress());
        assertEquals("mail@example.com", response.getMail());
        assertEquals("2024-02-01T10:15:30Z", response.getDate());
        assertEquals("PUBLISHED", response.getStatus());
        assertEquals(5L, response.getAuthorId());
        assertEquals(9L, response.getCategoryId());
    }

    @Test
    void categoryMapper_handlesNull() {
        assertNull(CategoryMapper.toResponse(null));
    }

    @Test
    void categoryMapper_mapsFields() {
        Category category = new Category();
        category.setId(7L);
        category.setLabel("Work");

        CategoryResponse response = CategoryMapper.toResponse(category);

        assertEquals(7L, response.getId());
        assertEquals("Work", response.getLabel());
    }

    @Test
    void userMapper_handlesNull() {
        assertNull(UserMapper.toResponse(null));
    }

    @Test
    void userMapper_mapsFields() {
        User user = new User();
        user.setId(2L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");

        UserResponse response = UserMapper.toResponse(user);

        assertEquals(2L, response.getId());
        assertEquals("alice", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());
    }
}
