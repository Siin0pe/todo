package com.example.todo;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet(name = "annonceListServlet", value = "/annonce-list")
public class AnnonceList extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Connection connection = ConnectionDB.getInstance();
            AnnonceDAO annonceDAO = new AnnonceDAO(connection);
            List<Annonce> annonces = annonceDAO.findAll();
            request.setAttribute("annonces", annonces);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WebContent/AnnonceList.jsp");
            dispatcher.forward(request, response);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
