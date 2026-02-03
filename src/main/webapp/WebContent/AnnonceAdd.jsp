<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Ajout d'annonce</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container py-4">
    <h1 class="mb-4">Créer une annonce</h1>

<%
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>
<% if (error != null) { %>
    <div class="alert alert-danger" role="alert"><%= error %></div>
<% } else if (success != null) { %>
    <div class="alert alert-success" role="alert">
        <div><%= success %></div>
        <div>Votre annonce a bien été envoyée.</div>
    </div>
<% } %>

<form method="post" action="" class="card p-4 shadow-sm">
    <div class="mb-3">
        <label for="title" class="form-label">Titre</label>
        <input type="text" id="title" name="title" class="form-control" required>
    </div>
    <div class="mb-3">
        <label for="description" class="form-label">Description</label>
        <textarea id="description" name="description" rows="5" class="form-control" required></textarea>
    </div>
    <div class="mb-3">
        <label for="adress" class="form-label">Adresse</label>
        <input type="text" id="adress" name="adress" class="form-control" required>
    </div>
    <div class="mb-3">
        <label for="mail" class="form-label">Email</label>
        <input type="email" id="mail" name="mail" class="form-control" required>
    </div>
    <div class="d-flex gap-2">
        <button type="submit" class="btn btn-primary">Enregistrer</button>
        <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list">Voir les annonces</a>
    </div>
</form>
</div>
</body>
</html>
