package com.taskforge.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.models.UserStory;
import com.taskforge.service.UserStoryService;

@RestController
@RequestMapping("/api/user-stories")
public class UserStoryController {
    
    @Autowired
    private UserStoryService userStoryService;
    
    @PostMapping
    public ResponseEntity<UserStory> createUserStory(
            @RequestBody CreateUserStoryRequest request, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.createUserStory(request, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<UserStory>> getUserStoriesByProject(
            @PathVariable Long projectId, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        List<UserStory> userStories = userStoryService.getUserStoriesByProject(projectId, principal.getName());
        return ResponseEntity.ok(userStories);
    }
    
    @GetMapping("/{userStoryId}")
    public ResponseEntity<UserStory> getUserStoryById(
            @PathVariable Long userStoryId, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.getUserStoryById(userStoryId, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    @PutMapping("/{userStoryId}")
    public ResponseEntity<UserStory> updateUserStory(
            @PathVariable Long userStoryId,
            @RequestBody CreateUserStoryRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.updateUserStory(userStoryId, request, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    @DeleteMapping("/{userStoryId}")
    public ResponseEntity<Void> deleteUserStory(
            @PathVariable Long userStoryId, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        userStoryService.deleteUserStory(userStoryId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}