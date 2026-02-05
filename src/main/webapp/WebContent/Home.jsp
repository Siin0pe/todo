<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.todo.model.Category" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Accueil</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="<%= request.getContextPath() %>/home">Todo</a>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list">Annonces</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/category-add">Categories</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>
</nav>

<main class="container py-4">
    <div class="d-flex flex-wrap align-items-center justify-content-between mb-3">
        <h1 class="mb-0">Accueil</h1>
        <div class="d-flex gap-2">
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/annonce-add">Nouvelle annonce</a>
            <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/category-add">Nouvelle categorie</a>
        </div>
    </div>

    <%
        String error = (String) request.getAttribute("error");
        String success = (String) request.getAttribute("success");
        List<Category> categories = (List<Category>) request.getAttribute("categories");
    %>

    <% if (error != null) { %>
        <div class="alert alert-danger" role="alert"><%= error %></div>
    <% } %>
    <% if (success != null) { %>
        <div class="alert alert-success" role="alert"><%= success %></div>
    <% } %>

    <div class="row g-3">
        <div class="col-12 col-lg-7">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h2 class="h5">Raccourcis</h2>
                    <div class="d-flex flex-wrap gap-2">
                        <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/annonce-list">Lister les annonces</a>
                        <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/annonce-add">Creer une annonce</a>
                        <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/category-add">Creer une categorie</a>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-12 col-lg-5">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h2 class="h5">Categories</h2>
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
        </div>
    </div>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Accueil</div>
</footer>
</body>
</html>