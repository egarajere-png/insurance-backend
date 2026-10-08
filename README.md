# Insurance Portal — Backend

Spring Boot REST API for the Insurance Portal.

The backend provides authentication integration, customer management, insurance product management, dependant management, insurance application processing, administrative functionality, database persistence, and PDF generation.

The backend uses PostgreSQL for persistent storage and Firebase Admin SDK for authentication token validation.

---

## Table of Contents

* [Overview](#overview)
* [Technology Stack](#technology-stack)
* [Features](#features)
* [Architecture](#architecture)
* [Project Structure](#project-structure)
* [Prerequisites](#prerequisites)
* [Database Setup](#database-setup)
* [Firebase Setup](#firebase-setup)
* [Environment Configuration](#environment-configuration)
* [Running Locally](#running-locally)
* [Running with Docker](#running-with-docker)
* [API Documentation](#api-documentation)
* [Security](#security)
* [PDF Generation](#pdf-generation)
* [Available Commands](#available-commands)
* [Troubleshooting](#troubleshooting)

---

# Overview

The Insurance Portal backend is a Spring Boot REST API responsible for the application's business logic and persistent data.

The backend communicates with:

* PostgreSQL
* Firebase Authentication
* The Insurance Portal frontend

The application exposes REST endpoints for:

* Authentication
* Customers
* Dependants
* Insurance products
* Insurance applications
* Administrative users
* Dashboard information
* Application review
* PDF generation

---

# Technology Stack

| Technology         | Purpose                           |
| ------------------ | --------------------------------- |
| Java 17            | Programming language              |
| Spring Boot 3.3.0  | Backend framework                 |
| Spring Web         | REST API                          |
| Spring Data JPA    | Database persistence              |
| Hibernate          | ORM                               |
| PostgreSQL         | Relational database               |
| Firebase Admin SDK | Authentication/token verification |
| Spring Validation  | Request validation                |
| SpringDoc OpenAPI  | API documentation                 |
| iText 5.5.10       | PDF generation                    |
| Lombok             | Boilerplate reduction             |
| Maven              | Dependency/build management       |
| Docker             | Containerization                  |

---

# Features

## Authentication

The backend integrates with Firebase Authentication.

The backend:

* Receives Firebase ID tokens from the frontend
* Validates authentication tokens
* Identifies authenticated users
* Protects API resources
* Provides authenticated-user information

Authentication filtering is implemented through:

```text
FirebaseAuthFilter
```

Firebase initialization is handled through:

```text
FirebaseConfig
```

---

# Customer Management

The backend supports:

* Customer creation
* Customer retrieval
* Customer updates
* Customer details
* Customer-related insurance applications

---

# Insurance Product Management

The system supports insurance product management including:

* Product creation
* Product retrieval
* Product updates
* Product details
* Product plans
* Product-related application workflows

---

# Dependant Management

Customers can have dependant information associated with their records.

The backend provides functionality for:

* Creating dependants
* Retrieving dependants
* Updating dependant information
* Associating dependants with customers

---

# Insurance Applications

The backend handles the insurance application lifecycle.

Applications include information such as:

* Customer
* Product
* Plan
* Applicant information
* People/dependants
* Health information
* Application status

Application statuses are represented by:

```text
ApplicationStatus
```

The application review process allows authorized users to review and process submitted applications.

---

# Administrative Functionality

The backend provides administrative functionality for:

* Administrative users
* User roles
* Customer management
* Product management
* Application management
* Dashboard statistics
* Application review

---

# PDF Generation

The backend generates application PDFs using:

```text
iText PDF 5.5.10
```

PDF generation is implemented in:

```text
src/main/java/com/abcbank/insurance/pdf/ApplicationPDFGenerator.java
```

Application PDF functionality is exposed through the application/customer-product backend workflow.

---

# Architecture

The backend follows a layered Spring Boot architecture:

```text
                 ┌─────────────────────┐
                 │     REST Client     │
                 │   React Frontend    │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    Controllers      │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │      Services       │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    Repositories     │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │     PostgreSQL      │
                 └─────────────────────┘
```

Authentication operates alongside the request pipeline:

```text
Frontend
   |
   | Firebase ID Token
   v
FirebaseAuthFilter
   |
   | validated identity
   v
Controller
```

---

# Project Structure

```text
insurance-backend/
│
├── src/
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── abcbank/
│   │   │           └── insurance/
│   │   │
│   │   │               ├── config/
│   │   │               │   ├── CorsConfig.java
│   │   │               │   ├── DbSchemaFixer.java
│   │   │               │   └── FirebaseConfig.java
│   │   │               │
│   │   │               ├── controllers/
│   │   │               │   ├── AdminUserController.java
│   │   │               │   ├── AuthController.java
│   │   │               │   ├── CustomerController.java
│   │   │               │   ├── CustomerProductController.java
│   │   │               │   ├── DependantController.java
│   │   │               │   └── ProductController.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── AdminUserDto.java
│   │   │               │   ├── AuthMeDto.java
│   │   │               │   ├── CustomerDto.java
│   │   │               │   ├── CustomerProductDto.java
│   │   │               │   ├── DashboardStatsDto.java
│   │   │               │   ├── DependantDto.java
│   │   │               │   ├── ProductDto.java
│   │   │               │   ├── ReviewDto.java
│   │   │               │   └── RoleUpdateDto.java
│   │   │               │
│   │   │               ├── entities/
│   │   │               │   ├── AppUser.java
│   │   │               │   ├── ApplicationStatus.java
│   │   │               │   ├── Customer.java
│   │   │               │   ├── CustomerProduct.java
│   │   │               │   ├── Dependant.java
│   │   │               │   ├── PersonType.java
│   │   │               │   ├── Plan.java
│   │   │               │   ├── Product.java
│   │   │               │   └── Role.java
│   │   │               │
│   │   │               ├── exception/
│   │   │               │   ├── ApiException.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               │
│   │   │               ├── pdf/
│   │   │               │   └── ApplicationPDFGenerator.java
│   │   │               │
│   │   │               ├── repo/
│   │   │               │   ├── AppUserRepo.java
│   │   │               │   ├── CustomerProductRepo.java
│   │   │               │   ├── CustomerRepo.java
│   │   │               │   ├── DependantRepo.java
│   │   │               │   └── ProductRepo.java
│   │   │               │
│   │   │               ├── security/
│   │   │               │   └── FirebaseAuthFilter.java
│   │   │               │
│   │   │               ├── services/
│   │   │               │   ├── AppUserService.java
│   │   │               │   ├── CustomerProductService.java
│   │   │               │   ├── CustomerService.java
│   │   │               │   ├── DependantService.java
│   │   │               │   └── ProductService.java
│   │   │               │
│   │   │               ├── util/
│   │   │               │   ├── Actor.java
│   │   │               │   ├── CurrentUser.java
│   │   │               │   └── Text.java
│   │   │               │
│   │   │               └── InsuranceApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── firebase-service-account.json
│   │       └── images/
│   │           ├── abc-logo.png
│   │           ├── abcib-logo.jpg
│   │           ├── abcib-logo.png
│   │           └── abcib-logo.gif
│   │
│   ├── test/
│   │
│   └── ...
│
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── Dockerfile
├── .dockerignore
└── compose.yaml
```

---

# Prerequisites

For local backend development install:

* Java 17
* Maven, or use the included Maven Wrapper
* PostgreSQL
* Git

Verify Java:

```bash
java -version
```

The project targets:

```text
Java 17
```

---

# Database Setup

The application uses PostgreSQL.

## Database

The Dockerized application uses:

```text
Database: insurance
Username: insurance_user
Password: password
Port: 5432
```

The backend connects to PostgreSQL using:

```text
jdbc:postgresql://postgres:5432/insurance
```

when running inside Docker.

For local development, PostgreSQL normally runs on:

```text
localhost:5432
```

---

# Local PostgreSQL Setup

Create the database:

```bash
sudo -u postgres psql
```

Then:

```sql
CREATE DATABASE insurance;
CREATE USER insurance_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE insurance TO insurance_user;
```

Exit:

```sql
\q
```

The exact database credentials should match the active Spring configuration.

---

# Firebase Setup

The backend uses Firebase Admin SDK to verify Firebase Authentication tokens.

A Firebase service account is required.

The service-account file is:

```text
firebase-service-account.json
```

For local development, it may be configured according to the application's Firebase configuration.

For Docker, the service-account file is mounted into the container as a read-only runtime file rather than being baked into the Docker image.

### Important

The Firebase service-account JSON contains sensitive credentials.

It should:

* Never be committed to Git
* Never be pushed to a public repository
* Never be embedded unnecessarily into the Docker image
* Be protected on the deployment server

---

# Environment Configuration

The backend uses Spring profiles.

The current Docker configuration uses:

```text
dev
```

The application configuration is located at:

```text
src/main/resources/application.yml
src/main/resources/application-dev.yml
```

The active profile can be specified using:

```bash
SPRING_PROFILES_ACTIVE=dev
```

---

# Running Locally

## 1. Navigate to the backend

```bash
cd insurance-backend
```

## 2. Start PostgreSQL

Make sure PostgreSQL is running.

For example:

```bash
sudo systemctl start postgresql
```

Verify:

```bash
sudo systemctl status postgresql
```

---

## 3. Configure Firebase

Make sure the Firebase service-account configuration is available to the application.

---

## 4. Run the backend

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On systems where the wrapper does not have execute permissions:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Alternatively:

```bash
mvn spring-boot:run
```

---

## 5. Verify the backend

The backend runs on:

```text
http://localhost:8092
```

The API base path is:

```text
/api/insurance
```

---

# Building the Backend Locally

Build the project:

```bash
./mvnw clean package
```

Skip tests:

```bash
./mvnw clean package -DskipTests
```

The resulting JAR is generated in:

```text
target/
```

It can be started using:

```bash
java -jar target/*.jar
```

---

# Running with Docker

The backend is containerized using a multi-stage Docker build.

The Docker build:

1. Uses Java 17 for compilation
2. Downloads Maven dependencies
3. Builds the Spring Boot application
4. Creates a smaller Java runtime image
5. Runs the generated JAR

---

# Backend Docker Configuration

The backend container is named:

```text
insurance-portal-api
```

It listens internally on:

```text
8092
```

It is not directly exposed to the host in the current Compose configuration.

The frontend accesses it through the Docker network.

---

# Running the Complete Application with Docker

From the project root:

```bash
cd insurance_Project
```

Start the application:

```bash
docker compose up -d
```

Check the containers:

```bash
docker compose ps
```

Expected services:

```text
insurance-portal
insurance-portal-api
insurance-portal-db
```

---

# Docker Architecture

The complete application consists of three services.

```text
                         Browser
                            |
                            | :8080
                            v
                 ┌────────────────────┐
                 │   insurance-portal │
                 │   React + Nginx    │
                 └─────────┬──────────┘
                           |
                           | /api/*
                           v
                 ┌────────────────────┐
                 │ insurance-portal-api│
                 │    Spring Boot     │
                 │       :8092        │
                 └─────────┬──────────┘
                           |
                           v
                 ┌────────────────────┐
                 │ insurance-portal-db│
                 │     PostgreSQL     │
                 │       :5432       │
                 └────────────────────┘
```

All three services communicate through the Docker network:

```text
insurance-network
```

---

# Database Persistence in Docker

PostgreSQL uses a Docker named volume:

```text
insurance_postgres_data
```

This allows database data to survive container recreation.

For example:

```bash
docker compose down
```

does not remove the named database volume.

Therefore, starting the application again with:

```bash
docker compose up -d
```

will preserve the database data.

---

# Starting From a Clean Database

If the database needs to be completely reset:

```bash
docker compose down -v
```

Then:

```bash
docker compose up -d
```

### Warning

`docker compose down -v` removes the PostgreSQL volume and therefore deletes the stored database data.

Use this only when a database reset is intentional.

---

# Docker Environment

The current Docker backend configuration uses:

```text
SPRING_PROFILES_ACTIVE=dev
```

The database connection inside Docker uses:

```text
Host: postgres
Port: 5432
Database: insurance
Username: insurance_user
```

The important point is that Docker services communicate using service names rather than `localhost`.

For example:

```text
jdbc:postgresql://postgres:5432/insurance
```

is correct inside the backend container.

---

# Firebase Credentials in Docker

The Firebase service account is mounted into the backend container at runtime.

The container receives it through:

```text
/run/secrets/firebase-service-account.json
```

The file is mounted read-only.

This approach prevents the service account from being permanently embedded into the backend Docker image.

---

# API Documentation

The project includes SpringDoc OpenAPI.

When the backend is running, the OpenAPI documentation can be accessed through the configured SpringDoc endpoints.

Swagger UI is typically available at:

```text
http://localhost:8092/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8092/v3/api-docs
```

---

# Main Backend Components

## Controllers

Controllers expose the REST API.

```text
AuthController
CustomerController
CustomerProductController
DependantController
ProductController
AdminUserController
```

---

## Services

Services contain the application's business logic.

```text
AppUserService
CustomerService
CustomerProductService
DependantService
ProductService
```

---

## Repositories

Repositories provide database access using Spring Data JPA.

```text
AppUserRepo
CustomerRepo
CustomerProductRepo
DependantRepo
ProductRepo
```

---

## Entities

The primary database/domain entities include:

```text
AppUser
Customer
CustomerProduct
Dependant
Product
Plan
```

Supporting enums include:

```text
ApplicationStatus
PersonType
Role
```

---

# Security

Authentication is based on Firebase Authentication.

The general request flow is:

```text
User
 |
 | Login
 v
Firebase Authentication
 |
 | Firebase ID Token
 v
React Frontend
 |
 | Authorization: Bearer <token>
 v
Spring Boot API
 |
 v
FirebaseAuthFilter
 |
 | Token validation
 v
Authenticated API request
```

The backend therefore does not rely on the frontend alone to determine whether a user is authenticated.

---

# Error Handling

Application-level errors are handled through:

```text
GlobalExceptionHandler
```

Custom API exceptions are represented by:

```text
ApiException
```

This provides a centralized mechanism for returning appropriate API responses.

---

# Database Schema Handling

The application contains:

```text
DbSchemaFixer
```

which performs required schema-related fixes during application startup.

When the backend starts, database connectivity and schema initialization/fixes are performed before the application becomes fully available.

---

# Running the Complete Application

The recommended Docker workflow is to run the application from the project root.

```bash
docker compose up -d
```

Check status:

```bash
docker compose ps
```

Expected result:

```text
insurance-portal       Up
insurance-portal-api   Up
insurance-portal-db    Up (healthy)
```

Open:

```text
http://localhost:8080
```

---

# Viewing Logs

Frontend:

```bash
docker compose logs frontend
```

Backend:

```bash
docker compose logs backend
```

Database:

```bash
docker compose logs postgres
```

Follow backend logs:

```bash
docker compose logs -f backend
```

---

# Stopping the Application

Stop the containers:

```bash
docker compose down
```

This stops and removes the containers and network while preserving the PostgreSQL named volume.

---

# Rebuilding the Application

After backend code changes:

```bash
docker compose build --no-cache backend
docker compose up -d
```

After frontend code/configuration changes:

```bash
docker compose build --no-cache frontend
docker compose up -d
```

To rebuild everything:

```bash
docker compose down
docker compose build --no-cache
docker compose up -d
```

---

# Health Verification

After starting Docker:

```bash
docker compose ps
```

The PostgreSQL service should report:

```text
healthy
```

The backend should report:

```text
Up
```

The frontend should report:

```text
Up
```

The complete application can then be accessed at:

```text
http://localhost:8080
```

---

# Development Workflow

For normal backend development, Docker is not required.

A developer can run:

```text
PostgreSQL
    ↓
Spring Boot
    ↓
localhost:8092
```

and run the frontend separately:

```text
Vite
    ↓
localhost:5173
```

For integrated testing, Docker Compose can run the complete stack:

```text
React + Nginx
      ↓
Spring Boot
      ↓
PostgreSQL
```

---

# Production / UAT Deployment

The Docker configuration is also structured to support deployment to a UAT environment.

The intended architecture is:

```text
Internet / Internal Network
          |
          v
     Reverse Proxy
        Nginx
          |
          v
   insurance-portal
       :8080
          |
          v
   insurance-portal-api
       :8092
          |
          v
   insurance-portal-db
       :5432
```

The external reverse proxy can terminate HTTPS and forward requests to the frontend container.

The PostgreSQL database and backend do not need to be directly exposed to external users.

---

# Security Considerations

The following files/configuration should be protected:

```text
firebase-service-account.json
.env
.env.docker
```

In particular:

* Do not commit Firebase service-account credentials.
* Do not expose PostgreSQL publicly.
* Do not expose the Spring Boot backend publicly unless required.
* Use HTTPS in UAT/production.
* Use appropriate database credentials for UAT/production.
* Use environment-specific Spring profiles.
* Keep secrets outside Docker images where possible.

---

# Useful Docker Commands

### List running containers

```bash
docker ps
```

### List Compose services

```bash
docker compose ps
```

### Start services

```bash
docker compose up -d
```

### Stop services

```bash
docker compose down
```

### View all logs

```bash
docker compose logs
```

### Follow backend logs

```bash
docker compose logs -f backend
```

### Rebuild backend

```bash
docker compose build --no-cache backend
```

### Rebuild frontend

```bash
docker compose build --no-cache frontend
```

### Reset database

```bash
docker compose down -v
docker compose up -d
```

Use the database reset command only when intentionally deleting existing database data.

---

# Summary

The Insurance Portal backend provides the central business and data layer for the application.

It is responsible for:

* REST API functionality
* Authentication token validation
* Customer management
* Insurance product management
* Dependant management
* Insurance applications
* Application review
* Administrative functionality
* PostgreSQL persistence
* PDF generation
* API documentation

The backend can be run independently for development or as part of the complete Dockerized Insurance Portal.
