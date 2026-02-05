<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="<%= request.getContextPath() %>/">Todo</a>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/register">Creer un compte</a>
        </div>
    </div>
</nav>

<main class="container py-5" style="max-width: 540px;">
    <h1 class="mb-4">Connexion</h1>
    <%
        String error = (String) request.getAttribute("error");
        String success = (String) request.getAttribute("success");
        String formLogin = (String) request.getAttribute("formLogin");
    %>
    <% if (error != null) { %>
        <div class="alert alert-danger" role="alert"><%= error %></div>
    <% } %>
    <% if (success != null) { %>
        <div class="alert alert-success" role="alert"><%= success %></div>
    <% } %>
    <form method="post" action="" class="card p-4 shadow-sm">
        <div class="mb-3">
            <label for="login" class="form-label">Username ou email</label>
            <input type="text" id="login" name="login" value="<%= formLogin == null ? "" : formLogin %>" class="form-control" required>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">Mot de passe</label>
            <input type="password" id="password" name="password" class="form-control" required>
        </div>
        <button type="submit" class="btn btn-primary w-100">Se connecter</button>
    </form>
    <div class="text-center mt-3">
        <a href="<%= request.getContextPath() %>/register">Pas de compte ? Creer un compte</a>
    </div>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Authentification</div>
</footer>
</body>
</html>