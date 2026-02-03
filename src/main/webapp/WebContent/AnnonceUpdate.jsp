<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.todo.Annonce" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Modifier une annonce</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container py-4">
<h1 class="mb-4">Modifier une annonce</h1>

<%
    Annonce annonce = (Annonce) request.getAttribute("annonce");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>

<% if (error != null) { %>
    <div class="alert alert-danger" role="alert"><%= error %></div>
<% } else if (success != null) { %>
    <div class="alert alert-success" role="alert"><%= success %></div>
<% } %>

<% if (annonce != null) { %>
    <form method="post" action="" class="card p-4 shadow-sm">
        <input type="hidden" name="id" value="<%= annonce.getId() %>">
        <div class="mb-3">
            <label for="title" class="form-label">Titre</label>
            <input type="text" id="title" name="title" value="<%= annonce.getTitle() %>" class="form-control" required>
        </div>
        <div class="mb-3">
            <label for="description" class="form-label">Description</label>
            <textarea id="description" name="description" rows="5" class="form-control" required><%= annonce.getDescription() %></textarea>
        </div>
        <div class="mb-3">
            <label for="adress" class="form-label">Adresse</label>
            <input type="text" id="adress" name="adress" value="<%= annonce.getAdress() %>" class="form-control" required>
        </div>
        <div class="mb-3">
            <label for="mail" class="form-label">Email</label>
            <input type="email" id="mail" name="mail" value="<%= annonce.getMail() %>" class="form-control" required>
        </div>
        <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary">Mettre à jour</button>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list">Retour à la liste</a>
        </div>
    </form>
<% } %>
</div>
</body>
</html>
