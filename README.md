# Resource Booking System

Resource Booking System is a backend application developed using Spring Boot.

This application is used to manage resources and reservations such as rooms, vehicles and equipment.

The application has two roles, ADMIN and USER.

ADMIN can manage resources and reservations.

USER can view resources, create reservations and view their own reservations.

# Technologies Used

Java 17
Spring Boot
Spring Web
Spring Security
Spring Data JPA
PostgreSQL
JWT
BCrypt
Jakarta Validation
Lombok
Swagger OpenAPI
Maven
JUnit 5
H2 Database for testing

# Project Structure

The project contains different packages.

config contains security, Swagger and initial data configuration.

controller contains REST API endpoints.

dto contains request and response classes.

entity contains database entities and enums.

exception contains exception handling classes.

repository is used for database operations.

security contains JWT authentication classes.

service contains the business logic.

Application flow:

Controller -> Service -> Repository -> Database

# Authentication

The application uses JWT authentication.

Login API:

POST /auth/login

The user enters email and password.

After successful login, the application returns a JWT token.

The JWT token is used to access protected APIs.

Authorization header:

Authorization: Bearer token

Passwords are stored using BCrypt.

# Roles

## ADMIN

Admin can create resources.
Admin can view resources.
Admin can update resources.
Admin can delete resources.

Admin can create reservations.
Admin can view all reservations.
Admin can update reservations.
Admin can delete reservations.

## USER

User can view resources.
User can create reservations.
User can view their own reservations.

User cannot view reservations created by another user.

While creating a reservation, user information is taken from the JWT token. userId is not required in the request.

# Database Setup

The application uses PostgreSQL.

Database name:

booking_db

Database connection:

jdbc:postgresql://localhost:5432/booking_db

Hibernate creates and updates the required database tables.

# Environment Variables

The following environment variables are used.

DB_PASSWORD contains the PostgreSQL password.

DB_USERNAME contains the PostgreSQL username.

JWT_SECRET contains the JWT secret key.

JWT_EXPIRATION contains the JWT expiration time.

Default database username:

postgres

Default JWT expiration:

86400000 milliseconds

JWT secret should contain at least 32 characters.

# PowerShell Setup

Set PostgreSQL password:

$env:DB_PASSWORD=your_postgres_password

Set JWT secret:

$env:JWT_SECRET=your_secret_key_with_at_least_32_characters

# Running the Application

Java 17 or above and PostgreSQL are required.

Compile the project:

.\mvnw.cmd compile

Run the application:

.\mvnw.cmd spring-boot:run

The application runs on port 8080.

# Seed Users

Two users are created automatically when the application starts.

ADMIN

Email: admin@example.com
Password: Admin@123

USER

Email: user@example.com
Password: User@123

The application checks whether the users already exist before creating them.

# API Endpoints

Login:

POST /auth/login

Resources:

GET /resources
GET /resources/{id}
POST /resources
PUT /resources/{id}
DELETE /resources/{id}

ADMIN and USER can view resources.

Only ADMIN can create, update and delete resources.

Reservations:

POST /reservations
GET /reservations
GET /reservations/{id}
PUT /reservations/{id}
DELETE /reservations/{id}

ADMIN can view all reservations.

USER can create reservations and view only their own reservations.

Only ADMIN can update and delete reservations.

# Filtering Pagination and Sorting

Reservations can be filtered using:

status
minPrice
maxPrice

Pagination parameters:

page
size

Sorting parameter:

sort

Example:

GET /reservations?status=PENDING&minPrice=100&maxPrice=5000&page=0&size=10&sort=price,asc

Reservation status values:

PENDING
CONFIRMED
CANCELLED

# Swagger

Swagger UI:

http://localhost:8080/swagger-ui/index.html

OpenAPI documentation:

http://localhost:8080/v3/api-docs

First login using POST /auth/login.

Copy the JWT token returned after login.

Use the token in the Swagger Authorize option to access protected APIs.

# Example Requests

## Login

POST /auth/login

Email: admin@example.com
Password: Admin@123

## Create Resource

POST /resources

Example data:

name: Conference Room A
description: Room with a projector and 10 seats
type: ROOM
price: 100.00
available: true

Only ADMIN can create a resource.

## Create Reservation

POST /reservations

Example data:

resourceId: 1
startTime: 2026-09-01T10:00:00
endTime: 2026-09-01T12:00:00
price: 150.00

userId is not required because the logged in user is identified using the JWT token.

# Testing

The project contains tests for the main APIs and security rules.

H2 in-memory database is used for testing.

Run tests:

.\mvnw.cmd test

Tests cover login, resource access, reservation creation, validation, user reservation ownership and admin access.