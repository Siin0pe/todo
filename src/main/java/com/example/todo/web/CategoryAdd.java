package com.example.todo.web;

import com.example.todo.model.Category;
import com.example.todo.service.CategoryService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "categoryAddServlet", value = "/category-add")
public class CategoryAdd extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CategoryService categoryService = new CategoryService();
        List<Category> categories = categoryService.listCategories();
        request.setAttribute("categories", categories);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/CategoryAdd.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String label = request.getParameter("label");
        Map<String, String> errors = new LinkedHashMap<>();
        if (label == null || label.trim().isEmpty()) {
            errors.put("label", "Label obligatoire.");
        } else if (label.trim().length() > 64) {
            errors.put("label", "Taille maximale: 64 caracteres.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("formLabel", label);
            doGet(request, response);
            return;
        }

        CategoryService categoryService = new CategoryService();
        if (categoryService.createCategory(label.trim()) == null) {
            errors.put("label", "Categorie deja existante.");
            request.setAttribute("errors", errors);
            request.setAttribute("formLabel", label);
            doGet(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/home?success=Categorie creee.");
    }
}
