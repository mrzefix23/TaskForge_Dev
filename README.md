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
- **`SECURITY.md`**: Security best practices and policies
- **`docker-compose.yml`**: Production-ready Docker configuration (secure)
- **`docker-compose.dev.yml`**: Development Docker configuration (ports exposed)

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

**⚠️ Warning**: Never use `docker-compose.dev.yml` in production as it exposes sensitive ports.

### 2. Access the Database

**En développement local :** Si vous avez besoin d'accéder directement à la base de données, vous pouvez temporairement décommenter le mapping du port 5432 dans `docker-compose.yml`.

**En production Docker :** Utilisez un tunnel SSH ou accédez via le conteneur :
```bash
docker-compose exec database psql -U ${POSTGRES_USER} -d ${POSTGRES_DB}
```

Les informations de connexion sont configurées dans votre fichier `.env`.

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

## API documentation

Une fois l'application lancée, la documentation Swagger est disponible aux adresses suivantes :

### Mode Développement Local (backend seul)
- **Interface Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **Documentation JSON OpenAPI** : `http://localhost:8080/v3/api-docs`

### Mode Docker
- **Interface Swagger UI** : `http://localhost/swagger-ui.html`
- **Documentation JSON OpenAPI** : `http://localhost/v3/api-docs`

### Utiliser l'authentification dans Swagger UI

1. **Se connecter** :
   - Aller sur `http://localhost:8080/swagger-ui.html`
   - Cliquer sur **POST /api/auth/login**
   - Testez avec : 
     ```json
     {
       "username": "votre_nom_utilisateur",
       "password": "votre_mot_de_passe"
     }
     ```
   - Cliquer sur **Execute**
   - Copier le token JWT de la réponse.

2. **Ajouter le token JWT** :
   - Cliquer sur le bouton **Authorize** (icône de cadenas en haut à droite).
   - Dans le champ `Value`, entrer : `Bearer VOTRE_TOKEN_JWT`
   - Cliquer sur **Authorize** puis sur **Close**.

3. **Accéder aux endpoints protégés** :
   - Vous pouvez maintenant accéder aux endpoints protégés en utilisant Swagger UI.

### Endpoints disponibles 

#### Authentification (`/api/auth/`)
- `POST /login` : Se connecter
- `POST /register` : S'inscrire

#### Projets (`/api/projects/`)
- `POST /` : Créer un nouveau projet
- `GET /myprojects` : Obtenir les projets de l'utilisateur connecté
- `GET /{projectId}` : Obtenir les détails d'un projet par son ID
- `PUT /{projectId}` : Mettre à jour un projet par son ID
- `DELETE /{projectId}` : Supprimer un projet par son ID (propriétaire uniquement)

#### User Stories (`/api/user-stories/`)
- `POST /` : Créer une nouvelle user story
- `GET /project/{projectId}` : Obtenir toutes les user stories d'un projet
- `GET /{userStoryId}` : Obtenir les détails d'une user story
- `PUT /{userStoryId}` : Mettre à jour une user story
- `DELETE /{userStoryId}` : Supprimer une user story
- `PUT /{userStoryId}/status` : Mettre à jour le statut d'une user story

#### Sprints (`/api/sprints/`)
- `POST /` : Créer un nouveau sprint
- `GET /project/{projectId}` : Obtenir tous les sprints d'un projet
- `GET /{sprintId}` : Obtenir les détails d'un sprint
- `PUT /{sprintId}` : Mettre à jour un sprint
- `DELETE /{sprintId}` : Supprimer un sprint
- `PUT /{sprintId}/status` : Mettre à jour le statut d'un sprint
- `POST /{sprintId}/user-stories/{userStoryId}` : Ajouter une user story à un sprint
- `DELETE /{sprintId}/user-stories/{userStoryId}` : Retirer une user story d'un sprint
- `GET /{sprintId}/user-stories` : Obtenir toutes les user stories d'un sprint

#### Versions (`/api/versions/`)
- `POST /` : Créer une nouvelle version
- `GET /project/{projectId}` : Obtenir toutes les versions d'un projet
- `GET /{id}` : Obtenir les détails d'une version
- `PUT /{id}` : Mettre à jour une version
- `DELETE /{id}` : Supprimer une version
- `PUT /{id}/status` : Mettre à jour le statut d'une version
- `POST /{versionId}/user-stories/{userStoryId}` : Ajouter une user story à une version
- `DELETE /{versionId}/user-stories/{userStoryId}` : Retirer une user story d'une version
- `GET /{versionId}/user-stories` : Obtenir toutes les user stories d'une version

#### Tâches (`/api/tasks/`)
- `POST /` : Créer une nouvelle tâche
- `GET /user-story/{userStoryId}` : Obtenir toutes les tâches d'une user story
- `GET /{taskId}` : Obtenir les détails d'une tâche
- `PUT /{taskId}` : Mettre à jour une tâche
- `DELETE /{taskId}` : Supprimer une tâche
- `PUT /{taskId}/status` : Mettre à jour le statut d'une tâche
- `PUT /{taskId}/assign/{userId}` : Assigner une tâche à un utilisateur
- `DELETE /{taskId}/unassign` : Désassigner une tâche

#### Colonnes Kanban (`/api/kanban-columns/`)
- `POST /` : Créer une nouvelle colonne Kanban
- `GET /project/{projectId}` : Obtenir toutes les colonnes Kanban d'un projet
- `GET /{columnId}` : Obtenir les détails d'une colonne
- `PUT /{columnId}` : Mettre à jour une colonne
- `DELETE /{columnId}` : Supprimer une colonne
- `PUT /{columnId}/reorder` : Réorganiser l'ordre des colonnes

#### Utilisateurs (`/api/users/`)
- `GET /` : Obtenir la liste des utilisateurs
- `GET /{id}` : Obtenir les détails d'un utilisateur par son ID

---

## Security Notes

### Development vs Production

**⚠️ Important Security Considerations:**

> 📖 **For detailed security guidelines, see [SECURITY.md](./SECURITY.md)**

- **Port Exposure**: In the current `docker-compose.yml`, only port 80 (Nginx) is exposed. The backend (8080) and database (5432) are NOT exposed externally and communicate only through the internal Docker network. This is the secure configuration for production.

- **Database Access**: 
  - In production: Access the database via Docker exec or SSH tunnel
  - For local development: You can temporarily expose port 5432 by uncommenting it in `docker-compose.yml`

- **Swagger Documentation**: 
  - In production, consider disabling Swagger or restricting access
  - Add `SPRINGDOC_SWAGGER_UI_ENABLED=false` to your backend `.env` for production

- **Environment Variables**: 
  - Never commit `.env` files to the repository
  - Use strong passwords and secure JWT secrets in production
  - Change all default credentials before deploying

### HTTPS Configuration

For production deployment, configure HTTPS:
1. Obtain SSL/TLS certificates (e.g., Let's Encrypt)
2. Update `nginx.conf` to listen on port 443
3. Configure automatic HTTP to HTTPS redirect

---

## Schéma

Section où l'on retrouve le schéma des données utilisées dans l'API :

- **AuthResponse** : Réponse d'authentification contenant le token JWT
- **CreateProjectRequest** : Requête pour créer un nouveau projet
- **GrantedAuthority** : Autorité accordée à un utilisateur
- **LoginRequest** : Requête de connexion avec username et password
- **Project** : Modèle représentant un projet
- **RegisterRequest** : Requête d'inscription d'un nouvel utilisateur
- **User** : Modèle représentant un utilisateur
- **UserDto** : Objet de transfert de données pour les informations utilisateur