# 📚 Book Store Management System

A Java-based Book Store Management System developed as a Database Management System (DBMS) project.

The application provides a graphical user interface for managing authors, books, customers, and orders using a MySQL database.

---

## 🎯 Project Objective

The objective of this project is to design and implement a database-driven Book Store Management System using:

- Java
- Java Swing
- JDBC
- MySQL
- SQL

The application demonstrates database operations such as:

- CREATE
- INSERT
- SELECT
- UPDATE
- DELETE

---

## ✨ Features

### 👨‍💼 Author Management

- Add author
- View authors
- Delete author
- Refresh author data

### 📚 Book Management

- Add book
- View books
- Delete book
- Update book information
- Manage stock

### 👤 Customer Management

- Add customer
- View customers
- Delete customer
- Update customer information

### 🛒 Order Management

- Select customer
- Select books
- Add books to cart
- Specify quantity
- Place order
- Automatically calculate total amount
- Update book stock
- View previous orders

---

## 🗄️ Database Design

The system uses the following tables:

1. AUTHOR
2. BOOK
3. CUSTOMER
4. ORDERS
5. ORDER_ITEM

### Relationships

```text
AUTHOR
   │
   │ 1 : N
   ↓
BOOK
   │
   │ 1 : N
   ↓
ORDER_ITEM
   ↑
   │ N : 1
ORDERS
   ↑
   │ N : 1
CUSTOMER