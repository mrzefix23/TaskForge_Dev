package com.taskforge.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;

@Service
public class UserStoryService {
    
    @Autowired
    private UserStoryRepository userStoryRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectService projectService;
    
    public UserStory createUserStory(CreateUserStoryRequest request, String username) {
        // Verify user has access to project
        Project project = projectService.getProjectById(request.getProjectId(), username);
        
        UserStory userStory = UserStory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .project(project)
                .build();
        
        // Assign user if provided
        if (request.getAssignedToUsername() != null && !request.getAssignedToUsername().isEmpty()) {
            User assignedUser = userRepository.findByUsername(request.getAssignedToUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.getAssignedToUsername()));
            
            // Verify assigned user is member of project
            if (!project.getMembers().contains(assignedUser)) {
                throw new RuntimeException("Assigned user is not a member of this project");
            }
            userStory.setAssignedTo(assignedUser);
        }
        
        return userStoryRepository.save(userStory);
    }
    
    public List<UserStory> getUserStoriesByProject(Long projectId, String username) {
        // Verify user has access to project
        projectService.getProjectById(projectId, username);
        return userStoryRepository.findByProjectId(projectId);
    }
    
    public UserStory getUserStoryById(Long userStoryId, String username) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User story not found"));
        
        // Verify user has access to project
        projectService.getProjectById(userStory.getProject().getId(), username);
        
        return userStory;
    }
    
    public UserStory updateUserStory(Long userStoryId, CreateUserStoryRequest request, String username) {
        UserStory userStory = getUserStoryById(userStoryId, username);
        
        userStory.setTitle(request.getTitle());
        userStory.setDescription(request.getDescription());
        userStory.setPriority(request.getPriority());
        userStory.setStatus(request.getStatus());
        
        if (request.getAssignedToUsername() != null && !request.getAssignedToUsername().isEmpty()) {
            User assignedUser = userRepository.findByUsername(request.getAssignedToUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!userStory.getProject().getMembers().contains(assignedUser)) {
                throw new RuntimeException("Assigned user is not a member of this project");
            }
            userStory.setAssignedTo(assignedUser);
        } else {
            userStory.setAssignedTo(null);
        }
        
        return userStoryRepository.save(userStory);
    }
    
    public void deleteUserStory(Long userStoryId, String username) {
        UserStory userStory = getUserStoryById(userStoryId, username);
        
        // Only project owner can delete user stories
        if (!userStory.getProject().getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Only project owner can delete user stories");
        }
        
        userStoryRepository.deleteById(userStoryId);
    }
}