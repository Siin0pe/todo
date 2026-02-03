package com.example.todo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@WebServlet(name = "annonceDeleteServlet", value = "/annonce-delete")
public class AnnonceDelete extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre id manquant");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre id invalide");
            return;
        }

        try {
            Connection connection = ConnectionDB.getInstance();
            AnnonceDAO annonceDAO = new AnnonceDAO(connection);
            Annonce annonce = new Annonce();
            annonce.setId(id);
            annonceDAO.delete(annonce);
            response.sendRedirect(request.getContextPath() + "/annonce-list");
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
