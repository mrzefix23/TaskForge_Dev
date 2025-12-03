# TaskForge

TaskForge is a web-based project management application that allows users to create projects, manage issues and tasks, collaborate with their team, and track work progress in real time. The goal is to centralize project tracking and enhance team productivity.

## Features

- 🔐 **User Authentication**: Secure JWT-based authentication and authorization
- 📊 **Project Management**: Create and manage multiple projects
- 📝 **User Stories**: Define and track user stories with status updates
- 🏃 **Sprint Planning**: Organize work into sprints with start/end dates
- ✅ **Task Management**: Break down user stories into tasks and assign them to team members
- 📋 **Kanban Board**: Visual workflow management with customizable columns
- 🎯 **Version Management**: Plan and track software releases
- 👥 **Team Collaboration**: Assign tasks and track team member contributions
- 📚 **API Documentation**: Interactive Swagger UI for API exploration

## Project Structure

- **`backend/`**: Contains the Spring Boot backend application, including RESTful APIs, database models, and business logic.
- **`frontend/`**: Contains the Angular frontend application, including components, services, and UI design.
- **`ADMIN_GUIDE.md`**: Comprehensive administrator guide for production deployment
- **`docker-compose.yml`**: Docker Compose configuration for production deployment

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
   ```

### Generate the JWT secret key

To generate a Base64 secret key to use as the value for SECURITY_JWT_SECRET in the backend `.env` file, you can use openssl. Example (generates 64 random bytes encoded in Base64):

```bash
openssl rand -base64 64
```

Copy the output and place it in your backend `.env` file as follows:

```bash
# backend/src/main/resources/.env
SECURITY_JWT_SECRET=the_value_generated_by_openssl
SECURITY_JWT_EXPIRATION-MS=3600000
```

> Note: The decoded key must be sufficiently long (>= 32 bytes) to be used with HMAC SHA (requirement by the JWT library). The above example produces an appropriate security key.
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

#### Production Mode (Recommended - Secure)

1. Ensure Docker is installed and running.

2. Build and start the application using Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. The frontend will be accessible at `http://localhost` (port 80 via Nginx).

4. The backend API will be accessible at `http://localhost/api/`.

5. The Swagger documentation will be accessible at `http://localhost/swagger-ui.html`.

#### Development Mode (Ports Exposed)

For local development where you need direct access to backend and database:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

This exposes:
- Frontend: `http://localhost`
- Backend API: `http://localhost:8080/api/`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Database: `localhost:5432`


### 2. Access the Database

**In local development:** If you need direct access to the database, you can temporarily uncomment the port 5432 mapping in `docker-compose.yml`.

**In Docker production:** Use an SSH tunnel or access via the container:
```bash
docker-compose exec database psql -U ${POSTGRES_USER} -d ${POSTGRES_DB}
```

Connection credentials are configured in your `.env` file.

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

## API Documentation

The API documentation is available via Swagger UI. It provides an interactive interface to explore and test the API endpoints.

### Local Development Mode (backend only)
- **Swagger UI Interface**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Documentation**: `http://localhost:8080/v3/api-docs`

### Docker Mode
- **Swagger UI Interface**: `http://localhost/swagger-ui.html`
- **OpenAPI JSON Documentation**: `http://localhost/v3/api-docs`

### Using Authentication in Swagger UI

1. **Login**:
   - Go to `http://localhost:8080/swagger-ui.html`
   - Click on **POST /api/auth/login**
   - Test with: 
     ```json
     {
       "username": "your_username",
       "password": "your_password"
     }
     ```
   - Click **Execute**
   - Copy the JWT token from the response.

2. **Add the JWT token**:
   - Click the **Authorize** button (lock icon at the top right).
   - In the `Value` field, enter: `Bearer YOUR_JWT_TOKEN`
   - Click **Authorize** then **Close**.

3. **Access protected endpoints**:
   - You can now access protected endpoints using Swagger UI.

### Available Endpoints

#### Authentication (`/api/auth/`)
- `POST /login`: Login
- `POST /register`: Register

#### Projects (`/api/projects/`)
- `POST /`: Create a new project
- `GET /myprojects`: Get projects for the logged-in user
- `GET /{projectId}`: Get project details by ID
- `PUT /{projectId}`: Update a project by ID
- `DELETE /{projectId}`: Delete a project by ID (owner only)

#### User Stories (`/api/user-stories/`)
- `POST /`: Create a new user story
- `GET /project/{projectId}`: Get all user stories for a project
- `GET /{userStoryId}`: Get user story details
- `PUT /{userStoryId}`: Update a user story
- `DELETE /{userStoryId}`: Delete a user story
- `PUT /{userStoryId}/status`: Update user story status

#### Sprints (`/api/sprints/`)
- `POST /`: Create a new sprint
- `GET /project/{projectId}`: Get all sprints for a project
- `GET /{sprintId}`: Get sprint details
- `PUT /{sprintId}`: Update a sprint
- `DELETE /{sprintId}`: Delete a sprint
- `PUT /{sprintId}/status`: Update sprint status
- `POST /{sprintId}/user-stories/{userStoryId}`: Add a user story to a sprint
- `DELETE /{sprintId}/user-stories/{userStoryId}`: Remove a user story from a sprint
- `GET /{sprintId}/user-stories`: Get all user stories in a sprint

#### Versions (`/api/versions/`)
- `POST /`: Create a new version
- `GET /project/{projectId}`: Get all versions for a project
- `GET /{id}`: Get version details
- `PUT /{id}`: Update a version
- `DELETE /{id}`: Delete a version
- `PUT /{id}/status`: Update version status
- `POST /{versionId}/user-stories/{userStoryId}`: Add a user story to a version
- `DELETE /{versionId}/user-stories/{userStoryId}`: Remove a user story from a version
- `GET /{versionId}/user-stories`: Get all user stories in a version

#### Tasks (`/api/tasks/`)
- `POST /`: Create a new task
- `GET /user-story/{userStoryId}`: Get all tasks for a user story
- `GET /{taskId}`: Get task details
- `PUT /{taskId}`: Update a task
- `DELETE /{taskId}`: Delete a task
- `PUT /{taskId}/status`: Update task status
- `PUT /{taskId}/assign/{userId}`: Assign a task to a user
- `DELETE /{taskId}/unassign`: Unassign a task

#### Kanban Columns (`/api/kanban-columns/`)
- `POST /`: Create a new Kanban column
- `GET /project/{projectId}`: Get all Kanban columns for a project
- `GET /{columnId}`: Get column details
- `PUT /{columnId}`: Update a column
- `DELETE /{columnId}`: Delete a column
- `PUT /{columnId}/reorder`: Reorder columns

#### Users (`/api/users/`)
- `GET /`: Get list of users
- `GET /{id}`: Get user details by ID

---

## Schema

Section where the data schema used in the API can be found:
- **AuthResponse**: Authentication response containing the JWT token
- **CreateProjectRequest**: Request to create a new project
- **GrantedAuthority**: Authority granted to a user
- **LoginRequest**: Login request with username and password
- **Project**: Model representing a project
- **RegisterRequest**: Request to register a new user
- **User**: Model representing a user
- **UserDto**: Data transfer object for user information