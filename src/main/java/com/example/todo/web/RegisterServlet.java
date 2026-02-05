package com.example.todo.web;

import com.example.todo.model.User;
import com.example.todo.service.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(name = "registerServlet", value = "/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/Register.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm");

        Map<String, String> errors = new LinkedHashMap<>();
        validateRequired(username, "username", 64, errors);
        validateEmail(email, 128, errors);
        validatePassword(password, errors);
        if (confirm == null || !confirm.equals(password)) {
            errors.put("confirm", "Confirmation invalide.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("formUsername", username);
            request.setAttribute("formEmail", email);
            doGet(request, response);
            return;
        }

        UserService userService = userService();
        User created = userService.registerUser(username.trim(), email.trim(), password);
        if (created == null) {
            request.setAttribute("error", "Username ou email deja utilise.");
            request.setAttribute("formUsername", username);
            request.setAttribute("formEmail", email);
            doGet(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login?success=Compte cree.");
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
            errors.put("email", "Email obligatoire.");
            return;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            errors.put("email", "Taille maximale: " + max + " caracteres.");
            return;
        }
        if (!trimmed.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.put("email", "Email invalide.");
        }
    }

    private void validatePassword(String value, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.put("password", "Mot de passe obligatoire.");
            return;
        }
        if (value.length() < 6) {
            errors.put("password", "Minimum 6 caracteres.");
        }
        if (value.length() > 255) {
            errors.put("password", "Taille maximale: 255 caracteres.");
        }
    }

    protected UserService userService() {
        return new UserService();
    }
}
