package com.example.todo.web;

import com.example.todo.db.EntityManagerUtil;
import com.example.todo.model.Annonce;
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
import java.io.IOException;
import java.util.List;

@WebServlet(name = "annonceListServlet", value = "/annonce-list")
public class AnnonceList extends HttpServlet {
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = parseInt(request.getParameter("page"), 0);
        int size = parseInt(request.getParameter("size"), DEFAULT_PAGE_SIZE);
        String keyword = trimToNull(request.getParameter("q"));
        Long categoryId = parseLong(request.getParameter("categoryId"));
        Annonce.Status status = parseStatus(request.getParameter("status"));
        String error = trimToNull(request.getParameter("error"));
        String success = trimToNull(request.getParameter("success"));

        AnnonceService service = new AnnonceService();
        List<Annonce> annonces;
        if (keyword != null) {
            annonces = service.searchAnnonces(keyword, page, size);
        } else {
            annonces = service.filterAnnonces(categoryId, status, page, size);
        }

        request.setAttribute("annonces", annonces);
        request.setAttribute("page", page);
        request.setAttribute("size", size);
        request.setAttribute("q", keyword);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("status", status);
        request.setAttribute("hasNext", annonces.size() == size);
        request.setAttribute("categories", loadCategories());
        request.setAttribute("error", error);
        request.setAttribute("success", success);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceList.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
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

    private Annonce.Status parseStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Annonce.Status.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
