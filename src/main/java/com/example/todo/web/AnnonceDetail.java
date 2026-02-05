package com.example.todo.web;

import com.example.todo.model.Annonce;
import com.example.todo.service.AnnonceService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "annonceDetailServlet", value = "/annonce-detail")
public class AnnonceDetail extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametre id manquant");
            return;
        }

        AnnonceService service = new AnnonceService();
        Annonce annonce = service.findByIdWithRelations(id);
        if (annonce == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Annonce introuvable");
            return;
        }

        request.setAttribute("annonce", annonce);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceDetail.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
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
}
