package com.example.todo.integration;

import com.example.todo.model.Annonce;
import com.example.todo.model.Category;
import com.example.todo.model.User;
import com.example.todo.repository.AnnonceRepository;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiIntegrationSpringTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("todo_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("todo.security.jwt.secret", () -> "integration-secret");
        registry.add("todo.security.admin-users", () -> "admin");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AnnonceRepository annonceRepository;

    @BeforeEach
    void cleanDatabase() {
        annonceRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() throws Exception {
        createUser("user-login", "user-login@example.com", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "user-login", "password", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void swaggerUiIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/swagger-ui/index.html")));
    }

    @Test
    void openApiDocsAreAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isString())
                .andExpect(jsonPath("$.paths").isMap());
    }

    @Test
    void actuatorHealthAndInfoAreExposed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Todo API"));
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void insufficientRoleReturns403() throws Exception {
        User user = createUser("user-role", "user-role@example.com", "secret123");
        Category category = createCategory("Services");
        Annonce annonce = createAnnonce(user, category, Annonce.Status.DRAFT);
        String userToken = loginAndGetToken("user-role", "secret123");

        mockMvc.perform(patch("/api/annonces/{id}", annonce.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "ARCHIVED"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("admin")));
    }

    @Test
    void annonceCrudWorksEndToEnd() throws Exception {
        User adminUser = createUser("admin", "admin@example.com", "secret123");
        Category category = createCategory("Immobilier");
        String adminToken = loginAndGetToken("admin", "secret123");

        MvcResult createResult = mockMvc.perform(post("/api/annonces")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Annonce initiale",
                                "description", "Description initiale",
                                "adress", "10 rue des Fleurs",
                                "mail", "admin@example.com",
                                "authorId", adminUser.getId(),
                                "categoryId", category.getId()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long annonceId = createdJson.path("id").asLong();

        mockMvc.perform(get("/api/annonces/{id}", annonceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(annonceId))
                .andExpect(jsonPath("$.title").value("Annonce initiale"));

        mockMvc.perform(put("/api/annonces/{id}", annonceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Annonce mise a jour",
                                "description", "Description mise a jour",
                                "adress", "12 avenue Centrale",
                                "mail", "admin@example.com",
                                "categoryId", category.getId(),
                                "status", "PUBLISHED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Annonce mise a jour"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(patch("/api/annonces/{id}", annonceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "ARCHIVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(delete("/api/annonces/{id}", annonceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/annonces/{id}", annonceId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNotFound());
    }

    private User createUser(String username, String email, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        return userRepository.save(user);
    }

    private Category createCategory(String label) {
        Category category = new Category();
        category.setLabel(label);
        return categoryRepository.save(category);
    }

    private Annonce createAnnonce(User author, Category category, Annonce.Status status) {
        Annonce annonce = new Annonce();
        annonce.setTitle("Annonce existante");
        annonce.setDescription("Description existante");
        annonce.setAdress("Adresse existante");
        annonce.setMail("contact@example.com");
        annonce.setStatus(status);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        return annonceRepository.save(annonce);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("token").asText();
    }

    private String json(Object payload) throws Exception {
        return objectMapper.writeValueAsString(payload);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
