package com.taskforge.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.exceptions.DuplicateUserStoryTitleException;
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
        Project project = projectService.getProjectById(request.getProjectId(), username);
        
        if (userStoryRepository.existsByTitleAndProjectId(request.getTitle(), project.getId())) {
            throw new DuplicateUserStoryTitleException("Une user story avec ce titre existe déjà dans ce projet.");
        }

        UserStory userStory = UserStory.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .project(project)
                .assignedTo(new HashSet<>())
                .build();
        
        if (request.getAssignedToUsernames() != null && !request.getAssignedToUsernames().isEmpty()) {
            for (String assignedUsername : request.getAssignedToUsernames()) {
                User assignedUser = userRepository.findByUsername(assignedUsername)
                    .orElseThrow(() -> new RuntimeException("User not found: " + assignedUsername));
            
                if (!project.getMembers().contains(assignedUser)) {
                    throw new RuntimeException("User " + assignedUsername + " is not a member of this project");
                }
                
                userStory.getAssignedTo().add(assignedUser);
            }
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
        
        if (userStoryRepository.existsByTitleAndProjectId(request.getTitle(), userStory.getProject().getId())) {
            UserStory existing = userStoryRepository.findByTitleAndProjectId(request.getTitle(), userStory.getProject().getId());
            if (existing != null && !existing.getId().equals(userStoryId)) {
                throw new DuplicateUserStoryTitleException("Une user story avec ce titre existe déjà dans ce projet.");
            }
        }
        
        userStory.setTitle(request.getTitle());
        userStory.setDescription(request.getDescription());
        userStory.setPriority(request.getPriority());
        userStory.setStatus(request.getStatus());
        
        userStory.getAssignedTo().clear();
        
        if (request.getAssignedToUsernames() != null && !request.getAssignedToUsernames().isEmpty()) {
            for (String assignedUsername : request.getAssignedToUsernames()) {
                User assignedUser = userRepository.findByUsername(assignedUsername)
                        .orElseThrow(() -> new RuntimeException("User not found: " + assignedUsername));
                
                if (!userStory.getProject().getMembers().contains(assignedUser)) {
                    throw new RuntimeException("User " + assignedUsername + " is not a member of this project");
                }
                
                userStory.getAssignedTo().add(assignedUser);
            }
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