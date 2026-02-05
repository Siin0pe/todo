package com.example.todo.web;

import com.example.todo.model.User;
import com.example.todo.service.AuthService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String success = request.getParameter("success");
        if (success != null && !success.trim().isEmpty()) {
            request.setAttribute("success", success.trim());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/Login.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String login = request.getParameter("login");
        String password = request.getParameter("password");

        if (isBlank(login) || isBlank(password)) {
            request.setAttribute("error", "Identifiants obligatoires.");
            request.setAttribute("formLogin", login);
            doGet(request, response);
            return;
        }

        AuthService authService = authService();
        User user = authService.authenticate(login, password);
        if (user == null) {
            request.setAttribute("error", "Login ou mot de passe incorrect.");
            request.setAttribute("formLogin", login);
            doGet(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        response.sendRedirect(request.getContextPath() + "/home");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    protected AuthService authService() {
        return new AuthService();
    }
}
