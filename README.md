# Feign Microservices Assignment

A comprehensive microservices application demonstrating inter-service communication using Spring Cloud OpenFeign,
circuit breakers, and resilience patterns.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Modules](#modules)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Running the Services](#running-the-services)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Overview

This project is a **two-service microservices system** implemented for an interview assignment.  
The architecture demonstrates:

- **Inter-service communication using Spring Cloud OpenFeign**
- **Circuit Breaker & Retry using Resilience4j**
- **DTO mapping using MapStruct**
- **Validation & Exception Handling**
- **Swagger/OpenAPI documentation**
- **H2 in-memory databases**
- **Clean layered architecture (Controller → Service → Mapper → Repository)**

## Architecture

### System Architecture

```
┌─────────────────┐         ┌─────────────────┐
│  Profile Service│────────▶│   User Service  │
│   (Port 8082)   │  Feign  │   (Port 8081)   │
│                 │  Client │                 │
└─────────────────┘         └─────────────────┘
         │                           │
         │                           │
         ▼                           ▼
    ┌─────────┐                 ┌─────────┐
    │ H2 DB   │                 │ H2 DB   │
    │(Profiles)│                 │ (Users) │
    └─────────┘                 └─────────┘
```

### Service Architecture (3-Layer)

Each microservice follows a classic 3-layer architecture:

1. **Controller Layer**: Handles HTTP requests/responses
2. **Service Layer**: Contains business logic
3. **Repository Layer**: Handles data persistence

Additional components:

- **DTO Layer**: Data Transfer Objects for API contracts
- **Mapper Layer**: MapStruct for Entity ↔ DTO conversion
- **Exception Handler**: Global exception handling
- **Feign Client**: Inter-service communication (Profile Service only)

## Modules

### 1. User Service (`user-service`)

**Port**: `8081`

**Description**: Core service for user management operations.

**Features**:

- Create, retrieve, and list users with pagination
- Email uniqueness validation
- Comprehensive input validation
- JPA auditing (creation/modification timestamps)
- RESTful API with proper HTTP status codes

**Endpoints**:

- `POST /users` - Create a new user
- `GET /users/{id}` - Get user by ID
- `GET /users` - Get all users (paginated)

**Database**: H2 in-memory database (`userdb`)

**API Documentation**: http://localhost:8081/swagger-ui.html

### 2. Profile Service (`profile-service`)

**Port**: `8082`

**Description**: Service for managing user profiles with integration to User Service.

**Features**:

- Create, retrieve
- Validates user existence via User Service
- Circuit breaker pattern for resilience
- Retry mechanism for transient failures
- Combined user and profile data retrieval

**Endpoints**:

- `POST /profiles` - Create a new profile
- `GET /profiles/{id}/with-user` - Get profile with user details

**Database**: H2 in-memory database (`profilesdb`)

**API Documentation**: http://localhost:8082/swagger-ui.html

**Resilience Configuration**:

- Circuit Breaker: Opens after 50% failure rate (10 call window)
- Retry: 3 attempts with 500ms wait duration
- Automatic recovery from half-open state

## Technology Stack

### Core Technologies

- **Java**: 21
- **Spring Boot**: 3.2.5
- **Spring Cloud**: 2023.0.0
- **Maven**: Build tool and dependency management

### Spring Framework Modules

- **Spring Boot Web**: RESTful web services
- **Spring Data JPA**: Database operations and persistence
- **Spring Boot Validation**: Input validation
- **Spring Cloud OpenFeign**: Declarative HTTP client
- **Resilience4j**: Circuit breaker and retry patterns

### Additional Libraries

- **MapStruct**: 1.5.5.Final - Type-safe bean mapping
- **Lombok**: 1.18.42 - Reducing boilerplate code
- **SpringDoc OpenAPI**: 2.6.0 - API documentation (Swagger UI)
- **H2 Database**: In-memory database for development

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/zahraazadiahmadabadi/feign-microservices-assignment.git
cd feign-microservices-assignment
```

### 2. Build the Project (Generate Executable JAR Files)

Before running Docker, build all modules from the root directory
```bash
mvn clean package -DskipTests
```
This will:

- Compile all modules
- Create executable Spring Boot JARs for both services
  - user-service/target/user-service-0.0.1-SNAPSHOT.jar

  - profile-service/target/profile-service-0.0.1-SNAPSHOT.jar

### 3. Run the Services with Docker (Recommended)

Prerequisites: Docker Desktop installed and running
From the project root (where docker-compose.yml exists), run

```bash
docker compose build
docker compose up
```

his will:

- Build Docker images for
  - user-service (port 8081)
  - profile-service (port 8082)

- Start both services in a shared Docker network

- Pass USER_SERVICE_URL=http://user-service:8081 to profile-service
  → enabling service-to-service communication inside Docker

To run containers in the background:

```bash
docker compose up -d
```

To stop all containers:

```bash
docker compose down
```

After startup:

- User Service → http://localhost:8081/swagger-ui.html
- Profile Service → http://localhost:8082/swagger-ui.html

## 4. (Optional) Run Services Locally Without Docker

### Option 1: Run Services Individually

#### Start User Service

```bash
cd user-service
./mvnw spring-boot:run
# Or: java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

User Service will start on: **http://localhost:8081**

#### Start Profile Service

In a new terminal:

```bash
cd profile-service
./mvnw spring-boot:run
# Or: java -jar target/profile-service-0.0.1-SNAPSHOT.jar
```

Profile Service will start on: **http://localhost:8082**

**Important**: Start User Service before Profile Service, as Profile Service depends on it.

## API Documentation

### User Service API Docs

Once User Service is running:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

### Profile Service API Docs

Once Profile Service is running:

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs

### H2 Database Console

Access H2 console for database inspection:

- **User Service DB**: http://localhost:8081/h2-console
    - JDBC URL: `jdbc:h2:mem:userdb`
    - Username: `sa`
    - Password: (empty)

- **Profile Service DB**: http://localhost:8082/h2-console
    - JDBC URL: `jdbc:h2:mem:profilesdb`
    - Username: `sa`
    - Password: (empty)

## Test Structure

Each service includes:

1. **Integration Tests**: Test service layer with database
2. **Web MVC Tests**: Test REST controllers with MockMvc

### Test Coverage

- Service layer business logic
- Controller endpoints
- Exception handling
- Validation logic
- Repository queries




