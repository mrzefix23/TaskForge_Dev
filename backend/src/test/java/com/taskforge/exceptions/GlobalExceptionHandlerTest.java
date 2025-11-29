package com.taskforge.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

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

    @Test
    void handleUsernameExists_shouldWorkWithEmptyMessage() {
        UsernameAlreadyExists exception = new UsernameAlreadyExists("");

        ResponseEntity<?> response = exceptionHandler.handleUsernameExists(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("message", "");
    }

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