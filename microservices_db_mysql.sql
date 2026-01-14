SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- Table pour Courses
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP
);

-- Table pour Exams
CREATE TABLE exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    subject_id BIGINT
);

-- Données initiales
INSERT INTO courses (name, description, created_at) VALUES
('Course 1', 'Introduction to Programming', '2023-01-01 10:00:00'),
('Course 2', 'Advanced Java', '2023-01-02 15:00:00');

INSERT INTO exams (name, subject_id) VALUES
('Exam 1', 1),
('Exam 2', 2);
