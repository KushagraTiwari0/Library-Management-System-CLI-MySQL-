-- Create users table
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE,
  password VARCHAR(100),
  role ENUM('user', 'admin') DEFAULT 'user',
  approved BOOLEAN DEFAULT FALSE
);

-- Create books table
CREATE TABLE books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100),
  author VARCHAR(100),
  availableCopies INT DEFAULT 1
);

-- Create issued_books table
CREATE TABLE issued_books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  userId INT,
  bookId INT,
  issueDate DATE,
  FOREIGN KEY (userId) REFERENCES users(id),
  FOREIGN KEY (bookId) REFERENCES books(id)
);
