package com.example.todo.api.rest;

import com.example.todo.api.dto.AnnonceCreateRequest;
import com.example.todo.api.dto.LoginRequest;
import com.example.todo.api.dto.RegisterRequest;
import com.example.todo.api.dto.UserResponse;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLoadIT extends RestIntegrationTestBase {

    @Test
    void listAnnonces_handlesSimpleConcurrentLoad() throws Exception {
        runConcurrentRequests(8, 20, () -> {
            try (Response response = target("annonces")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .request(MediaType.APPLICATION_JSON)
                    .get()) {
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new IllegalStateException("Expected 200 but got " + response.getStatus());
                }
                response.readEntity(String.class);
            }
        });
    }

    @Test
    void login_handlesSimpleConcurrentLoad() throws Exception {
        registerUser("load-user", "load-user@example.com", "secret123");

        runConcurrentRequests(6, 15, () -> {
            LoginRequest request = new LoginRequest();
            request.setLogin("load-user");
            request.setPassword("secret123");

            try (Response response = target("login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request))) {
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new IllegalStateException("Expected 200 but got " + response.getStatus());
                }
                response.readEntity(String.class);
            }
        });
    }

    @Test
    void createAnnonce_handlesSimpleConcurrentLoad() throws Exception {
        UserResponse user = registerUser("load-create-user", "load-create-user@example.com", "secret123");
        String token = loginToken("load-create-user", "secret123");
        Long categoryId = findCategoryId("Category A");
        AtomicInteger sequence = new AtomicInteger(0);

        runConcurrentRequests(6, 12, () -> {
            int n = sequence.incrementAndGet();
            AnnonceCreateRequest request = new AnnonceCreateRequest();
            request.setTitle("Load annonce " + n);
            request.setDescription("Load description " + n);
            request.setAdress("Load address " + n);
            request.setMail("load-create-" + n + "@example.com");
            request.setAuthorId(user.getId());
            request.setCategoryId(categoryId);

            try (Response response = target("annonces")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.json(request))) {
                if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                    throw new IllegalStateException("Expected 201 but got " + response.getStatus());
                }
                response.readEntity(String.class);
            }
        });
    }

    @Test
    void register_handlesSimpleConcurrentLoad() throws Exception {
        AtomicInteger sequence = new AtomicInteger(0);

        runConcurrentRequests(5, 10, () -> {
            int n = sequence.incrementAndGet();
            RegisterRequest request = new RegisterRequest();
            request.setUsername("load-register-" + n);
            request.setEmail("load-register-" + n + "@example.com");
            request.setPassword("secret123");

            try (Response response = target("register")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(request))) {
                if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                    throw new IllegalStateException("Expected 201 but got " + response.getStatus());
                }
                response.readEntity(String.class);
            }
        });
    }

    private void runConcurrentRequests(int threads, int requestsPerThread, CheckedRunnable action) throws Exception {
        int totalRequests = threads * requestsPerThread;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>(threads);

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startGate.await();
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        action.run();
                        successCount.incrementAndGet();
                    } catch (Exception ex) {
                        errors.add(ex.getMessage());
                    }
                }
                return null;
            }));
        }

        long start = System.nanoTime();
        startGate.countDown();

        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate");

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertEquals(totalRequests, successCount.get(),
                "Some requests failed. durationMs=" + durationMs + ", firstError=" + errors.peek());
        assertTrue(errors.isEmpty(),
                "There were request errors. durationMs=" + durationMs + ", firstError=" + errors.peek());
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
