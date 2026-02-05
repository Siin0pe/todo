<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.example.todo.model.Category" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Categories</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="<%= request.getContextPath() %>/home">Todo</a>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/home">Accueil</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list">Annonces</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>
</nav>

<main class="container py-4" style="max-width: 900px;">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="mb-0">Categories</h1>
    </div>

    <%
        Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
        String formLabel = (String) request.getAttribute("formLabel");
        List<Category> categories = (List<Category>) request.getAttribute("categories");
    %>

    <% if (errors != null && !errors.isEmpty()) { %>
        <div class="alert alert-danger" role="alert">
            <ul class="mb-0">
                <% for (String message : errors.values()) { %>
                    <li><%= message %></li>
                <% } %>
            </ul>
        </div>
    <% } %>

    <div class="card p-4 shadow-sm mb-4">
        <h2 class="h5">Creer une categorie</h2>
        <form method="post" action="" class="row g-3">
            <div class="col-12 col-md-8">
                <label for="label" class="form-label">Label</label>
                <input type="text" id="label" name="label" value="<%= formLabel == null ? "" : formLabel %>" class="form-control" required>
                <% if (errors != null && errors.get("label") != null) { %>
                    <div class="text-danger small"><%= errors.get("label") %></div>
                <% } %>
            </div>
            <div class="col-12 col-md-4 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">Enregistrer</button>
            </div>
        </form>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <h2 class="h5">Liste des categories</h2>
            <% if (categories == null || categories.isEmpty()) { %>
                <div class="text-muted">Aucune categorie pour le moment.</div>
            <% } else { %>
                <ul class="list-group list-group-flush">
                    <% for (Category category : categories) { %>
                        <li class="list-group-item"><%= category.getLabel() %></li>
                    <% } %>
                </ul>
            <% } %>
        </div>
    </div>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Categories</div>
</footer>
</body>
</html>