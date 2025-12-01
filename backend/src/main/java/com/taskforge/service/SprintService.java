package com.taskforge.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskforge.dto.CreateSprintRequest;
import com.taskforge.dto.SprintResponse;
import com.taskforge.exceptions.DuplicateSprintNameException;
import com.taskforge.exceptions.InvalidSprintDateException;
import com.taskforge.models.Project;
import com.taskforge.models.Sprint;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.SprintRepository;
import com.taskforge.repositories.UserStoryRepository;

import jakarta.transaction.Transactional;

@Service
public class SprintService {
    
    @Autowired
    private SprintRepository sprintRepository;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserStoryRepository userStoryRepository;
    
    public Sprint createSprint(CreateSprintRequest request, String username) {
        // Verify user has access to project and is owner
        Project project = projectService.getProjectById(request.getProjectId(), username);
        
        if (!project.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Only project owner can create sprints");
        }
        
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidSprintDateException("La date de fin doit être après la date de début");
        }
        
        // Check for duplicate sprint name in project
        if (sprintRepository.existsByNameAndProjectId(request.getName(), project.getId())) {
            throw new DuplicateSprintNameException("Un sprint avec ce nom existe déjà dans ce projet.");
        }
        
        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .project(project)
                .build();
        
        return sprintRepository.save(sprint);
    }
    
    public List<SprintResponse> getSprintsByProject(Long projectId, String username) {
        // Verify user has access to project
        projectService.getProjectById(projectId, username);
        
        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Sprint getSprintById(Long sprintId, String username) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        
        // Verify user has access to project
        projectService.getProjectById(sprint.getProject().getId(), username);
        
        return sprint;
    }
    
    public Sprint updateSprint(Long sprintId, CreateSprintRequest request, String username) {
        Sprint sprint = getSprintById(sprintId, username);
        
        // Only project owner can update sprint
        if (!sprint.getProject().getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Only project owner can update sprints");
        }
        
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidSprintDateException("La date de fin doit être après la date de début");
        }
        
        // Check for duplicate name (excluding current sprint)
        if (sprintRepository.existsByNameAndProjectId(request.getName(), sprint.getProject().getId())) {
            Sprint existing = sprintRepository.findByNameAndProjectId(request.getName(), sprint.getProject().getId());
            if (existing != null && !existing.getId().equals(sprintId)) {
                throw new DuplicateSprintNameException("Un sprint avec ce nom existe déjà dans ce projet.");
            }
        }
        
        sprint.setName(request.getName());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setStatus(request.getStatus());
        
        return sprintRepository.save(sprint);
    }
    
    @Transactional
    public void deleteSprint(Long sprintId, String username) {
        Sprint sprint = getSprintById(sprintId, username);
        
        // Only project owner can delete sprint
        if (!sprint.getProject().getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Only project owner can delete sprints");
        }
        
        // Remove sprint from all user stories (move them to backlog)
        List<UserStory> userStories = userStoryRepository.findByProjectId(sprint.getProject().getId());
        for (UserStory us : userStories) {
            if (us.getSprint() != null && us.getSprint().getId().equals(sprintId)) {
                us.setSprint(null);
                userStoryRepository.save(us);
            }
        }
        
        sprintRepository.deleteById(sprintId);
    }
    
    @Transactional
    public UserStory assignUserStoryToSprint(Long userStoryId, Long sprintId, String username) {
        Sprint sprint = getSprintById(sprintId, username);
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User story not found"));
        
        // Verify user story belongs to same project as sprint
        if (!userStory.getProject().getId().equals(sprint.getProject().getId())) {
            throw new RuntimeException("User story and sprint must belong to the same project");
        }
        
        // Verify user has access to project
        projectService.getProjectById(sprint.getProject().getId(), username);
        
        userStory.setSprint(sprint);
        return userStoryRepository.save(userStory);
    }
    
    @Transactional
    public UserStory removeUserStoryFromSprint(Long userStoryId, String username) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User story not found"));
        
        // Verify user has access to project
        projectService.getProjectById(userStory.getProject().getId(), username);
        
        userStory.setSprint(null);
        return userStoryRepository.save(userStory);
    }
    
    public List<UserStory> getUserStoriesBySprint(Long sprintId, String username) {
        Sprint sprint = getSprintById(sprintId, username);
        
        return userStoryRepository.findByProjectId(sprint.getProject().getId()).stream()
                .filter(us -> us.getSprint() != null && us.getSprint().getId().equals(sprintId))
                .collect(Collectors.toList());
    }
    
    public List<UserStory> getBacklogUserStories(Long projectId, String username) {
        // Verify user has access to project
        projectService.getProjectById(projectId, username);
        
        return userStoryRepository.findByProjectId(projectId).stream()
                .filter(us -> us.getSprint() == null)
                .collect(Collectors.toList());
    }
    
    private SprintResponse convertToResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus())
                .projectId(sprint.getProject().getId())
                .build();
    }
}
