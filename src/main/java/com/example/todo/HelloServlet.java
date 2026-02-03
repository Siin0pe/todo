package com.example.todo;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import java.io.IOException;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello the World";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String fullMessage = message + (name == null || name.trim().isEmpty() ? "" : " " + name);

        request.setAttribute("message", fullMessage);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    public void destroy() {
    }
}
