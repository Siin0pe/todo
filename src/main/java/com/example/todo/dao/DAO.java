package com.example.todo.dao;

import java.sql.Connection;
import java.util.List;

public abstract class DAO<T> {
    protected Connection connect;

    public DAO(Connection connect) {
        this.connect = connect;
    }

    public abstract boolean create(T obj);

    public abstract T find(String title, String mail);

    public abstract List<T> findAll();

    public abstract boolean update(T obj);

    public abstract boolean delete(T obj);
}
