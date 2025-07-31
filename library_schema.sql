-- Library Management System SQL Schema

CREATE DATABASE IF NOT EXISTS LibraryDB;
USE LibraryDB;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'user') DEFAULT 'user',
    approved BOOLEAN DEFAULT FALSE
);

-- Books Table
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100),
    author VARCHAR(100),
    available_copies INT DEFAULT 1
);

-- Issued Books Table
CREATE TABLE IF NOT EXISTS issued_books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    book_id INT,
    issue_date DATE,
    due_date DATE,
    returned BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
