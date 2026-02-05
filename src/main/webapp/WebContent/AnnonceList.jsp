<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.example.todo.model.Annonce" %>
<%@ page import="com.example.todo.model.Category" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Liste des annonces</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand fw-semibold" href="<%= request.getContextPath() %>/home">Todo</a>
        <div class="d-flex gap-2">
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/home">Accueil</a>
            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/logout">Logout</a>
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/annonce-add">Nouvelle annonce</a>
        </div>
    </div>
</nav>

<main class="container py-4">
    <div class="d-flex flex-wrap align-items-center justify-content-between mb-3">
        <h1 class="mb-0">Liste des annonces</h1>
    </div>

    <%
        List<Annonce> annonces = (List<Annonce>) request.getAttribute("annonces");
        List<Category> categories = (List<Category>) request.getAttribute("categories");
        Integer pageNumber = (Integer) request.getAttribute("page");
        Integer size = (Integer) request.getAttribute("size");
        String q = (String) request.getAttribute("q");
        Long categoryId = (Long) request.getAttribute("categoryId");
        Annonce.Status status = (Annonce.Status) request.getAttribute("status");
        Boolean hasNext = (Boolean) request.getAttribute("hasNext");
        String error = (String) request.getAttribute("error");
        String success = (String) request.getAttribute("success");
    %>

    <% if (error != null) { %>
        <div class="alert alert-danger" role="alert"><%= error %></div>
    <% } %>
    <% if (success != null) { %>
        <div class="alert alert-success" role="alert"><%= success %></div>
    <% } %>

    <form method="get" action="" class="row g-2 align-items-end mb-3">
        <div class="col-12 col-md-4">
            <label for="q" class="form-label">Mot-cle</label>
            <input type="text" id="q" name="q" value="<%= q == null ? "" : q %>" class="form-control">
        </div>
        <div class="col-12 col-md-3">
            <label for="categoryId" class="form-label">Categorie</label>
            <select id="categoryId" name="categoryId" class="form-select">
                <option value="">Toutes</option>
                <% if (categories != null) { %>
                    <% for (Category cat : categories) { %>
                        <option value="<%= cat.getId() %>" <%= categoryId != null && categoryId.equals(cat.getId()) ? "selected" : "" %>><%= cat.getLabel() %></option>
                    <% } %>
                <% } %>
            </select>
        </div>
        <div class="col-12 col-md-3">
            <label for="status" class="form-label">Statut</label>
            <select id="status" name="status" class="form-select">
                <option value="">Tous</option>
                <% for (Annonce.Status s : Annonce.Status.values()) { %>
                    <option value="<%= s.name() %>" <%= status != null && status == s ? "selected" : "" %>><%= s.name() %></option>
                <% } %>
            </select>
        </div>
        <div class="col-6 col-md-1">
            <label for="size" class="form-label">Taille</label>
            <input type="number" id="size" name="size" min="1" value="<%= size == null ? 10 : size %>" class="form-control">
        </div>
        <div class="col-6 col-md-1">
            <button type="submit" class="btn btn-outline-primary w-100">OK</button>
        </div>
    </form>

    <% if (annonces == null || annonces.isEmpty()) { %>
        <div class="alert alert-secondary" role="alert">Aucune annonce disponible.</div>
    <% } else { %>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle bg-white">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Titre</th>
                    <th>Statut</th>
                    <th>Date</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <% for (Annonce annonce : annonces) { %>
                    <tr>
                        <td><%= annonce.getId() %></td>
                        <td><%= annonce.getTitle() %></td>
                        <td><%= annonce.getStatus() %></td>
                        <td><%= annonce.getDate() %></td>
                        <td class="d-flex flex-wrap gap-2">
                            <a class="btn btn-sm btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-detail?id=<%= annonce.getId() %>">Detail</a>
                            <a class="btn btn-sm btn-outline-primary" href="<%= request.getContextPath() %>/annonce-update?id=<%= annonce.getId() %>">Modifier</a>
                            <form method="post" action="<%= request.getContextPath() %>/annonce-delete">
                                <input type="hidden" name="id" value="<%= annonce.getId() %>">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Supprimer</button>
                            </form>
                            <% if (annonce.getStatus() == Annonce.Status.DRAFT) { %>
                                <form method="post" action="<%= request.getContextPath() %>/annonce-publish">
                                    <input type="hidden" name="id" value="<%= annonce.getId() %>">
                                    <button type="submit" class="btn btn-sm btn-success">Publier</button>
                                </form>
                            <% } else if (annonce.getStatus() == Annonce.Status.PUBLISHED) { %>
                                <form method="post" action="<%= request.getContextPath() %>/annonce-archive">
                                    <input type="hidden" name="id" value="<%= annonce.getId() %>">
                                    <button type="submit" class="btn btn-sm btn-warning">Archiver</button>
                                </form>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    <% } %>

    <%
        StringBuilder query = new StringBuilder();
        if (q != null) {
            query.append("q=").append(URLEncoder.encode(q, "UTF-8"));
        }
        if (categoryId != null) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append("categoryId=").append(categoryId);
        }
        if (status != null) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append("status=").append(status.name());
        }
        if (size != null) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append("size=").append(size);
        }
        String queryString = query.length() > 0 ? "&" + query.toString() : "";
        int currentPage = pageNumber == null ? 0 : pageNumber;
        boolean showPrev = currentPage > 0;
        boolean showNext = hasNext != null && hasNext;
    %>

    <div class="d-flex justify-content-between align-items-center mt-3">
        <div>
            <% if (showPrev) { %>
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list?page=<%= currentPage - 1 %><%= queryString %>">Precedent</a>
            <% } %>
        </div>
        <div class="text-muted">Page <%= currentPage + 1 %></div>
        <div>
            <% if (showNext) { %>
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/annonce-list?page=<%= currentPage + 1 %><%= queryString %>">Suivant</a>
            <% } %>
        </div>
    </div>
</main>

<footer class="border-top bg-white">
    <div class="container py-3 text-muted small">Todo App - Gestion des annonces</div>
</footer>
</body>
</html>