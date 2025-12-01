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

/**
 * Service gérant la logique métier liée aux tâches techniques.
 * Permet de créer, lire, mettre à jour et supprimer des tâches,
 * ainsi que de gérer leur assignation aux utilisateurs.
 */
@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserStoryService userStoryService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Crée une nouvelle tâche associée à une User Story.
     * Vérifie que l'utilisateur a accès au projet, que le titre est unique dans l'US,
     * et que l'utilisateur assigné (si présent) est membre du projet.
     *
     * @param request  Les détails de la tâche à créer.
     * @param username Le nom d'utilisateur de la personne effectuant la création.
     * @return La tâche créée et sauvegardée.
     * @throws DuplicateTaskTitleException Si une tâche avec le même titre existe déjà dans l'US.
     * @throws RuntimeException            Si l'utilisateur assigné n'est pas trouvé ou n'est pas membre.
     */
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
    
    /**
     * Récupère une tâche par son identifiant.
     * Vérifie que l'utilisateur demandeur a accès au projet contenant la tâche.
     *
     * @param taskId   L'identifiant de la tâche.
     * @param username Le nom d'utilisateur effectuant la requête.
     * @return La tâche trouvée.
     * @throws TaskNotFoundException Si la tâche n'existe pas.
     */
    public Task getTaskById(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Tâche non trouvée"));
        
        // Vérifier que l'utilisateur a accès au projet
        userStoryService.getUserStoryById(task.getUserStory().getId(), username);
        
        return task;
    }
    
    /**
     * Récupère toutes les tâches associées à une User Story spécifique.
     *
     * @param userStoryId L'identifiant de la User Story.
     * @param username    Le nom d'utilisateur effectuant la requête.
     * @return Une liste de tâches.
     */
    public List<Task> getTasksByUserStoryId(Long userStoryId, String username) {
        // Vérifier que l'utilisateur a accès à la user story
        userStoryService.getUserStoryById(userStoryId, username);
        
        return taskRepository.findAllByUserStoryId(userStoryId);
    }
    
    /**
     * Met à jour une tâche existante.
     * Vérifie l'unicité du titre si modifié et met à jour l'assignation si nécessaire.
     *
     * @param taskId   L'identifiant de la tâche à mettre à jour.
     * @param request  Les nouvelles informations de la tâche.
     * @param username Le nom d'utilisateur effectuant la mise à jour.
     * @return La tâche mise à jour.
     * @throws DuplicateTaskTitleException Si le nouveau titre est déjà utilisé dans l'US.
     * @throws RuntimeException            Si l'utilisateur assigné n'est pas valide.
     */
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
    
    /**
     * Supprime une tâche.
     *
     * @param taskId   L'identifiant de la tâche à supprimer.
     * @param username Le nom d'utilisateur effectuant la suppression.
     */
    @Transactional
    public void deleteTask(Long taskId, String username) {
        Task task = getTaskById(taskId, username);
        taskRepository.delete(task);
    }
}