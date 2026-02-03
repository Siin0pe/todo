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

@WebServlet(name = "annonceAddServlet", value = "/annonce-add")
public class AnnonceAdd extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceAdd.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        if (isBlank(title) || isBlank(description) || isBlank(adress) || isBlank(mail)) {
            request.setAttribute("error", "Tous les champs sont obligatoires.");
            doGet(request, response);
            return;
        }

        try {
            Connection connection = ConnectionDB.getInstance();
            AnnonceDAO annonceDAO = new AnnonceDAO(connection);
            Annonce annonce = new Annonce(
                    null,
                    title.trim(),
                    description.trim(),
                    adress.trim(),
                    mail.trim(),
                    new Timestamp(System.currentTimeMillis())
            );
            boolean created = annonceDAO.create(annonce);
            if (created) {
                request.setAttribute("success", "Annonce enregistrée.");
            } else {
                request.setAttribute("error", "Erreur lors de l'enregistrement.");
            }
            doGet(request, response);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
