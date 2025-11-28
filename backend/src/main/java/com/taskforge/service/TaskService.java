package com.taskforge.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskforge.dto.CreateTaskRequest;
import com.taskforge.exceptions.DuplicateTaskTitleException;
import com.taskforge.exceptions.TaskNotFoundException;
import com.taskforge.models.Task;
import com.taskforge.models.User;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.TaskRepository;
import com.taskforge.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserStoryService userStoryService;
    
    @Autowired
    private UserRepository userRepository;
    
    public Task createTask(CreateTaskRequest request, String username) {
        // Vérifier que l'utilisateur a accès à la user story
        UserStory userStory = userStoryService.getUserStoryById(request.getUserStoryId(), username);
        
        // Vérifier que le titre est unique dans cette US
        if (taskRepository.existsByTitleAndUserStoryId(request.getTitle(), userStory.getId())) {
            throw new DuplicateTaskTitleException("Une tâche avec ce titre existe déjà dans cette user story.");
        }
        
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .userStory(userStory)
                .build();
        
        // Assigner à un utilisateur si spécifié
        if (request.getAssignedToUsername() != null && !request.getAssignedToUsername().isEmpty()) {
            User assignedUser = userRepository.findByUsername(request.getAssignedToUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + request.getAssignedToUsername()));
            
            // Vérifier que l'utilisateur est membre du projet
            if (!userStory.getProject().getMembers().contains(assignedUser)) {
                throw new RuntimeException("L'utilisateur doit être membre du projet pour être assigné à une tâche.");
            }
            
            task.setAssignedTo(assignedUser);
        }
        
        return taskRepository.save(task);
    }
    
    public Task getTaskById(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tâche non trouvée"));
        
        // Vérifier que l'utilisateur a accès au projet
        userStoryService.getUserStoryById(task.getUserStory().getId(), username);
        
        return task;
    }
    
    public List<Task> getTasksByUserStoryId(Long userStoryId, String username) {
        // Vérifier que l'utilisateur a accès à la user story
        userStoryService.getUserStoryById(userStoryId, username);
        
        return taskRepository.findAllByUserStoryId(userStoryId);
    }
    
    public Task updateTask(Long taskId, CreateTaskRequest request, String username) {
        Task task = getTaskById(taskId, username);
        
        // Vérifier l'unicité du titre si changé
        if (!task.getTitle().equals(request.getTitle()) && 
            taskRepository.existsByTitleAndUserStoryId(request.getTitle(), task.getUserStory().getId())) {
            Task existing = taskRepository.findByTitleAndUserStoryId(request.getTitle(), task.getUserStory().getId());
            if (existing != null && !existing.getId().equals(taskId)) {
                throw new DuplicateTaskTitleException("Une tâche avec ce titre existe déjà dans cette user story.");
            }
        }
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        
        // Gérer l'assignation
        if (request.getAssignedToUsername() != null && !request.getAssignedToUsername().isEmpty()) {
            User assignedUser = userRepository.findByUsername(request.getAssignedToUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + request.getAssignedToUsername()));
            
            if (!task.getUserStory().getProject().getMembers().contains(assignedUser)) {
                throw new RuntimeException("L'utilisateur doit être membre du projet pour être assigné à une tâche.");
            }
            
            task.setAssignedTo(assignedUser);
        } else {
            task.setAssignedTo(null);
        }
        
        return taskRepository.save(task);
    }
    
    @Transactional
    public void deleteTask(Long taskId, String username) {
        Task task = getTaskById(taskId, username);
        taskRepository.delete(task);
    }
}