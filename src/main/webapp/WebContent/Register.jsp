<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Creer un compte</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="<%= request.getContextPath() %>/">Todo</a>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/login">Login</a>
        </div>
    </div>
</nav>

<main class="container py-5" style="max-width: 620px;">
    <h1 class="mb-4">Creer un compte</h1>
    <%
        String error = (String) request.getAttribute("error");
        Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
        String formUsername = (String) request.getAttribute("formUsername");
        String formEmail = (String) request.getAttribute("formEmail");
    %>
    <% if (error != null) { %>
        <div class="alert alert-danger" role="alert"><%= error %></div>
    <% } %>
    <% if (errors != null && !errors.isEmpty()) { %>
        <div class="alert alert-danger" role="alert">
            <ul class="mb-0">
                <% for (String message : errors.values()) { %>
                    <li><%= message %></li>
                <% } %>
            </ul>
        </div>
    <% } %>
    <form method="post" action="" class="card p-4 shadow-sm">
        <div class="mb-3">
            <label for="username" class="form-label">Username</label>
            <input type="text" id="username" name="username" value="<%= formUsername == null ? "" : formUsername %>" class="form-control" required>
            <% if (errors != null && errors.get("username") != null) { %>
                <div class="text-danger small"><%= errors.get("username") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="email" class="form-label">Email</label>
            <input type="email" id="email" name="email" value="<%= formEmail == null ? "" : formEmail %>" class="form-control" required>
            <% if (errors != null && errors.get("email") != null) { %>
                <div class="text-danger small"><%= errors.get("email") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">Mot de passe</label>
            <input type="password" id="password" name="password" class="form-control" required>
            <% if (errors != null && errors.get("password") != null) { %>
                <div class="text-danger small"><%= errors.get("password") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="confirm" class="form-label">Confirmer le mot de passe</label>
            <input type="password" id="confirm" name="confirm" class="form-control" required>
            <% if (errors != null && errors.get("confirm") != null) { %>
                <div class="text-danger small"><%= errors.get("confirm") %></div>
            <% } %>
        </div>
        <button type="submit" class="btn btn-primary w-100">Creer le compte</button>
    </form>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Creation de compte</div>
</footer>
</body>
</html>