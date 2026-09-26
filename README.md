# Stock Manager

A small product inventory and stock-movement tracking API built with Java and Spring Boot, with a lightweight web UI on top.

## Features

- Add, view, and delete products
- Add or remove stock for an existing product, with every movement recorded
- Full stock-movement history (per product and across the whole inventory)
- A configurable reorder level per product driving real low-stock detection
- Server-side validation with clear, structured error responses (no raw stack traces)
- Business rules enforced in the service layer: no duplicate product names, and a product can't be deleted while it still has stock
- A dependency-free single-page web UI (Dashboard, Products, Product Details, Add Product, Update Stock, Transactions)

## Tech Stack

**Backend**
- Java 17, Spring Boot 3.2
- Spring Data JPA / Hibernate
- Spring Validation (Bean Validation)
- Lombok
- MySQL
- Maven (via the included wrapper, `mvnw` / `mvnw.cmd`)
- JUnit 5 + Mockito

**Frontend**
- Plain HTML, CSS and JavaScript (no build step, no framework)
- Served directly by Spring Boot from `src/main/resources/static`
- Talks to the backend with the Fetch API

## Getting Started

### Prerequisites

- JDK 17+
- A running MySQL instance

### 1. Configure the database

Edit `src/main/resources/application.properties` and point it at your MySQL instance:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventoryappdb?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

The schema is created/updated automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Run the application

```bash
./mvnw spring-boot:run       # macOS/Linux
mvnw.cmd spring-boot:run     # Windows
```

The app starts on **http://localhost:9091** (configurable via `server.port`). Opening that URL in a browser loads the web UI directly; the same host also serves the REST API described below.

### 3. Run the tests

```bash
./mvnw test
```

Unit tests (service layer, Mockito) and controller tests (`@WebMvcTest`/MockMvc) run with no extra setup. There's also a full-stack integration test (`ProductApiIntegrationTest`) that boots the real app against a real, disposable MySQL instance via [Testcontainers](https://testcontainers.com/) — it requires Docker to be installed and running locally. Run just that one with:

```bash
./mvnw test -Dtest=ProductApiIntegrationTest
```

## API Reference

Base path: `/api`. All request/response bodies are JSON.

Errors use a consistent shape across every endpoint:

```json
{
  "status": 404,
  "message": "Product 42 was not found",
  "path": "/api/products/42",
  "timestamp": "2026-09-26T09:23:56.023899"
}
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `/products` | List every product |
| GET | `/products/{id}` | Get a single product |
| POST | `/products` | Create a new product |
| DELETE | `/products/{id}` | Delete a product (must have quantity `0`) |
| POST | `/products/{id}/stock-in` | Add stock to an existing product |
| POST | `/products/{id}/stock-out` | Remove stock from an existing product |
| GET | `/products/{id}/transactions` | List stock movements for one product (newest first) |
| GET | `/transactions` | List all stock movements (newest first) |

### List / Get Products

`GET /api/products` and `GET /api/products/{id}`

```json
{ "id": 1, "name": "Laptop", "quantity": 10, "reorderLevel": 5, "lowStock": false }
```

`lowStock` is `true` whenever `quantity <= reorderLevel`. `GET /products/{id}` returns `404` if the product doesn't exist.

### Create Product

`POST /api/products` → `201 Created`

```json
{ "name": "Laptop", "quantity": 10, "reorderLevel": 5 }
```

`reorderLevel` is optional and defaults to `5`. Rejected with `400` if `name` is blank or `quantity`/`reorderLevel` is negative, and with `409` if a product with that name already exists.

### Stock In / Stock Out

`POST /api/products/{id}/stock-in` and `POST /api/products/{id}/stock-out`

```json
{ "quantity": 5 }
```

Rejected with `400` if `quantity` isn't a positive number, or (on stock-out) there isn't enough stock available.

### Delete Product

`DELETE /api/products/{id}` → `204 No Content`, or `409` if the product's quantity isn't `0` yet.

> Note: a product that has ever had a stock movement recorded against it also can't be deleted, due to a foreign key from `stock_movements` back to `products`. This is intentional — deleting a product doesn't currently cascade-delete its movement history.

### Transactions

`GET /api/transactions` and `GET /api/products/{id}/transactions`

```json
[
  {
    "id": 1,
    "productId": 1,
    "productName": "Laptop",
    "quantityChange": 5,
    "type": "STOCK_IN",
    "occurredAt": "2026-09-26T09:23:56.098592"
  }
]
```

## Database Schema

```sql
CREATE TABLE products (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(255) UNIQUE NOT NULL,
  quantity      INT NOT NULL,
  reorder_level INT NOT NULL
);

CREATE TABLE stock_movements (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id       BIGINT NOT NULL REFERENCES products(id),
  quantity_change  INT NOT NULL,
  type             ENUM('STOCK_IN', 'STOCK_OUT') NOT NULL,
  occurred_at      TIMESTAMP NOT NULL
);
```

## Project Structure

```
src/main/java/com/aditya/stockmanager/
├── StockManagerApplication.java
├── domain/       # JPA entities (Product, StockMovement)
├── dto/          # Request/response records
├── exception/    # ApiException hierarchy + global error handling
├── repository/   # Spring Data repositories
├── service/      # Business logic and validation rules
└── web/          # REST controllers

src/main/resources/
├── application.properties
└── static/       # Web UI (index.html, css/, js/)
```
