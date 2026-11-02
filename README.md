# Online Store Database System

A desktop application for managing a small online store, built with **Java Swing** and backed by a **MySQL** relational database. It covers the full retail flow — products, suppliers, customers, orders, payments, inventory, and a loyalty-points program — behind a tabbed GUI, with a reporting module driven by multi-table SQL.

Designed and built by **Hicham Ouikrim**.

## Features

- **7 management modules** in a tabbed interface: Products, Customers, Orders, Payments, Inventory, Reports, and a free-form SQL console.
- **Relational schema** with foreign-key relationships across 9 tables (suppliers, products, inventory, customers, orders, order items, payments, points records, low-stock warnings).
- **Triggers** that automatically award loyalty points when an order is completed and raise a warning when stock falls below its reorder threshold.
- **SQL views** for cashier, management-sales, and member-discount reporting.
- **10+ reports** powered by joins, aggregates, and subqueries — revenue by product, repeat customers, monthly sales summaries, low-stock-with-supplier, top customers by points, and more.
- **Injection-safe** data entry using parameterized `PreparedStatement` queries throughout.

## Tech stack

- Java (Swing, AWT, JDBC)
- MySQL 8
- MySQL Connector/J

## Getting started

### Prerequisites
- JDK 11 or newer
- A running MySQL 8 server
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (the `.jar` is not committed — download it and place it next to the source)

### 1. Create the database
```sql
SOURCE online_store_044.sql;
```
(Run the included SQL script in your MySQL client to create the schema, triggers, views, and sample data.)

### 2. Configure the connection
Credentials are read from environment variables so nothing sensitive is stored in the code. Set them before running:

```bash
set DB_URL=jdbc:mysql://localhost:3306/online_store_044
set DB_USER=root
set DB_PASS=your_password
```
(On macOS/Linux use `export` instead of `set`.) If you leave them unset, the app falls back to `localhost` / `root` with an empty password.

### 3. Compile and run
```bash
javac -cp .;mysql-connector-j-9.7.0.jar OnlineStoreGUI.java
java  -cp .;mysql-connector-j-9.7.0.jar OnlineStoreGUI
```
(On macOS/Linux, use `:` instead of `;` in the classpath.)

## Project layout

| File | Purpose |
|------|---------|
| `OnlineStoreGUI.java` | Full Swing GUI, JDBC data access, and reporting logic |
| `online_store_044.sql` | Database schema: tables, foreign keys, views, triggers, and seed data |

## License

MIT
