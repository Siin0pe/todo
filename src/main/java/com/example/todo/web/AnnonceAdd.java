package com.example.todo.web;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Category;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.AnnonceService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "annonceAddServlet", value = "/annonce-add")
public class AnnonceAdd extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setAttribute("categories", loadCategories());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceAdd.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");
        Long categoryId = parseLong(request.getParameter("categoryId"));

        Map<String, String> errors = new LinkedHashMap<>();
        validateRequired(title, "title", 64, errors);
        validateRequired(description, "description", 256, errors);
        validateRequired(adress, "adress", 64, errors);
        validateEmail(mail, 64, errors);
        if (categoryId == null) {
            errors.put("categoryId", "Categorie obligatoire.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("formTitle", title);
            request.setAttribute("formDescription", description);
            request.setAttribute("formAdress", adress);
            request.setAttribute("formMail", mail);
            request.setAttribute("formCategoryId", categoryId);
            doGet(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        AnnonceService service = new AnnonceService();
        if (service.createAnnonce(title.trim(), description.trim(), adress.trim(), mail.trim(), userId, categoryId) != null) {
            request.setAttribute("success", "Annonce enregistree.");
            request.setAttribute("formTitle", null);
            request.setAttribute("formDescription", null);
            request.setAttribute("formAdress", null);
            request.setAttribute("formMail", null);
            request.setAttribute("formCategoryId", null);
        } else {
            request.setAttribute("error", "Erreur lors de l'enregistrement.");
        }
        doGet(request, response);
    }

    private List<Category> loadCategories() {
        EntityManager entityManager = EntityManagerUtil.getEntityManager();
        try {
            CategoryRepository repository = new CategoryRepository(entityManager);
            return repository.findAll(0, 1000);
        } finally {
            entityManager.close();
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateRequired(String value, String field, int max, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.put(field, "Champ obligatoire.");
            return;
        }
        if (value.trim().length() > max) {
            errors.put(field, "Taille maximale: " + max + " caracteres.");
        }
    }

    private void validateEmail(String value, int max, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.put("mail", "Email obligatoire.");
            return;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            errors.put("mail", "Taille maximale: " + max + " caracteres.");
            return;
        }
        if (!trimmed.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.put("mail", "Email invalide.");
        }
    }
}