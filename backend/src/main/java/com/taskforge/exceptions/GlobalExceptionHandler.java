package com.taskforge.exceptions;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UsernameAlreadyExists.class)
    public ResponseEntity<?> handleUsernameExists(UsernameAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExists.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateUserStoryTitleException.class)
    public ResponseEntity<?> handleDuplicateUserStoryTitle(DuplicateUserStoryTitleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateProjectNameException.class)
    public ResponseEntity<?> handleDuplicateProjectName(DuplicateProjectNameException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ProjectSuppressionException.class)
    public ResponseEntity<?> handleProjectSuppression(ProjectSuppressionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UpdateProjectException.class)
    public ResponseEntity<?> handleUpdateProject(UpdateProjectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateSprintNameException.class)
    public ResponseEntity<?> handleDuplicateSprintName(DuplicateSprintNameException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidSprintDateException.class)
    public ResponseEntity<?> handleInvalidSprintDate(InvalidSprintDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Gère les RuntimeException génériques et les mappe vers les codes HTTP appropriés.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        
        // Mapper les messages d'erreur aux codes HTTP appropriés
        if (message != null) {
            if (message.contains("User is not a member of this project") 
                || message.contains("Vous n'avez pas accès à ce projet")
                || message.contains("Only project owner")
                || message.contains("Les colonnes par défaut ne peuvent pas être supprimées")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", message));
            }
            
            if (message.contains("not found") 
                || message.contains("non trouvé")
                || message.contains("n'existe pas")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", message));
            }
            
            if (message.contains("existe déjà") 
                || message.contains("already exists")
                || message.contains("Invalid")
                || message.contains("Cannot start sprint")
                || message.contains("Only PLANNED sprints")
                || message.contains("Only ACTIVE sprints")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", message));
            }
        }

        // Par défaut, renvoyer une erreur 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", message != null ? message : "Internal server error"));
    }
}