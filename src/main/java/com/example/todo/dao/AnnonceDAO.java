package com.example.todo.dao;

import com.example.todo.model.Annonce;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnnonceDAO extends DAO<Annonce> {
    public AnnonceDAO(Connection connect) {
        super(connect);
    }

    @Override
    public boolean create(Annonce obj) {
        String sql = "INSERT INTO annonce (title, description, adress, mail, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setString(1, obj.getTitle());
            stmt.setString(2, obj.getDescription());
            stmt.setString(3, obj.getAdress());
            stmt.setString(4, obj.getMail());
            Timestamp ts = obj.getDate() != null ? obj.getDate() : new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(5, ts);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Annonce find(String title, String mail) {
        String sql = "SELECT id, title, description, adress, mail, date FROM annonce WHERE title = ? AND mail = ?";
        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, mail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Annonce(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("adress"),
                            rs.getString("mail"),
                            rs.getTimestamp("date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Annonce findById(int id) {
        String sql = "SELECT id, title, description, adress, mail, date FROM annonce WHERE id = ?";
        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Annonce(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("adress"),
                            rs.getString("mail"),
                            rs.getTimestamp("date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Annonce> findAll() {
        String sql = "SELECT id, title, description, adress, mail, date FROM annonce ORDER BY date DESC";
        List<Annonce> annonces = new ArrayList<>();
        try (PreparedStatement stmt = connect.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                annonces.add(new Annonce(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("adress"),
                        rs.getString("mail"),
                        rs.getTimestamp("date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return annonces;
    }

    @Override
    public boolean update(Annonce obj) {
        String sql = "UPDATE annonce SET title = ?, description = ?, adress = ?, mail = ?, date = ? WHERE id = ?";
        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setString(1, obj.getTitle());
            stmt.setString(2, obj.getDescription());
            stmt.setString(3, obj.getAdress());
            stmt.setString(4, obj.getMail());
            Timestamp ts = obj.getDate() != null ? obj.getDate() : new Timestamp(System.currentTimeMillis());
            stmt.setTimestamp(5, ts);
            stmt.setLong(6, obj.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Annonce obj) {
        String sql = "DELETE FROM annonce WHERE id = ?";
        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setLong(1, obj.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
