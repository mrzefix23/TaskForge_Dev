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
import com.taskforge.repositories.KanbanColumnRepository;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.TaskRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;

import jakarta.transaction.Transactional;

/**
 * Service gérant la logique métier liée aux User Stories.
 * Permet de créer, récupérer, mettre à jour et supprimer des User Stories,
 * ainsi que de gérer leur assignation aux membres du projet.
 */
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

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private KanbanColumnRepository kanbanColumnRepository;
    
    /**
     * Crée une nouvelle User Story dans un projet.
     * Vérifie que le titre est unique dans le projet et que les utilisateurs assignés sont bien membres du projet.
     *
     * @param request  Les détails de la User Story à créer.
     * @param username Le nom d'utilisateur de la personne effectuant la création.
     * @return La User Story crée et sauvegardée.
     * @throws DuplicateUserStoryTitleException Si une User Story avec le même titre existe déjà dans le projet.
     * @throws RuntimeException                 Si un utilisateur assigné n'est pas trouvé ou n'est pas membre du projet.
     */
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
    
    /**
     * Récupère la liste des User Stories d'un projet.
     * Vérifie au préalable que l'utilisateur a accès au projet.
     *
     * @param projectId L'identifiant du projet.
     * @param username  Le nom d'utilisateur effectuant la requête.
     * @return Une liste de User Stories.
     */
    public List<UserStory> getUserStoriesByProject(Long projectId, String username) {
        // Verify user has access to project
        projectService.getProjectById(projectId, username);
        return userStoryRepository.findByProjectId(projectId);
    }
    
    /**
     * Récupère une User Story par son identifiant.
     * Vérifie que l'utilisateur a accès au projet contenant la User Story.
     *
     * @param userStoryId L'identifiant de la User Story.
     * @param username    Le nom d'utilisateur effectuant la requête.
     * @return La User Story trouvée.
     * @throws RuntimeException Si la User Story n'existe pas ou si l'accès est refusé.
     */
    public UserStory getUserStoryById(Long userStoryId, String username) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User story not found"));
        
        // Verify user has access to project
        projectService.getProjectById(userStory.getProject().getId(), username);
        
        return userStory;
    }
    
    /**
     * Met à jour une User Story existante.
     * Vérifie l'unicité du titre si modifié et met à jour les assignations.
     *
     * @param userStoryId L'identifiant de la User Story à mettre à jour.
     * @param request     Les nouvelles informations de la User Story.
     * @param username    Le nom d'utilisateur effectuant la mise à jour.
     * @return La User Story mise à jour.
     * @throws DuplicateUserStoryTitleException Si le nouveau titre est déjà utilisé dans le projet.
     * @throws RuntimeException                 Si un utilisateur assigné n'est pas valide.
     */
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
    
    /**
     * Supprime une User Story et toutes ses tâches associées.
     * Seul le propriétaire du projet est autorisé à effectuer cette action.
     *
     * @param userStoryId L'identifiant de la User Story à supprimer.
     * @param username    Le nom d'utilisateur effectuant la suppression.
     * @throws RuntimeException Si l'utilisateur n'est pas le propriétaire du projet.
     */
    @Transactional
    public void deleteUserStory(Long userStoryId, String username) {
        UserStory userStory = getUserStoryById(userStoryId, username);
        
        // Only project owner can delete user stories
        if (!userStory.getProject().getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Only project owner can delete user stories");
        }

        // Supprimer toutes les tâches associées
        taskRepository.deleteAllByUserStoryId(userStoryId);
        
        userStoryRepository.deleteById(userStoryId);
    }
    
    /**
     * Met à jour uniquement le statut d'une User Story (drag & drop).
     * Permet de déplacer une User Story entre les colonnes Kanban.
     *
     * @param userStoryId L'identifiant de la User Story.
     * @param status      Le nouveau statut.
     * @param username    Le nom d'utilisateur effectuant la mise à jour.
     * @return La User Story avec le statut mis à jour.
     * @throws RuntimeException Si la User Story n'existe pas ou si l'accès est refusé.
     */
    @Transactional
    public UserStory updateUserStoryStatus(Long userStoryId, String status, String username) {
        UserStory userStory = getUserStoryById(userStoryId, username);
        
        // Mettre à jour le statut
        userStory.setStatus(status);
        
        // Mettre à jour la colonne Kanban si elle existe
        kanbanColumnRepository.findByStatusAndProjectId(status, userStory.getProject().getId())
                .ifPresent(userStory::setKanbanColumn);
        
        return userStoryRepository.save(userStory);
    }
}