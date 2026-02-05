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
import java.util.List;

@WebServlet(name = "homeServlet", value = "/home")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CategoryService categoryService = categoryService();
        List<Category> categories = categoryService.listCategories();
        request.setAttribute("categories", categories);
        request.setAttribute("success", trimToNull(request.getParameter("success")));
        request.setAttribute("error", trimToNull(request.getParameter("error")));

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/Home.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    protected CategoryService categoryService() {
        return new CategoryService();
    }
}
