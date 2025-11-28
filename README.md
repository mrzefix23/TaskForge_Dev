# TaskForge

TaskForge is a web-based project management application that allows users to create projects, manage issues and tasks, collaborate with their team, and track work progress in real time. The goal is to centralize project tracking and enhance team productivity.

## Project Structure

- **`backend/`**: Contains the Spring Boot backend application, including RESTful APIs, database models, and business logic.
- **`frontend/`**: Contains the Angular frontend application, including components, services, and UI design.

## Prerequisites

- **Java**: Version 21 or higher
- **Node.js**: Version 20 or higher
- **Maven**
- **NPM**

---

## Environment Configuration

Before starting the application, you need to configure the `.env` files for the backend and database. Example `.env` files are provided in the repository.

### 1. Copy `.env.example` Files

1. Navigate to the project root directory.
2. Copy the example `.env` files to their respective locations:
   ```bash
   cp backend/src/main/resources/.env.example backend/src/main/resources/.env
   cp .env.example .env
---

## Running the Application

### 1. Clone the Repository

```bash
git clone https://github.com/mrzefix23/TaskForge_Dev.git
cd TaskForge_Dev
```

### 2. Backend Setup

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```

2. Install dependencies and build the project:
   ```bash
   mvn clean install
   ```

3. Run the backend application:
   ```bash
   mvn spring-boot:run
   ```

4. The backend API will be accessible at `http://localhost:8080`.

### 3. Frontend Setup

1. Navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run the development server:
   ```bash
   ng serve
   ```

4. Open your browser and navigate to `http://localhost:4200`.

---

## Running with Docker

Ensure you have Docker and Docker Compose installed on your machine.

### 1. Build and Start the Application

1. Ensure Docker is installed and running.

2. Build and start the application using Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. The frontend will be accessible at `http://localhost:4200`.

4. The backend API will be accessible at `http://localhost:8080/`.

### 2. Access the Database

You can connect to the PostgreSQL database using a database client at `localhost:5432`. The credentials (username, password, and database name) should be configured in your `.env` file.

---

## Additional Commands

### Stop the Application
To stop the application, run:
```bash
docker-compose down -v
```

### Rebuild the Application
To rebuild the application with the latest changes, run:
```bash
docker-compose up -d --build
```

---

## Testing

### Backend Tests
To run backend tests, navigate to the `backend` directory and execute:
```bash
mvn test
```

To see code coverage reports, use:
```bash
mvn exec:exec@coverage-report
```

You can add an alias to your shell configuration for convenience:
```bash
alias mvn-coverage="mvn exec:exec@coverage-report"
```

### Frontend Tests
To run frontend tests, navigate to the `frontend` directory and execute:
```bash
npm test
```

---