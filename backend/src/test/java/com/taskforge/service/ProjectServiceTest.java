package com.taskforge.service;

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private CreateProjectRequest createRequest;

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
        createRequest.setMembers(List.of());
    }

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

    @Test
    void createProject_shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

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

    @Test
    void getProjectById_shouldThrowException_WhenUserNotMember() {
        User otherUser = User.builder()
                .username("otheruser")
                .build();

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