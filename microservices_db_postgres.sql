SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET timezone = '+00:00';

-- Supprimer la table si elle existe
DROP TABLE IF EXISTS students;

-- Créer une séquence pour l'identifiant
CREATE SEQUENCE students_id_seq;

-- Créer la table students
CREATE TABLE students (
    id BIGINT NOT NULL DEFAULT nextval('students_id_seq') PRIMARY KEY,
    name VARCHAR(90) NOT NULL,
    last_name VARCHAR(90) NOT NULL,
    email VARCHAR(90) NOT NULL,
    created_at TIMESTAMP,
    image BYTEA,
    CONSTRAINT uk_students_email UNIQUE (email)
);

-- Données initiales
INSERT INTO students (name, last_name, email, created_at, image) VALUES
('Alice', 'Smith', 'alice@example.com', '2023-01-01 10:00:00', NULL),
('Bob', 'Jones', 'bob@example.com', '2023-01-02 15:00:00', NULL);

-- Définir la séquence comme propriété de la colonne id
ALTER SEQUENCE students_id_seq OWNED BY students.id;
