package com.example.todo.web;

import com.example.todo.service.AnnonceService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@WebServlet(name = "annoncePublishServlet", value = "/annonce-publish")
public class AnnoncePublish extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        if (id == null) {
            redirectWithError(response, request, "Parametre id manquant.");
            return;
        }

        AnnonceService service = new AnnonceService();
        if (service.publishAnnonce(id) == null) {
            redirectWithError(response, request, "Annonce introuvable.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/annonce-list?success=" +
                URLEncoder.encode("Annonce publiee.", "UTF-8"));
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

    private void redirectWithError(HttpServletResponse response, HttpServletRequest request, String message)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/annonce-list?error=" +
                URLEncoder.encode(message, "UTF-8"));
    }
}
