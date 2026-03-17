<h1 align="center">E-Commerce Backend Platform</h1>

<p align="center">
  A production-grade backend system for an e-commerce platform, featuring an industry-standard layered architecture and scalable REST APIs.
</p>

## Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
  - [Clone the Repository](#1-clone-the-repository)
  - [Configure application.properties](#2-configure-application.properties)
  - [Run with Docker Compose](#3-run-with-docker-compose)
  - [Run Locally (Without Docker)](#4-run-locally-without-docker)
- [Database Schema](#-database-schema)
  - [Entity-Relationship (ER) diagram](#1-entity-relationship-diagram)
  - [Table Structure](#2-table-structure)
  - [Useful SQL Commands](#3-useful-sql-commands)
- [API Documentation](#-api-documentation)
  - [Authentication](#1-authentication)
  - [Roles](#2-roles)
  - [API Endpoint Authorization](#3-api-endpoint-authorization)
  - [User APIs](#4-user-apis)
  - [Product APIs](#5-product-apis)
  - [Cart APIs](#6-cart-apis)
  - [Order APIs](#7-order-apis)
- [Exception Handling](#-exception-handling)
- [Logging Strategy](#-logging-strategy)
- [Testing](#-testing)
- [Swagger UI Documentation](#-swagger-ui-documentation)

## Overview

**ECommerce Backend Platform** is a RESTful API-based production-grade backend system designed to manage the complete operations of a modern e-commerce platform. Built with **Java 17** and **Spring Boot 3**, it acts as a scalable engine exposing robust REST APIs intended to be consumed by the clients.

The system seamlessly handles the entire digital storefront lifecycle. This includes stateless *JWT user authentication*, *dynamic product listings*, *shopping cart operations*, *order placement*, *real-time inventory management*, and *simulated payment handling*.

To demonstrate professional backend development standards, this application follows an industry-standard **layered architecture** (Controller → Service → Repository → Entity). The project utilizes clean Data Transfer Objects (DTOs) with ModelMapper, Hibernate for automated schema management, and Swagger UI for interactive API documentation. Everything is fully containerized via Docker Compose, allowing for instant, zero-configuration deployment across any environment.

## Features

| Feature | Description |
|---|---|
| JWT Authentication | Stateless auth using Spring Security + JWT tokens |
| User Management | Registration, login, role-based access (ADMIN, CUSTOMER) |
| Product Catalog | CRUD operations with filtering, sorting, and pagination |
| Cart Handling | Shopping Cart management with instant price updates |
| Order Management | Order placement with simulated payment processing |
| Email Notifications | Integrated SMTP service for order confirmations |
| Inventory Tracking | Real-time stock management |
| Swagger UI | Interactive API docs at `/swagger-ui.html` |
| phpMyAdmin UI | Bundled web interface for real-time database inspection |
| Docker Support | One-click deployment with synchronized timezones |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.x |
| Security | Spring Security 6, JWT (jjwt 0.12) |
| Database | MySQL 8.0.34 |
| ORM | Spring Data JPA / Hibernate 6 |
| Logging | SLF4J / Logback |
| Email | Java Mail Sender (SMTP) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, Jacoco, H2 |

## Project Structure

```
E_Commerce_Backend_System/
├── src/
│   ├── main/
│   │   ├── java/com/incture/E_Commerce_Backend_System/
│   │   │   ├── config/                     # Security, Swagger and ModelMapper config
│   │   │   ├── controller/                 # REST controllers
│   │   │   ├── dto/                        # Request/Response DTOs
│   │   │   ├── entity/                     # JPA entities
│   │   │   ├── exception/                  # Custom exception & global handler
│   │   │   ├── filter/                     # Custom JWT Filter
│   │   │   ├── repository/                 # Spring Data JPA repositories
│   │   │   ├── service/                    # Business logic
│   │   │   ├── utils/                      # Managing JSON Web Tokens
│   │   │   └── E_Commerce_Backend_System.java
│   │   └── resources/
│   │       └── application.properties      # Base config
│   └── test/
│       └── java/com/incture/E_Commerce_Backend_System/
│           ├── controller/                 # Controller (MockMvc) tests
│           ├── repository/                 # Repository (Unit) tests
│           └── service/                    # Service (Mockito) tests
├──target/
├──logs/                                    # Stores all logs by date
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Prerequisites

Before running this project, make sure you have the following installed:

| Tool | Version | Notes |
|---|---|---|
| Java (JDK) | 17+ | `java -version` to verify |
| Maven | 3.9+ | `mvn -version` to verify |
| Docker Desktop | 4.64.0+ | Required for Docker Compose setup |
| Docker Compose | 2.x+ | Bundled with Docker Desktop |
| MySQL | 8.0+ | Only if running without Docker |
| Postman | 11.x+ | API Testing Tool |

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/RajeshJena14/E_Commerce_Backend_System.git
cd E_Commerce_Backend_System
```

### 2. Configure application.properties

Modify the application.properties file inside src/main/resources:

```env
# Database
spring.application.name=E_Commerce_Backend_System
spring.datasource.name=ECommerceBackendSystemApplication
spring.datasource.url=jdbc:mysql://localhost:3306/inctureECommerceDatabase  # For Running Locally
spring.datasource.url=jdbc:mysql://localhost:3307/inctureECommerceDatabase  # For Running using Docker Desktop
spring.datasource.username=root
spring.datasource.password=your_secure_password  // Replace with your DB password

...Rest of the code

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email     // Replace with your email
spring.mail.password=your_password  // Generate from App Password from your Google account
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Create Database (if it does not exist)
```sql
CREATE DATABASE inctureECommerceDatabase;
```

### 3. Run with Docker Compose

This is the **recommended way** to get everything running in one command

1. To start Docker Desktop:
```bash
docker desktop start
```

2. To run your application:
```bash
docker-compose up --build
```

- API Access: `http://localhost:8082`
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- phpMyAdmin: `http://localhost:8081`

3. To stop:
```bash
docker-compose down
```
To stop and remove volumes (wipes the database):
```bash
docker-compose down -v
```
4. To stop Docker Desktop:
```bash
docker desktop stop
```

**Services started by Docker Compose:**

| Service | Port | Description |
|---|---|---|
| `ECommerceBackend-Application` | 8082 | Spring Boot application |
| `MySQL` | 3307 | MySQL database |
| `phpMyAdmin` | 8081 | Web Interface to manage MySQL database |


### 4. Run Locally (Without Docker)

**Step 1:** Build the project:

```bash
mvn clean install
```

**Step 2:** Run the application:

```bash
mvn spring-boot:run
```

Or run the JAR directly in `target` folder:

```bash
java -jar target/E_Commerce_Backend_System-0.0.1-SNAPSHOT.jar
```

- API Access: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`


## Database Schema

The database is built on **MySQL 8.0** and the schema is automatically generated and managed by Hibernate via Spring Data JPA.

### Entity-Relationship (ER) diagram
Below is the Entity-Relationship (ER) diagram mapping out the core domain models and their cardinalities:


```
┌───────────────────────┐ 1               1 ┌───────────────────────┐
│         users         │───────────────────│         carts         │
├───────────────────────┤    (OneToOne)     ├───────────────────────┤
│ **id** (PK)           │                   │ **id** (PK)           │
│ name                  │                   │ user_id (FK, UNIQUE)  │
│ email                 │                   │ total_price           │
│ password              │                   └──────────┬────────────┘
│ role                  │                              │ 1
└──────────┬────────────┘                              │
           │ 1                                         │
           │                                           │ (OneToMany)
           │ (OneToMany)                               │
           │                                           │
           │ N                                         │ N
┌──────────▼────────────┐                   ┌──────────▼────────────┐
│        orders         │                   │      cart_items       │
├───────────────────────┤                   ├───────────────────────┤
│ **id** (PK)           │                   │ **id** (PK)           │
│ user_id (FK)          │                   │ cart_id (FK)          │
│ total_amount          │                   │ product_id (FK)       │
│ order_date            │                   │ quantity              │
│ order_status          │                   └──────────┬────────────┘
└──────────┬────────────┘                              │ N
           │ 1                                         │
           │                                           │
           │ (OneToMany)                               │ (OneToMany)
           │                                           │
           │ N                                         │ 1
┌──────────▼────────────┐                   ┌──────────▼────────────┐
│      order_items      │ N               1 │       products        │
├───────────────────────┤───────────────────├───────────────────────┤
│ **id** (PK)           │    (OneToMany)    │ **id** (PK)           │
│ order_id (FK)         │                   │ name                  │
│ product_id (FK)       │                   │ description           │
│ quantity              │                   │ price                 │
│ price                 │                   │ stock                 │
└───────────────────────┘                   | category              |
                                            | image_url             |
                                            | rating                |
                                            └───────────────────────┘
```

### Table Structure

#### 1. User Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Unique identifier for the user (Auto-incremented) |
| name | VARCHAR(255) | Name of the user (Must be UNIQUE) |
| email | VARCHAR(255) | User's email address (Must be UNIQUE) |
| password | VARCHAR(255) | BCrypt-hashed password |
| role | VARCHAR(50) | Security role assigned to the user (e.g., ADMIN, CUSTOMER) |

#### 2. Product Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Unique identifier for the product | 
| name | VARCHAR(255) | Name or title of the product (Must be UNIQUE) | 
| description | VARCHAR(255) | Detailed description of the product features | 
| price | DOUBLE(10,2) | Current selling price of the product | 
| stock | INT | Current available inventory count | 
| category | VARCHAR(100) | The category grouping (e.g., Electronics, Fashion) | 
| image_url | VARCHAR(255) | Link to the product's primary image | 
| rating | INT | Average customer rating | 


#### 3. Cart Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Unique identifier for the shopping cart | 
| user_id | BIGINT (FK) | Maps to users.id (UNIQUE constraint) | 
| total_price | DOUBLE(10,2) | Real-time calculated sum of all items currently in the cart | 


#### 4. Cart Items Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Unique identifier for the cart item entry| 
| cart_id | BIGINT (FK) | Maps to carts.id| 
| product_id | BIGINT (FK) | Maps to products.id| 
| quantity | INT | Number of units of this specific product the user intends to buy| 


#### 5. Orders Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Unique identifier for the placed order | 
| user_id | BIGINT (FK) | Maps to users.id (The customer who placed the order) | 
| total_amount | DOUBLE(10,2) | Final amount paid at checkout | 
| order_date | DATETIME | Timestamp of when the checkout was completed | 
| payment_status | VARCHAR(50) | Current payment state (e.g., PENDING, SUCCESS, FAILED) | 
| order_status | VARCHAR(50) | Fulfillment state (e.g., PLACED, SHIPPED, DELIVERED) | 


#### 6. Order Items Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Unique identifier for the order item entry | 
| order_id | BIGINT (FK) | Maps to orders.id | 
| product_id | BIGINT (FK) | Maps to products.id | 
| quantity | INT | Number of units purchased | 
| price | DOUBLE(10,2) | The exact price of the product at the moment of purchase | 

### Useful SQL Commands
If you want to manually inspect the database inside the `phpMyAdmin` container or your local MySQL workbench, you can use these quick-reference queries to verify the tables generated by Hibernate and check your data:

```sql
USE inctureECommerceDatabase;

SHOW TABLES;

SELECT * FROM users;
SELECT * FROM products;
SELECT * FROM carts;
SELECT * FROM cart_items;
SELECT * FROM orders;
SELECT * FROM order_items;
```

## API Documentation

### Authentication

This application uses stateless JWT authentication. All secured endpoints require a valid token to be passed in the HTTP headers.

#### Authentication Workflow

1. **Register a user**: Make a POST request to `/api/users/register` if not registered.

2. **Login to the application**: Make a POST request to `/api/users/login` for signing in into the application.

3. **Receive Token**: On successful authentication, the server will return a `JWT` in the `Authorization` header.

4. **Authorize Requests**: For all subsequent requests to secured endpoints, include the `JWT` token in the `Authorization` header using the `Bearer` schema:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Roles
| Role | System Permissions & Capabilities | 
|---|---|
| CUSTOMER | Represents the standard end-user. Customers have read-only access to the product catalog but possess full read/write capabilities for their personal shopping sessions (managing their cart, initiating checkouts, and viewing personal order history) |
| ADMIN | Represents the system administrator. Admins bypass customer restrictions, possessing full CRUD (Create, Read, Update, Delete) privileges to manage the overarching product catalog, update order fulfillment statuses, and modify user accounts | 


### API Endpoint Authorization

1. **Public Endpoints:**

    No authentication required (*`permitAll`*).

    | Method | Endpoint | Description | 
    |---|---|---|
    | POST | `/api/users/register` | Register a new user account | 
    | POST | `/api/users/login` | Authenticate and receive a JWT | 
    | GET | `SWAGGER_URL(s)` | Access the Swagger UI and OpenAPI documentation |

<br>

 2. **ADMIN only Endpoints:**

    Requires a valid JWT with role = *`ADMIN`*

    | Method | Endpoint | Description | 
    |---|---|---|
    | GET | `/api/users/` | Retrieve a list of all users | 
    | DELETE | `/api/users/{id}` | Delete a user from the system | 
    | POST | `/api/products/` | Add a new product to the catalog | 
    | PUT | `/api/products/{id}` | Update an existing product's details | 
    | DELETE | `/api/products/{id}` | Remove a product from the catalog | 
    | PUT | `/api/orders/{id}/status` | Update the fulfillment status of an order | 

<br>

 3. **CUSTOMER only Endpoints:**

    Requires a valid JWT with role = *`CUSTOMER`*

    | Method | Endpoint | Description | 
    |---|---|---|
    | ALL | `/api/cart/**` | Full access to manage the user's shopping cart (Add, View, Update, Remove) | 
    | POST | `/api/orders/checkout` | Convert the current active cart into a placed order | 
    | GET | `/api/orders/history` | View the user's past order history |  

    > ⚠️ Logged-in users hitting these endpoints can access only their records.

<br>

 4. **Authenticated Endpoints (Both ADMIN & CUSTOMER):**

    Requires ANY valid JWT. Because these are not explicitly locked to a specific role above, both *`ADMIN`* and *`CUSTOMER`* can access them.

    | Method | Endpoint | Description | 
    |---|---|---|
    | GET | `/api/products` | View the full catalog of active products | 
    | GET | `/api/products/{id}` | View details of a specific product | 
    | GET | `/api/products/category` | View details of products of a specific category |
    | PUT | `/api/users/{id}` | Update personal profile information | 
    | GET | `/api/users/{id}` | Retrieve details of a specific user | 
    | GET | `/api/orders/` | Retrieves all orders | 
    | GET | `/api/orders/{id}` | Retrieves a specific order |

    > ⚠️ Logged-in users hitting `/api/users/{id}`, `/api/orders/` & `/api/orders/{id}` endpoints can access only their records. Accessing other user's records will show **ACCESS DENIED** response.

<br>

### USER APIs


1. **Register User:**

    ```
    POST /api/users/register
    ```

    **Request Body and Response:**

    ![Architecture](Pictures/Screenshot%202026-03-16%20232516.png)

    > 💡 Mandatory fields: Name, Email, Password. If Role is not provided, then the User is provided the role `CUSTOMER` by-default

<br>

2. **Login User:**

    ```
    POST /api/users/login
    ```

    **Request Body and Response:**

    ![Architecture](Pictures/Screenshot%202026-03-16%20233338.png)

    >💡 Mandatory fields: Name, Password

<br>

3. **Get All Users (ADMIN):**

    ```
    GET /api/users/
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-16%20234213.png)

<br>

4. **Get User by ID (ADMIN & CURRENT USER):**

    ```
    GET /api/users/{id}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-16%20234549.png)

<br>

5. **Update details of Existing User (ADMIN & CURRENT USER):**

    ```
    PUT /api/users/{id}
    ```

    **Request Body and Response:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20000656.png)

<br>

6. **Delete an existing User (ADMIN):**

    ```
    DELETE /api/users/{id}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20000835.png)

<br>

### PRODUCT APIs


1. **Add a new Product (ADMIN):**

    ```
    POST /api/products/
    ```

    **Request Body and Response:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20001710.png)

    > 💡 Mandatory fields: Name, Description, Price, Stock, Category

<br>

2. **Get All Products:**

    ```
    GET /api/products/
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20001812.png)

    <br>

    ![Architecture](Pictures/Screenshot%202026-03-17%20001926.png)

    >💡 For Custom Pagination, pass custom values to the parameters

<br>

3. **Get Products by Category:**

    ```
    GET /api/products/category
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20002036.png)

    >💡 For Custom Pagination, pass custom values to the parameters

<br>

4. **Get a specific Product by ID:**

    ```
    GET /api/products/{id}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20005351.png)

<br>


5. **Update details of Existing Product (ADMIN):**

    ```
    PUT /api/products/{id}
    ```

    **Request Body and Response:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20002357.png)

<br>

6. **Delete an existing Product (ADMIN):**

    ```
    DELETE /api/products/{id}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20002501.png)

<br>

### CART APIs (Only CUSTOMER)


1. **Add a Product to Cart:**

    ```
    POST /api/cart/add/{productId}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20005702.png)

<br>

2. **Show a Customer's Cart:**

    ```
    GET /api/cart/
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20005722.png)

<br>

3. **Delete an Item from Cart:**

    ```
    DELETE /api/cart/remove/{productId}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20011416.png)

<br>

4. **Update quantity of a Product in Cart:**

    ```
    PUT /api/cart/update/{productId}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20005926.png)

<br>


5. **Show Total price before Checkout:**

    ```
    GET /api/cart/showPrice
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20010043.png)

<br>

### ORDER APIs


1. **Checkout your Cart Items (CUSTOMER):**

    ```
    POST /api/orders/checkout
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20010241.png)

<br>

2. **Show Order History (CUSTOMER):**

    ```
    GET /api/orders/history
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20010440.png)

<br>

3. **Display all orders (ADMIN):**

    ```
    GET /api/orders/
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20011705.png)

<br>

4. **Get a specific order by ID:**

    ```
    GET /api/orders/{orderId}
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20011749.png)

<br>


5. **Update Order Status of a specific Order (ADMIN):**

    ```
    PUT /api/orders/{orderId}/status
    ```

    **Response Body:**

    ![Architecture](Pictures/Screenshot%202026-03-17%20011931.png)

<br>


## Exception Handling

To ensure a consistent and predictable client experience, the application implements a centralized error-handling mechanism using Spring's `@RestControllerAdvice`. 

Instead of returning raw server stack traces, the API catches specific exceptions (e.g., `IllegalArgumentException`, `NullPointerException`, `UsernameNotFoundException`, `AccessDeniedException`, `CustomException` etc.) and translates them into clean, standardized Responses.

**Example**
```json
// 404 NOT FOUND
"Product with ID 5 not found in the catalog..."
```

<br>

## Logging Strategy

Robust monitoring and debugging are critical for production systems. This project utilizes **SLF4J** abstracted over **Logback** for comprehensive application logging.

- **Console Logging**: Formatted output for real-time monitoring during local development.

- **File-Based Rolling Logs**: In production, logs are automatically written to the `/logs/` directory. The application uses a daily rolling policy, meaning a new log file is generated each day to prevent massive, unmanageable files.

- **Audit Trails**: Critical actions, such as user registrations, admin product modifications, and order checkouts, are logged at the `INFO` level, while caught exceptions are logged at the `ERROR` level with stack traces for debugging.

<br>

## Testing

The application is rigorously tested to ensure enterprise-grade reliability and maintainability. The test suite leverages **JUnit 5** and **Mockito** for isolated unit testing, bypassing the full Spring context where possible for maximum execution speed.

### Test Architecture

* **Controllers:** Tested using `MockMvc` in a pure Mockito Standalone setup. This isolates the web layer and intentionally bypasses Spring Security filters to rapidly validate routing, HTTP status codes, and JSON serialization.
* **Services:** Pure unit tests utilizing `@ExtendWith(MockitoExtension.class)` to mock all repository dependencies. This ensures the core business logic (such as cart total calculations and inventory reduction rules) functions flawlessly in complete isolation.
* **Repositories:** Tested using the lightweight, in-memory **H2 Database** combined with `@DataJpaTest`. This validates custom native queries, JPQL, and data integrity constraints without needing to spin up the heavy MySQL Docker container.

### Running the Tests

> ⚠️ **Important Note on H2 Database Dependency:** > The H2 in-memory database is used exclusively for testing the repository layer. Before running the test suite, please ensure to **uncomment the H2 dependency** in the `pom.xml` file. Once all tests have passed, **comment it out again** to keep the production build clean.

To execute the entire test suite:

```bash
mvn test
```

### Generate test coverage report

We utilize **JaCoCo** to track test coverage and enforce quality standards. To generate the full HTML coverage report, run:

```bash
mvn clean
mvn install
# Report available at: target/site/jacoco/index.html
```

**Current Coverage:**

![Architecture](Pictures/Screenshot%202026-03-17%20021908.png)

<br>

## Swagger UI Documentation

To enhance developer experience and API consumption, this project integrates **SpringDoc OpenAPI 3 (Swagger UI)**. This provides a beautiful, interactive web interface to explore, test, and validate all REST endpoints directly from your browser—without needing external tools like Postman.

### Accessing the Documentation

Depending on how you launched the application, the Swagger UI dashboard is available at:
* **Docker Compose:** `http://localhost:8082/swagger-ui/index.html`
* **Local (Maven):** `http://localhost:8080/swagger-ui/index.html`

### How to Authenticate in Swagger

Because the application is secured with strict Role-Based Access Control (RBAC), you must inject your JWT into the Swagger UI session to test secured endpoints (like managing a cart or placing an order).

**Step 1: Generate a Token**
1. Scroll down to the `User APIs` section in Swagger.
2. Expand the `POST /api/users/login` endpoint.
3. Click **"Try it out"**, enter valid user credentials in the request body, and hit **"Execute"**.
4. Scroll down to the Server Response and copy the raw `token` string.

**Step 2: Authorize your Session**
1. Scroll to the very top of the Swagger page and click the green 🔓 **Authorize** button.
2. In the input field, paste your copied JWT (If your OpenAPI config requires it, type `Bearer ` before the token).
3. Click **Authorize** and then **Close**.

*🎉 You are now authenticated! You will see closed padlocks (🔒) next to the secured endpoints, indicating you have the active clearance to execute them based on your assigned role (`ADMIN` or `CUSTOMER`).*
