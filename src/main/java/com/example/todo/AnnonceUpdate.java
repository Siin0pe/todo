package com.example.todo;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;

@WebServlet(name = "annonceUpdateServlet", value = "/annonce-update")
public class AnnonceUpdate extends HttpServlet {
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
            Annonce annonce = annonceDAO.findById(id);
            if (annonce == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Annonce introuvable");
                return;
            }
            request.setAttribute("annonce", annonce);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceUpdate.jsp");
            dispatcher.forward(request, response);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        if (isBlank(idParam) || isBlank(title) || isBlank(description) || isBlank(adress) || isBlank(mail)) {
            request.setAttribute("error", "Tous les champs sont obligatoires.");
            doGet(request, response);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Identifiant invalide.");
            doGet(request, response);
            return;
        }

        try {
            Connection connection = ConnectionDB.getInstance();
            AnnonceDAO annonceDAO = new AnnonceDAO(connection);
            Annonce annonce = new Annonce(
                    id,
                    title.trim(),
                    description.trim(),
                    adress.trim(),
                    mail.trim(),
                    new Timestamp(System.currentTimeMillis())
            );
            boolean updated = annonceDAO.update(annonce);
            if (updated) {
                request.setAttribute("success", "Annonce mise à jour.");
            } else {
                request.setAttribute("error", "Erreur lors de la mise à jour.");
            }
            request.setAttribute("annonce", annonceDAO.findById(id));
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceUpdate.jsp");
            dispatcher.forward(request, response);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
