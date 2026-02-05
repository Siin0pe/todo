<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.todo.model.Annonce" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Detail annonce</title>
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
        <h1 class="mb-0">Detail annonce</h1>
    </div>

    <%
        Annonce annonce = (Annonce) request.getAttribute("annonce");
    %>
    <% if (annonce != null) { %>
        <div class="card p-4 shadow-sm">
            <h2 class="h4"><%= annonce.getTitle() %></h2>
            <p class="text-muted mb-1">Statut: <strong><%= annonce.getStatus() %></strong></p>
            <p class="text-muted mb-3">Date: <%= annonce.getDate() %></p>
            <p><%= annonce.getDescription() %></p>
            <div class="row mt-3">
                <div class="col-md-6">
                    <div><strong>Adresse:</strong> <%= annonce.getAdress() %></div>
                    <div><strong>Email:</strong> <%= annonce.getMail() %></div>
                </div>
                <div class="col-md-6">
                    <div><strong>Auteur:</strong> <%= annonce.getAuthor() != null ? annonce.getAuthor().getUsername() : "" %></div>
                    <div><strong>Categorie:</strong> <%= annonce.getCategory() != null ? annonce.getCategory().getLabel() : "" %></div>
                </div>
            </div>
            <div class="d-flex gap-2 mt-4">
                <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/annonce-update?id=<%= annonce.getId() %>">Modifier</a>
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list">Retour</a>
            </div>
        </div>
    <% } %>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Detail d'annonce</div>
</footer>
</body>
</html>