<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.todo.Annonce" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Liste des annonces</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container py-4">
<div class="d-flex flex-wrap align-items-center justify-content-between mb-3">
    <h1 class="mb-0">Liste des annonces</h1>
    <a class="btn btn-primary" href="<%= request.getContextPath() %>/annonce-add">Nouvelle annonce</a>
</div>

<%
    List<Annonce> annonces = (List<Annonce>) request.getAttribute("annonces");
%>

<% if (annonces == null || annonces.isEmpty()) { %>
    <div class="alert alert-secondary" role="alert">Aucune annonce disponible.</div>
<% } else { %>
    <div class="table-responsive">
    <table class="table table-striped table-hover align-middle">
        <thead>
        <tr>
            <th>ID</th>
            <th>Titre</th>
            <th>Description</th>
            <th>Adresse</th>
            <th>Email</th>
            <th>Date</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <% for (Annonce annonce : annonces) { %>
            <tr>
                <td><%= annonce.getId() %></td>
                <td><%= annonce.getTitle() %></td>
                <td><%= annonce.getDescription() %></td>
                <td><%= annonce.getAdress() %></td>
                <td><%= annonce.getMail() %></td>
                <td><%= annonce.getDate() %></td>
                <td>
                    <a class="btn btn-sm btn-outline-primary" href="<%= request.getContextPath() %>/annonce-update?id=<%= annonce.getId() %>">Modifier</a>
                    <a class="btn btn-sm btn-outline-danger" href="<%= request.getContextPath() %>/annonce-delete?id=<%= annonce.getId() %>">Supprimer</a>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    </div>
<% } %>
</div>
</body>
</html>
