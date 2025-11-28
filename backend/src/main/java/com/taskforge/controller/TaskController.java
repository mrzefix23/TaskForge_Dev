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

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
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
    
    @GetMapping("/user-story/{userStoryId}")
    public ResponseEntity<List<Task>> getTasksByUserStory(@PathVariable Long userStoryId, Authentication authentication) {
        List<Task> tasks = taskService.getTasksByUserStoryId(userStoryId, authentication.getName());
        return ResponseEntity.ok(tasks);
    }
    
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