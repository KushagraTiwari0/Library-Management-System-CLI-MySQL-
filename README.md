# Library Management System (Java CLI + MySQL)

This is a **Command Line Interface (CLI)** based **Library Management System** built using **Java** and **MySQL**. It manages library operations such as user registration, book issuance/return, user approvals, and inventory management with role-based access control.

---

## 🔧 Tech Stack

- **Language:** Java  
- **Database:** MySQL  
- **Database Connectivity:** JDBC  
- **IDE:** IntelliJ IDEA  
- **Other Tools:** MySQL Workbench, ChronoUnit (Java Time API)

---

## ✨ Features

### 👥 User Roles
- **Admin**
  - Approve newly registered users
  - Add new books to the inventory
  - View list of issued books and users
  - Track total registered users

- **User**
  - Register and await admin approval
  - Login and view available books
  - Issue books and track due dates
  - Return books if issued
  - See only relevant options depending on state (e.g. return option shown only if a book is issued)

---

## 📦 Functionalities

- ✅ User Registration and Login (with admin approval required)
- 📚 Book Inventory Management (Add/View)
- 🔐 Role-Based Access
- 📅 Due Date Calculation using Java's `ChronoUnit`
- 📖 Issue/Return Book Workflow
- 📈 Admin Dashboard Features via CLI

---

## 🛠 Setup Instructions

### 1. Clone the Repository 

```bash
git clone https://github.com/KushagraTiwari0/Library-Management-System-Java-CLI.git
cd Library-Management-System-Java-CLI
