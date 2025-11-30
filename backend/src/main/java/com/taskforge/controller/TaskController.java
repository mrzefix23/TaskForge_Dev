package com.taskforge.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.CreateTaskRequest;
import com.taskforge.exceptions.DuplicateTaskTitleException;
import com.taskforge.exceptions.TaskNotFoundException;
import com.taskforge.models.Task;
import com.taskforge.service.TaskService;

/**
 * Contrôleur REST pour la gestion des tâches.
 * Fournit des points de terminaison pour créer, récupérer, mettre à jour et supprimer des tâches
 * au sein des User Stories.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Crée une nouvelle tâche.
     *
     * @param request        Les détails de la tâche à créer (titre, description, priorité, etc.).
     * @param authentication L'authentification de l'utilisateur courant.
     * @return La tâche créée avec le statut 201 (Created), ou une erreur 400 (Bad Request) en cas de problème.
     */
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request, Authentication authentication) {
        try {
            Task task = taskService.createTask(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (DuplicateTaskTitleException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Récupère une tâche par son identifiant.
     *
     * @param taskId         L'identifiant de la tâche à récupérer.
     * @param authentication L'authentification de l'utilisateur courant.
     * @return La tâche demandée, ou une erreur 404 (Not Found) si elle n'existe pas.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTask(@PathVariable Long taskId, Authentication authentication) {
        try {
            Task task = taskService.getTaskById(taskId, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Récupère la liste des tâches associées à une User Story spécifique.
     *
     * @param userStoryId    L'identifiant de la User Story parente.
     * @param authentication L'authentification de l'utilisateur courant.
     * @return Une liste de tâches appartenant à la User Story.
     */
    @GetMapping("/user-story/{userStoryId}")
    public ResponseEntity<List<Task>> getTasksByUserStory(@PathVariable Long userStoryId, Authentication authentication) {
        List<Task> tasks = taskService.getTasksByUserStoryId(userStoryId, authentication.getName());
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Met à jour une tâche existante.
     *
     * @param taskId         L'identifiant de la tâche à mettre à jour.
     * @param request        Les nouvelles informations de la tâche.
     * @param authentication L'authentification de l'utilisateur courant.
     * @return La tâche mise à jour, ou une erreur 400 (Bad Request) en cas de problème.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId, 
                                       @RequestBody CreateTaskRequest request,
                                       Authentication authentication) {
        try {
            Task task = taskService.updateTask(taskId, request, authentication.getName());
            return ResponseEntity.ok(task);
        } catch (DuplicateTaskTitleException | TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Supprime une tâche.
     *
     * @param taskId         L'identifiant de la tâche à supprimer.
     * @param authentication L'authentification de l'utilisateur courant.
     * @return Une réponse vide avec le statut 204 (No Content) en cas de succès, ou une erreur 404 (Not Found).
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId, Authentication authentication) {
        try {
            taskService.deleteTask(taskId, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}