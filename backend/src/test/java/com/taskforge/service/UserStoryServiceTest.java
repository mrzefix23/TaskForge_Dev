package com.taskforge.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;

@ExtendWith(MockitoExtension.class)
class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserStoryService userStoryService;

    private User projectOwner;
    private Project project;
    private UserStory userStory;

    @BeforeEach
    void setUp() {
        projectOwner = User.builder().id(1L).username("owner").build();
        project = Project.builder().id(1L).name("Test Project").owner(projectOwner).build();
        userStory = UserStory.builder()
                .id(1L)
                .title("Test Story")
                .project(project)
                .build();
    }

    @Test
    void createUserStory_shouldSucceed() {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setProjectId(project.getId());
        request.setTitle("New US");
        request.setDescription("A description");
        request.setPriority(UserStory.Priority.HIGH);
        request.setStatus(UserStory.Status.TODO);

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStory created = userStoryService.createUserStory(request, projectOwner.getUsername());

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("New US");
        assertThat(created.getProject()).isEqualTo(project);
        verify(userStoryRepository, times(1)).save(any(UserStory.class));
    }

    @Test
    void deleteUserStory_shouldSucceed_whenUserIsOwner() {
        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);

        userStoryService.deleteUserStory(userStory.getId(), projectOwner.getUsername());

        verify(userStoryRepository, times(1)).deleteById(userStory.getId());
    }

    @Test
    void deleteUserStory_shouldThrowException_whenUserIsNotOwner() {
        String notOwnerUsername = "notOwner";
        User notOwner = User.builder().id(2L).username(notOwnerUsername).build();
        
        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(projectService.getProjectById(project.getId(), notOwnerUsername)).thenReturn(project);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userStoryService.deleteUserStory(userStory.getId(), notOwnerUsername);
        });

        assertThat(exception.getMessage()).isEqualTo("Only project owner can delete user stories");
        verify(userStoryRepository, never()).deleteById(anyLong());
    }
}
