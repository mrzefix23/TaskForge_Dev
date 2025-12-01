package com.taskforge.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.exceptions.DuplicateProjectNameException;
import com.taskforge.exceptions.ProjectSuppressionException;
import com.taskforge.exceptions.UpdateProjectException;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;
import com.taskforge.repositories.SprintRepository;

/**
 * Tests unitaires pour le service de gestion des projets (ProjectService).
 * Vérifie la logique métier de création, récupération, mise à jour et suppression des projets,
 * en isolant les dépendances (Repositories) via Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private SprintRepository sprintRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private CreateProjectRequest createRequest;

    /**
     * Initialise les données de test avant chaque exécution.
     * Crée un utilisateur fictif et une requête de création de projet par défaut.
     */
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        UserDto userDto = UserDto.builder().username("testuser").build();
        createRequest = new CreateProjectRequest();
        createRequest.setName("Test Project");
        createRequest.setDescription("Test Description");
        createRequest.setUser(userDto);
        createRequest.setMembers(new ArrayList<>());
    }

    /**
     * Vérifie que la création d'un projet réussit avec des données valides.
     * Le service doit sauvegarder le projet et l'associer au propriétaire.
     */
    @Test
    void createProject_shouldCreateProject_WhenValidRequest() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Project savedProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .members(Set.of(testUser))
                .build();
        
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        Project result = projectService.createProject(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Project");
        assertThat(result.getOwner().getUsername()).isEqualTo("testuser");
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    /**
     * Vérifie que la création échoue si l'utilisateur créateur n'existe pas.
     * Doit lever une RuntimeException.
     */
    @Test
    void createProject_shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    /**
     * Vérifie que les membres spécifiés sont correctement ajoutés au projet lors de la création.
     */
    @Test
    void createProject_shouldAddMembers_WhenProvided() {
        UserDto memberDto = UserDto.builder().username("member").build();
        createRequest.setMembers(List.of(memberDto));
        User memberUser = User.builder().id(2L).username("member").build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(memberUser));
        
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.createProject(createRequest);

        assertThat(result.getMembers()).contains(testUser, memberUser);
    }

    /**
     * Vérifie que la création échoue si l'un des membres spécifiés n'existe pas.
     * Doit lever une RuntimeException.
     */
    @Test
    void createProject_shouldThrowException_WhenMemberNotFound() {
        UserDto memberDto = UserDto.builder().username("unknown").build();
        createRequest.setMembers(List.of(memberDto));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: unknown");
    }

    /**
     * Vérifie qu'un membre du projet peut récupérer les détails du projet par son ID.
     */
    @Test
    void getProjectById_shouldReturnProject_WhenUserIsMember() {
        Project project = Project.builder()
                .id(1L)
                .name("Test Project")
                .owner(testUser)
                .members(Set.of(testUser))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(1L, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    /**
     * Vérifie que la récupération échoue si le projet n'existe pas.
     * Doit lever une RuntimeException.
     */
    @Test
    void getProjectById_shouldThrowException_WhenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L, "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");
    }

    /**
     * Vérifie que la récupération échoue si l'utilisateur n'est pas membre du projet.
     * Doit lever une RuntimeException (accès refusé implicite).
     */
    @Test
    void getProjectById_shouldThrowException_WhenUserNotMember() {
        User otherUser = User.builder().username("otheruser").build();
        Project project = Project.builder()
                .id(1L)
                .name("Test Project")
                .owner(otherUser)
                .members(Set.of(otherUser))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.getProjectById(1L, "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not a member");
    }


    /**
     * Vérifie que le propriétaire peut mettre à jour les informations de son projet.
     */
    @Test
    void updateProject_shouldSucceed_WhenOwnerUpdatesValidData() {
        Project existingProject = Project.builder()
                .id(1L)
                .name("Old Name")
                .owner(testUser)
                .members(new HashSet<>(Set.of(testUser)))
                .build();

        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("New Name");
        updateRequest.setDescription("New Desc");
        updateRequest.setMembers(new ArrayList<>());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project updated = projectService.updateProject(1L, "testuser", updateRequest);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Desc");
        verify(projectRepository).save(existingProject);
    }

    /**
     * Vérifie que la mise à jour échoue si l'utilisateur n'est pas le propriétaire.
     * Doit lever une UpdateProjectException.
     */
    @Test
    void updateProject_shouldThrowException_WhenNotOwner() {
        User otherUser = User.builder().username("other").build();
        Project existingProject = Project.builder()
                .id(1L)
                .owner(otherUser)
                .members(Set.of(otherUser, testUser))
                .build();

        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Any Name");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.findByName("Any Name")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(1L, "testuser", updateRequest))
                .isInstanceOf(UpdateProjectException.class)
                .hasMessageContaining("Seul le propriétaire");
    }

    /**
     * Vérifie que la mise à jour échoue si le nouveau nom est déjà pris par un autre projet.
     * Doit lever une DuplicateProjectNameException.
     */
    @Test
    void updateProject_shouldThrowException_WhenDuplicateNameExists() {
        Project existingProject = Project.builder().id(1L).name("Old").owner(testUser).members(Set.of(testUser)).build();
        Project duplicateProject = Project.builder().id(2L).name("New Name").build(); // ID Différent

        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("New Name");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.findByName("New Name")).thenReturn(Optional.of(duplicateProject));

        assertThatThrownBy(() -> projectService.updateProject(1L, "testuser", updateRequest))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("Un projet avec ce nom existe déjà");
    }

    /**
     * Vérifie que la mise à jour réussit si le nom reste inchangé (pas de conflit avec soi-même).
     */
    @Test
    void updateProject_shouldSucceed_WhenNameIsSameAsCurrentProject() {
        Project existingProject = Project.builder().id(1L).name("Same Name").owner(testUser).members(new HashSet<>(Set.of(testUser))).build();
        
        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Same Name");
        updateRequest.setDescription("Updated Desc");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.findByName("Same Name")).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.updateProject(1L, "testuser", updateRequest);
        assertThat(result.getDescription()).isEqualTo("Updated Desc");
    }

    /**
     * Vérifie que de nouveaux membres peuvent être ajoutés lors de la mise à jour du projet.
     */
    @Test
    void updateProject_shouldAddNewMembers() {
        Project existingProject = Project.builder()
                .id(1L)
                .name("Project")
                .owner(testUser)
                .members(new HashSet<>(List.of(testUser)))
                .build();

        UserDto newMemberDto = UserDto.builder().username("newMember").build();
        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Project");
        updateRequest.setMembers(List.of(newMemberDto));

        User newMember = User.builder().id(2L).username("newMember").build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.findByName("Project")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newMember")).thenReturn(Optional.of(newMember));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        Project result = projectService.updateProject(1L, "testuser", updateRequest);

        assertThat(result.getMembers()).contains(testUser, newMember);
    }
    
    /**
     * Vérifie que la mise à jour échoue si l'un des nouveaux membres n'existe pas.
     */
    @Test
    void updateProject_shouldThrowException_WhenNewMemberNotFound() {
         Project existingProject = Project.builder()
                .id(1L)
                .name("Project")
                .owner(testUser)
                .members(new HashSet<>(List.of(testUser)))
                .build();

        UserDto unknownDto = UserDto.builder().username("unknown").build();
        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Project");
        updateRequest.setMembers(List.of(unknownDto));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(1L, "testuser", updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    /**
     * Vérifie que le propriétaire peut supprimer son projet.
     * Le service doit également supprimer les dépendances (User Stories, Tâches).
     */
    @Test
    void deleteProject_shouldDeleteProject_WhenUserIsOwner() {
        Project project = Project.builder()
                .id(1L)
                .name("Test Project")
                .owner(testUser)
                .members(Set.of(testUser))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        doNothing().when(userStoryRepository).deleteAllByProjectId(1L);
        doNothing().when(projectRepository).deleteById(1L);

        projectService.deleteProject(1L, "testuser");

        verify(userStoryRepository, times(1)).deleteAllByProjectId(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    /**
     * Vérifie que la suppression échoue si l'utilisateur n'est pas le propriétaire.
     * Doit lever une ProjectSuppressionException.
     */
    @Test
    void deleteProject_shouldThrowException_WhenUserNotOwner() {
        User otherUser = User.builder().username("other").build();
        Project project = Project.builder()
                .id(1L)
                .owner(otherUser)
                .members(Set.of(otherUser, testUser))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(1L, "testuser"))
                .isInstanceOf(ProjectSuppressionException.class)
                .hasMessageContaining("Uniquement le propriétaire");
        
        verify(projectRepository, never()).deleteById(anyLong());
    }

    /**
     * Vérifie que la méthode getProjectsByUsername retourne bien la liste des projets
     * où l'utilisateur est impliqué (propriétaire ou membre).
     */
    @Test
    void getProjectsByUsername_shouldReturnUserProjects() {
        Project project1 = Project.builder().id(1L).name("Project 1").owner(testUser).build();
        Project project2 = Project.builder().id(2L).name("Project 2").owner(testUser).build();
        
        when(projectRepository.findAllByOwnerOrMember("testuser"))
                .thenReturn(Arrays.asList(project1, project2));

        List<Project> projects = projectService.getProjectsByUsername("testuser");

        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Project 1", "Project 2");
    }
}