# 🔐 Secure API Gateway for NMS Northbound Interfaces

## 📌 Project Overview

The **Secure API Gateway for NMS Northbound Interfaces** is a security-focused application designed to protect communication between external clients and Network Management System (NMS) backend services.

The project uses an API Gateway to provide a centralized entry point for incoming API requests. It implements authentication, authorization, request validation, and traffic management to ensure secure communication between clients and network management services.

The gateway acts as an intermediate layer between external applications and NMS backend APIs, reducing direct exposure of backend services.

## 🎯 Objectives

- Provide secure access to NMS REST APIs.
- Implement JWT-based authentication and authorization.
- Centralize API request handling through Kong API Gateway.
- Validate incoming API requests.
- Control API traffic using rate limiting.
- Protect backend services from unauthorized access.
- Simplify API management and deployment using Docker.

## 🏗️ System Architecture

```text
        External Client
              |
              v
       Kong API Gateway
              |
              v
       Authentication
          (JWT)
              |
              v
        Authorization
              |
              v
      Request Validation
              |
              v
        Rate Limiting
              |
              v
        Security Inspection
              |
              v
         NMS Backend
              |
       +------+------+
       |             |
       v             v
    Devices        Alarms
       |             |
       +------+------+
              |
              v
           MySQL
```

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | REST API development |
| Spring Security | Application security |
| JWT | Authentication |
| Kong Gateway OSS | API Gateway |
| MySQL | Database |
| React.js | Frontend development |
| Docker | Containerization |
| Maven | Dependency management |
| REST API | Client-server communication |

## ✨ Key Features

### 1. API Gateway
- Provides a centralized entry point for external API requests.
- Routes requests to the appropriate backend services.
- Hides direct backend access from external clients.

### 2. Authentication and Authorization
- Implements JWT-based authentication.
- Protects backend endpoints from unauthorized access.
- Supports role-based access control.

### 3. Request Validation
- Validates incoming API requests.
- Prevents invalid data from reaching backend services.

### 4. Rate Limiting
- Controls the number of API requests within a specified time.
- Helps prevent excessive API traffic.

### 5. NMS REST APIs
- Device management operations.
- Alarm management operations.
- Communication with the MySQL database.

### 6. Docker Integration
- Uses Docker to simplify application deployment.
- Uses Docker Compose to manage gateway-related services.
- Supports consistent deployment environments.

## 📂 Project Structure

```text
secure-api-gateway/
│
├── frontend/
│
├── kong/
│   └── kong.yml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/secure_api_gateway/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │
│   └── test/
│
├── docker-compose-kong.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## ⚙️ Prerequisites

Install the following before running the project:

- Java Development Kit (JDK)
- Maven
- Node.js and npm
- MySQL
- Docker Desktop
- Git

## 🚀 Installation and Execution

### Step 1: Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/Secure-API-Gateway-for-NMS.git
```

### Step 2: Navigate to the project

```bash
cd Secure-API-Gateway-for-NMS
```

### Step 3: Configure MySQL

Create a MySQL database and configure the database URL, username, and password in the application configuration.

Never commit actual database credentials to GitHub.

### Step 4: Run the Spring Boot Backend

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

The backend runs on its configured application port (default: 8080).

### Step 5: Start Kong API Gateway

Make sure Docker Desktop is running.

Execute:

```bash
docker compose -f docker-compose-kong.yml up -d
```

Kong services:

- Proxy: `http://localhost:8000`
- Admin API: `http://localhost:8001`

### Step 6: Run the React Frontend

Open another terminal:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the frontend:

```bash
npm run dev
```

Open the frontend URL displayed in the terminal.

## 🔍 API Testing

APIs can be tested using Postman.

Example API resources:

| Module | Endpoint |
|---|---|
| Devices | `/api/devices` |
| Alarms | `/api/alarms` |

Requests should be sent through the Kong Gateway proxy after gateway routing is configured.

For protected endpoints, include the JWT access token in the Authorization header:

```text
Authorization: Bearer YOUR_JWT_TOKEN
```

## 🐳 Docker Deployment

Docker is used to containerize and manage application services.

Kong uses DB-less mode with declarative configuration through `kong/kong.yml`.

Start the gateway:

```bash
docker compose -f docker-compose-kong.yml up -d
```

Stop the services:

```bash
docker compose -f docker-compose-kong.yml down
```

## 🔒 Security Considerations

- JWT-based authentication.
- Role-based authorization.
- Centralized API access management.
- Request validation.
- Rate limiting.
- Backend service protection.
- Secure handling of application credentials.

## 🎓 Learning Outcomes

- Understanding API Gateway architecture.
- Implementing authentication using Spring Security and JWT.
- Developing RESTful APIs using Spring Boot.
- Configuring Kong API Gateway.
- Working with MySQL database integration.
- Containerizing applications using Docker.
- Understanding secure communication between external clients and backend services.

## 👨‍💻 Author

**Rubavelan**

Java Full Stack Developer | Software Development Enthusiast

---

⭐ If you find this project useful, consider giving the repository a star!
