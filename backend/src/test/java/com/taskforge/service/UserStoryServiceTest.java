package com.taskforge.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.exceptions.DuplicateUserStoryTitleException;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.TaskRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;

/**
 * Tests unitaires pour le service de gestion des User Stories (UserStoryService).
 * Vérifie la logique métier de création, récupération, mise à jour et suppression des User Stories,
 * en isolant les dépendances (Repositories, ProjectService) via Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UserStoryService userStoryService;

    private User projectOwner;
    private User memberUser;
    private Project project;
    private UserStory userStory;

    /**
     * Initialise les données de test avant chaque exécution.
     * Crée un projet, un propriétaire, un membre et une User Story par défaut.
     */
    @BeforeEach
    void setUp() {
        projectOwner = User.builder().id(1L).username("owner").build();
        memberUser = User.builder().id(2L).username("member").build();
        
        Set<User> members = new HashSet<>();
        members.add(projectOwner);
        members.add(memberUser);

        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .owner(projectOwner)
                .members(members)
                .build();
        
        userStory = UserStory.builder()
                .id(1L)
                .title("Test Story")
                .project(project)
                .assignedTo(new HashSet<>())
                .build();
    }

    /**
     * Vérifie que la création d'une User Story réussit avec des données valides.
     * Le service doit vérifier l'accès au projet, l'unicité du titre et sauvegarder l'entité.
     */
    @Test
    void createUserStory_shouldSucceed() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setProjectId(project.getId());
        request.setTitle("New US");
        request.setDescription("A description");
        request.setPriority(UserStory.Priority.HIGH);
        request.setStatus("TODO");

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userStoryRepository.existsByTitleAndProjectId(request.getTitle(), project.getId())).thenReturn(false);
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStory created = userStoryService.createUserStory(request, projectOwner.getUsername());

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("New US");
        assertThat(created.getProject()).isEqualTo(project);
        verify(userStoryRepository, times(1)).save(any(UserStory.class));
    }

    /**
     * Vérifie que la création échoue si une User Story avec le même titre existe déjà dans le projet.
     * Doit lever une DuplicateUserStoryTitleException.
     */
    @Test
    void createUserStory_shouldThrowException_whenTitleExists() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setProjectId(project.getId());
        request.setTitle("Existing Title");

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userStoryRepository.existsByTitleAndProjectId(request.getTitle(), project.getId())).thenReturn(true);

        assertThatThrownBy(() -> userStoryService.createUserStory(request, projectOwner.getUsername()))
                .isInstanceOf(DuplicateUserStoryTitleException.class)
                .hasMessageContaining("Une user story avec ce titre existe déjà");
        
        verify(userStoryRepository, never()).save(any());
    }

    /**
     * Vérifie que la création réussit avec des utilisateurs assignés valides (membres du projet).
     */
    @Test
    void createUserStory_shouldSucceed_withAssignees() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setProjectId(project.getId());
        request.setTitle("US with Assignee");
        request.setAssignedToUsernames(Collections.singletonList("member"));

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(memberUser));
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> {
            UserStory saved = invocation.getArgument(0);
            assertThat(saved.getAssignedTo()).contains(memberUser);
            return saved;
        });

        userStoryService.createUserStory(request, projectOwner.getUsername());
    }

    /**
     * Vérifie que la création échoue si l'un des utilisateurs assignés n'est pas membre du projet.
     * Doit lever une RuntimeException.
     */
    @Test
    void createUserStory_shouldThrow_whenAssigneeNotMember() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setProjectId(project.getId());
        request.setTitle("US Fail");
        request.setAssignedToUsernames(Collections.singletonList("outsider"));

        User outsider = User.builder().id(3L).username("outsider").build();

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userRepository.findByUsername("outsider")).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> userStoryService.createUserStory(request, projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("is not a member of this project");
    }

    /**
     * Vérifie que la récupération des User Stories d'un projet retourne la liste attendue.
     */
    @Test
    void getUserStoriesByProject_shouldReturnList() {
        when(userStoryRepository.findByProjectId(project.getId())).thenReturn(Collections.singletonList(userStory));
        
        List<UserStory> results = userStoryService.getUserStoriesByProject(project.getId(), projectOwner.getUsername());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(userStory);
        verify(projectService).getProjectById(project.getId(), projectOwner.getUsername());
    }

    /**
     * Vérifie qu'une User Story spécifique peut être récupérée par son ID.
     */
    @Test
    void getUserStoryById_shouldSucceed() {
        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        
        UserStory found = userStoryService.getUserStoryById(userStory.getId(), projectOwner.getUsername());
        
        assertThat(found).isEqualTo(userStory);
        verify(projectService).getProjectById(project.getId(), projectOwner.getUsername());
    }

    /**
     * Vérifie que la récupération échoue si la User Story n'existe pas.
     * Doit lever une RuntimeException.
     */
    @Test
    void getUserStoryById_shouldThrowException_whenUserStoryNotFound() {
        when(userStoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStoryService.getUserStoryById(999L, projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User story not found");
    }

    /**
     * Vérifie que la mise à jour d'une User Story réussit avec des données valides.
     */
    @Test
    void updateUserStory_shouldSucceed() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setTitle("Updated Title");
        request.setAssignedToUsernames(Collections.singletonList("member"));

        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.existsByTitleAndProjectId(request.getTitle(), project.getId())).thenReturn(false);
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(memberUser));
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStory updated = userStoryService.updateUserStory(userStory.getId(), request, projectOwner.getUsername());

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getAssignedTo()).contains(memberUser);
    }

    /**
     * Vérifie que la mise à jour échoue si la User Story n'existe pas.
     */
    @Test
    void updateUserStory_shouldThrowException_whenUserStoryNotFound() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        when(userStoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userStoryService.updateUserStory(999L, request, projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User story not found");
    }

    /**
     * Vérifie que la mise à jour échoue si le nouveau titre est déjà utilisé par une autre User Story du même projet.
     */
    @Test
    void updateUserStory_shouldThrow_whenTitleDuplicateOnOtherStory() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setTitle("Other Story Title");

        UserStory otherStory = UserStory.builder().id(2L).title("Other Story Title").build();

        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.existsByTitleAndProjectId(request.getTitle(), project.getId())).thenReturn(true);
        when(userStoryRepository.findByTitleAndProjectId(request.getTitle(), project.getId())).thenReturn(otherStory);

        assertThatThrownBy(() -> userStoryService.updateUserStory(userStory.getId(), request, projectOwner.getUsername()))
                .isInstanceOf(DuplicateUserStoryTitleException.class);
    }
    
    /**
     * Vérifie que la mise à jour réussit si le titre reste inchangé (pas de conflit avec soi-même).
     */
    @Test
    void updateUserStory_shouldSucceed_whenTitleSameAsCurrent() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setTitle("Test Story"); 
        request.setDescription("New Desc");

        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.existsByTitleAndProjectId("Test Story", project.getId())).thenReturn(true);
        when(userStoryRepository.findByTitleAndProjectId("Test Story", project.getId())).thenReturn(userStory);
        when(userStoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserStory updated = userStoryService.updateUserStory(userStory.getId(), request, projectOwner.getUsername());
        
        assertThat(updated.getDescription()).isEqualTo("New Desc");
    }

    /**
     * Vérifie que le propriétaire du projet peut supprimer une User Story.
     * Le service doit également supprimer les tâches associées.
     */
    @Test
    void deleteUserStory_shouldSucceed_whenUserIsOwner() {
        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        userStoryService.deleteUserStory(userStory.getId(), projectOwner.getUsername());

        verify(taskRepository, times(1)).deleteAllByUserStoryId(userStory.getId());
        verify(userStoryRepository, times(1)).deleteById(userStory.getId());
    }

    /**
     * Vérifie que la suppression échoue si l'utilisateur n'est pas le propriétaire du projet.
     */
    @Test
    void deleteUserStory_shouldThrowException_whenUserIsNotOwner() {
        String notOwnerUsername = "notOwner";
        
        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userStoryService.deleteUserStory(userStory.getId(), notOwnerUsername);
        });

        assertThat(exception.getMessage()).isEqualTo("Only project owner can delete user stories");
        verify(userStoryRepository, never()).deleteById(anyLong());
    }

    /**
     * Vérifie que la suppression échoue si la User Story n'existe pas.
     */
    @Test
    void deleteUserStory_shouldThrowException_whenUserStoryNotFound() {
        when(userStoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStoryService.deleteUserStory(999L, projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User story not found");
    }
}