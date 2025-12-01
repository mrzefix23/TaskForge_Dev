package com.taskforge.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour le gestionnaire global d'exceptions (GlobalExceptionHandler).
 * Vérifie que chaque exception métier est correctement interceptée et transformée
 * en une réponse HTTP appropriée (code statut et message JSON).
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    /**
     * Initialise l'instance du gestionnaire d'exceptions avant chaque test.
     */
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    /**
     * Vérifie que l'exception UsernameAlreadyExists renvoie une réponse 400 Bad Request
     * avec le message d'erreur correct.
     */
    @Test
    void handleUsernameExists_shouldReturnBadRequestWithMessage() {
        UsernameAlreadyExists exception = new UsernameAlreadyExists("Username 'testuser' already exists");

        ResponseEntity<?> response = exceptionHandler.handleUsernameExists(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "Username 'testuser' already exists");
    }

    /**
     * Vérifie que l'exception EmailAlreadyExists renvoie une réponse 400 Bad Request
     * avec le message d'erreur correct.
     */
    @Test
    void handleEmailExists_shouldReturnBadRequestWithMessage() {
        EmailAlreadyExists exception = new EmailAlreadyExists("Email 'test@example.com' already exists");

        ResponseEntity<?> response = exceptionHandler.handleEmailExists(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "Email 'test@example.com' already exists");
    }

    /**
     * Vérifie que l'exception InvalidCredentialsException renvoie une réponse 401 Unauthorized
     * avec le message d'erreur correct.
     */
    @Test
    void handleInvalidCredentials_shouldReturnUnauthorizedWithMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid username or password");

        ResponseEntity<?> response = exceptionHandler.handleInvalidCredentials(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "Invalid username or password");
    }

    /**
     * Vérifie que l'exception DuplicateUserStoryTitleException renvoie une réponse 400 Bad Request
     * avec le message d'erreur correct.
     */
    @Test
    void handleDuplicateUserStoryTitle_shouldReturnBadRequestWithMessage() {
        DuplicateUserStoryTitleException exception = new DuplicateUserStoryTitleException("User story title 'Implement login' already exists");

        ResponseEntity<?> response = exceptionHandler.handleDuplicateUserStoryTitle(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "User story title 'Implement login' already exists");
    }

    /**
     * Vérifie que l'exception DuplicateProjectNameException renvoie une réponse 400 Bad Request
     * avec le message d'erreur correct.
     */
    @Test
    void handleDuplicateProjectName_shouldReturnBadRequestWithMessage() {
        DuplicateProjectNameException exception = new DuplicateProjectNameException("Project name 'TaskForge' already exists");

        ResponseEntity<?> response = exceptionHandler.handleDuplicateProjectName(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "Project name 'TaskForge' already exists");
    }

    /**
     * Vérifie que l'exception ProjectSuppressionException renvoie une réponse 403 Forbidden
     * avec le message d'erreur correct.
     */
    @Test
    void handleProjectSuppression_shouldReturnForbiddenWithMessage() {
        ProjectSuppressionException exception = new ProjectSuppressionException("Cannot delete project: you are not the owner");

        ResponseEntity<?> response = exceptionHandler.handleProjectSuppression(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "Cannot delete project: you are not the owner");
    }

    /**
     * Vérifie que l'exception UpdateProjectException renvoie une réponse 400 Bad Request
     * avec le message d'erreur correct.
     */
    @Test
    void handleUpdateProject_shouldReturnBadRequestWithMessage() {
        UpdateProjectException exception = new UpdateProjectException("Cannot update project: invalid data");

        ResponseEntity<?> response = exceptionHandler.handleUpdateProject(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "Cannot update project: invalid data");
    }

    /**
     * Vérifie que le gestionnaire traite correctement une exception avec un message vide.
     */
    @Test
    void handleUsernameExists_shouldWorkWithEmptyMessage() {
        UsernameAlreadyExists exception = new UsernameAlreadyExists("");

        ResponseEntity<?> response = exceptionHandler.handleUsernameExists(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "");
    }

    /**
     * Vérifie que le gestionnaire traite correctement une exception avec un message très long.
     */
    @Test
    void handleInvalidCredentials_shouldHandleLongMessage() {
        String longMessage = "Invalid credentials: " + "a".repeat(500);
        InvalidCredentialsException exception = new InvalidCredentialsException(longMessage);

        ResponseEntity<?> response = exceptionHandler.handleInvalidCredentials(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).hasSize(longMessage.length());
    }

    /**
     * Vérifie que tous les gestionnaires d'exceptions retournent un corps de réponse
     * structuré sous forme de Map (JSON).
     */
    @Test
    void allHandlers_shouldReturnMapWithMessageKey() {
        ResponseEntity<?> response1 = exceptionHandler.handleUsernameExists(new UsernameAlreadyExists("test"));
        ResponseEntity<?> response2 = exceptionHandler.handleEmailExists(new EmailAlreadyExists("test"));
        ResponseEntity<?> response3 = exceptionHandler.handleInvalidCredentials(new InvalidCredentialsException("test"));
        ResponseEntity<?> response4 = exceptionHandler.handleDuplicateUserStoryTitle(new DuplicateUserStoryTitleException("test"));
        ResponseEntity<?> response5 = exceptionHandler.handleDuplicateProjectName(new DuplicateProjectNameException("test"));
        ResponseEntity<?> response6 = exceptionHandler.handleProjectSuppression(new ProjectSuppressionException("test"));
        ResponseEntity<?> response7 = exceptionHandler.handleUpdateProject(new UpdateProjectException("test"));

        assertThat(response1.getBody()).isInstanceOf(Map.class);
        assertThat(response2.getBody()).isInstanceOf(Map.class);
        assertThat(response3.getBody()).isInstanceOf(Map.class);
        assertThat(response4.getBody()).isInstanceOf(Map.class);
        assertThat(response5.getBody()).isInstanceOf(Map.class);
        assertThat(response6.getBody()).isInstanceOf(Map.class);
        assertThat(response7.getBody()).isInstanceOf(Map.class);
    }

    /**
     * Vérifie que différentes exceptions retournent bien des codes de statut HTTP distincts
     * (400, 401, 403) conformément à la logique métier définie.
     */
    @Test
    void differentExceptions_shouldReturnDifferentStatusCodes() {
        ResponseEntity<?> badRequest = exceptionHandler.handleUsernameExists(new UsernameAlreadyExists("test"));
        ResponseEntity<?> unauthorized = exceptionHandler.handleInvalidCredentials(new InvalidCredentialsException("test"));
        ResponseEntity<?> forbidden = exceptionHandler.handleProjectSuppression(new ProjectSuppressionException("test"));

        assertThat(badRequest.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(badRequest.getStatusCode()).isNotEqualTo(unauthorized.getStatusCode());
        assertThat(unauthorized.getStatusCode()).isNotEqualTo(forbidden.getStatusCode());
    }
}