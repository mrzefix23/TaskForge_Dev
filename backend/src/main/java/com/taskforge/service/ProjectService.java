package com.taskforge.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.exceptions.DuplicateProjectNameException;
import com.taskforge.exceptions.ProjectSuppressionException;
import com.taskforge.exceptions.UpdateProjectException;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.SprintRepository;
import com.taskforge.repositories.TaskRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;

import jakarta.transaction.Transactional;

/**
 * Service gérant la logique métier liée aux projets.
 * Permet de créer, récupérer, mettre à jour et supprimer des projets,
 * ainsi que de gérer les membres associés.
 */
@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SprintRepository sprintRepository;
  
    /**
     * Crée un nouveau projet.
     * Associe le créateur en tant que propriétaire et membre, et ajoute les autres membres spécifiés.
     *
     * @param createProjectRequest Les informations du projet à créer.
     * @return Le projet créé et sauvegardé.
     * @throws RuntimeException Si l'utilisateur propriétaire ou un membre n'est pas trouvé.
     */
    public Project createProject(CreateProjectRequest createProjectRequest) {
        User owner = userRepository.findByUsername(createProjectRequest.getUser().getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create the project
        Project project = Project.builder()
                .name(createProjectRequest.getName())
                .description(createProjectRequest.getDescription())
                .owner(owner)
                .build();

        Set<User> members = new HashSet<>();
        // Add members to the project
        if(createProjectRequest.getMembers() != null) {
            for (var memberDto : createProjectRequest.getMembers()) {
                User member = userRepository.findByUsername(memberDto.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found: " + memberDto.getUsername()));
                members.add(member);
            }
        }
        // Add owner as a member
        members.add(owner);

        // Set members to project
        project.setMembers(members);
        
        return projectRepository.save(project);
    }

    /**
     * Récupère un projet par son identifiant, en vérifiant que l'utilisateur demandeur est membre.
     *
     * @param projectId L'identifiant du projet.
     * @param username  Le nom d'utilisateur de la personne effectuant la requête.
     * @return Le projet trouvé.
     * @throws RuntimeException Si le projet n'existe pas ou si l'utilisateur n'est pas membre.
     */
    public Project getProjectById(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUsername().equals(username));

        if (!isMember) {
            throw new RuntimeException("User is not a member of this project");
        }

        return project;
    }

    /**
     * Met à jour les informations d'un projet existant.
     * Seul le propriétaire du projet est autorisé à effectuer cette action.
     *
     * @param projectId     L'identifiant du projet à mettre à jour.
     * @param username      Le nom d'utilisateur effectuant la mise à jour.
     * @param updateRequest Les nouvelles données du projet.
     * @return Le projet mis à jour.
     * @throws DuplicateProjectNameException Si le nouveau nom est déjà pris par un autre projet.
     * @throws UpdateProjectException        Si l'utilisateur n'est pas le propriétaire.
     * @throws RuntimeException              Si un membre spécifié n'existe pas.
     */
    public Project updateProject(Long projectId, String username, CreateProjectRequest updateRequest) {
        Project project = getProjectById(projectId, username);

        // Vérifier si le projet existe déjà
        projectRepository.findByName(updateRequest.getName()).filter(existingProject -> !existingProject.getId().equals(projectId))
            .ifPresent(existingProject -> {
                throw new DuplicateProjectNameException("Un projet avec ce nom existe déjà.");
            });

        // Vérifier si l'utilisateur est le propriétaire du projet
        boolean isOwner = project.getOwner().getUsername().equals(username);
        
        if (!isOwner) {
            throw new UpdateProjectException("Seul le propriétaire du projet peut le mettre à jour.");
        }
        Set<User> members = new HashSet<>();
        if(updateRequest.getMembers() != null) {
            for (var memberDto : updateRequest.getMembers()) {
                User member = userRepository.findByUsername(memberDto.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found: " + memberDto.getUsername()));
                members.add(member);
            }
        }
        // Ensure owner is still a member
        members.add(project.getOwner());
        
        project.setName(updateRequest.getName());
        project.setDescription(updateRequest.getDescription());
        project.setMembers(members);
        
        return projectRepository.save(project);
    }

    /**
     * Supprime un projet et toutes ses dépendances (User Stories, Tâches).
     * Seul le propriétaire du projet peut effectuer cette suppression.
     *
     * @param projectId L'identifiant du projet à supprimer.
     * @param username  Le nom d'utilisateur effectuant la suppression.
     * @throws ProjectSuppressionException Si l'utilisateur n'est pas le propriétaire.
     */
    @Transactional
    public void deleteProject(Long projectId, String username) {
        Project project = getProjectById(projectId, username);
        if (!project.getOwner().getUsername().equals(username)) {
            throw new ProjectSuppressionException("Uniquement le propriétaire du projet peut le supprimer.");
        }

        // Récupérer toutes les US du projet
        List<UserStory> userStories = userStoryRepository.findByProjectId(projectId);
    
        // Supprimer toutes les tâches de chaque US
        for (UserStory us : userStories) {
            taskRepository.deleteAllByUserStoryId(us.getId());
        }
        //Supprimer les US lié au projet
        userStoryRepository.deleteAllByProjectId(projectId);
        
        // Supprimer tous les sprints du projet
        sprintRepository.deleteAllByProjectId(projectId);

        projectRepository.deleteById(projectId);
    }   

    /**
     * Récupère la liste de tous les projets dont l'utilisateur est propriétaire ou membre.
     *
     * @param username Le nom d'utilisateur.
     * @return Une liste de projets associés à cet utilisateur.
     */
    public List<Project> getProjectsByUsername(String username) {
        return projectRepository.findAllByOwnerOrMember(username);
    }
}
