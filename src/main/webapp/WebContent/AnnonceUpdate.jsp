<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.todo.model.Annonce" %>
<%@ page import="com.example.todo.model.Category" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Modifier une annonce</title>
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
        <h1 class="mb-0">Modifier une annonce</h1>
    </div>

<%
    Annonce annonce = (Annonce) request.getAttribute("annonce");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
    Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    String formTitle = (String) request.getAttribute("formTitle");
    String formDescription = (String) request.getAttribute("formDescription");
    String formAdress = (String) request.getAttribute("formAdress");
    String formMail = (String) request.getAttribute("formMail");
    Long formCategoryId = (Long) request.getAttribute("formCategoryId");
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
<% if (success != null) { %>
    <div class="alert alert-success" role="alert"><%= success %></div>
<% } %>

<% if (annonce != null) { %>
    <form method="post" action="" class="card p-4 shadow-sm">
        <input type="hidden" name="id" value="<%= annonce.getId() %>">
        <div class="mb-3">
            <label for="title" class="form-label">Titre</label>
            <input type="text" id="title" name="title" value="<%= formTitle != null ? formTitle : annonce.getTitle() %>" class="form-control" required>
            <% if (errors != null && errors.get("title") != null) { %>
                <div class="text-danger small"><%= errors.get("title") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="description" class="form-label">Description</label>
            <textarea id="description" name="description" rows="5" class="form-control" required><%= formDescription != null ? formDescription : annonce.getDescription() %></textarea>
            <% if (errors != null && errors.get("description") != null) { %>
                <div class="text-danger small"><%= errors.get("description") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="adress" class="form-label">Adresse</label>
            <input type="text" id="adress" name="adress" value="<%= formAdress != null ? formAdress : annonce.getAdress() %>" class="form-control" required>
            <% if (errors != null && errors.get("adress") != null) { %>
                <div class="text-danger small"><%= errors.get("adress") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="mail" class="form-label">Email</label>
            <input type="email" id="mail" name="mail" value="<%= formMail != null ? formMail : annonce.getMail() %>" class="form-control" required>
            <% if (errors != null && errors.get("mail") != null) { %>
                <div class="text-danger small"><%= errors.get("mail") %></div>
            <% } %>
        </div>
        <div class="mb-3">
            <label for="categoryId" class="form-label">Categorie</label>
            <select id="categoryId" name="categoryId" class="form-select" required>
                <option value="">Choisir</option>
                <% if (categories != null) { %>
                    <% for (Category cat : categories) { %>
                        <%
                            Long selectedId = formCategoryId != null ? formCategoryId
                                    : (annonce.getCategory() != null ? annonce.getCategory().getId() : null);
                        %>
                        <option value="<%= cat.getId() %>" <%= selectedId != null && selectedId.equals(cat.getId()) ? "selected" : "" %>><%= cat.getLabel() %></option>
                    <% } %>
                <% } %>
            </select>
            <% if (errors != null && errors.get("categoryId") != null) { %>
                <div class="text-danger small"><%= errors.get("categoryId") %></div>
            <% } %>
        </div>
        <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary">Mettre a jour</button>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list">Retour a la liste</a>
        </div>
    </form>
<% } %>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Edition d'annonce</div>
</footer>
</body>
</html>