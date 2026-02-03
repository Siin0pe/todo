-- PostgreSQL schema for the "todo" database
-- Run as a superuser to create the database, then connect and create the table.

CREATE DATABASE todo;

\c todo

CREATE TABLE annonce (
    id SERIAL PRIMARY KEY,
    title VARCHAR(64) NOT NULL,
    description VARCHAR(256) NOT NULL,
    adress VARCHAR(64) NOT NULL,
    mail VARCHAR(64) NOT NULL,
    date TIMESTAMP NOT NULL
);
