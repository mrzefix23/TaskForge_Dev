package com.taskforge.service;

import java.time.LocalDate;
import java.util.Collections;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskforge.dto.CreateSprintRequest;
import com.taskforge.exceptions.DuplicateSprintNameException;
import com.taskforge.exceptions.InvalidSprintDateException;
import com.taskforge.models.Project;
import com.taskforge.models.Sprint;
import com.taskforge.models.User;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.SprintRepository;
import com.taskforge.repositories.UserStoryRepository;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private SprintService sprintService;

    private User projectOwner;
    private User memberUser;
    private Project project;
    private Sprint sprint;

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

        sprint = Sprint.builder()
                .id(1L)
                .name("Sprint 1")
                .startDate(LocalDate.of(2025, 12, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .status(Sprint.Status.PLANNED)
                .project(project)
                .build();
    }

    @Test
    void createSprint_shouldSucceed() {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1");
        request.setStartDate(LocalDate.of(2025, 12, 1));
        request.setEndDate(LocalDate.of(2025, 12, 15));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(project.getId());

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(sprintRepository.existsByNameAndProjectId(request.getName(), project.getId())).thenReturn(false);
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sprint created = sprintService.createSprint(request, projectOwner.getUsername());

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Sprint 1");
        assertThat(created.getProject()).isEqualTo(project);
        verify(sprintRepository, times(1)).save(any(Sprint.class));
    }

    @Test
    void createSprint_shouldThrowException_whenNameDuplicate() {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1");
        request.setStartDate(LocalDate.of(2025, 12, 1));
        request.setEndDate(LocalDate.of(2025, 12, 15));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(project.getId());

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(sprintRepository.existsByNameAndProjectId(request.getName(), project.getId())).thenReturn(true);

        assertThatThrownBy(() -> sprintService.createSprint(request, projectOwner.getUsername()))
                .isInstanceOf(DuplicateSprintNameException.class);
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void createSprint_shouldThrowException_whenEndDateBeforeStartDate() {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1");
        request.setStartDate(LocalDate.of(2025, 12, 15));
        request.setEndDate(LocalDate.of(2025, 12, 1));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(project.getId());

        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.createSprint(request, projectOwner.getUsername()))
                .isInstanceOf(InvalidSprintDateException.class);
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void createSprint_shouldThrowException_whenUserIsNotOwner() {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1");
        request.setStartDate(LocalDate.of(2025, 12, 1));
        request.setEndDate(LocalDate.of(2025, 12, 15));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(project.getId());

        when(projectService.getProjectById(project.getId(), memberUser.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.createSprint(request, memberUser.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner");
    }

    @Test
    void getSprintById_shouldSucceed() {
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);

        Sprint found = sprintService.getSprintById(sprint.getId(), projectOwner.getUsername());

        assertThat(found).isEqualTo(sprint);
        verify(projectService).getProjectById(project.getId(), projectOwner.getUsername());
    }

    @Test
    void deleteSprint_shouldSucceed_whenUserIsOwner() {
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userStoryRepository.findByProjectId(project.getId())).thenReturn(Collections.emptyList());

        sprintService.deleteSprint(sprint.getId(), projectOwner.getUsername());

        verify(sprintRepository, times(1)).deleteById(sprint.getId());
    }

    @Test
    void deleteSprint_shouldThrowException_whenUserIsNotOwner() {
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), memberUser.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.deleteSprint(sprint.getId(), memberUser.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner");
        verify(sprintRepository, never()).deleteById(anyLong());
    }

    @Test
    void assignUserStoryToSprint_shouldSucceed() {
        UserStory userStory = UserStory.builder()
                .id(1L)
                .title("Test Story")
                .project(project)
                .build();

        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStory updated = sprintService.assignUserStoryToSprint(userStory.getId(), sprint.getId(), projectOwner.getUsername());

        assertThat(updated.getSprint()).isEqualTo(sprint);
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    void removeUserStoryFromSprint_shouldSucceed() {
        UserStory userStory = UserStory.builder()
                .id(1L)
                .title("Test Story")
                .project(project)
                .sprint(sprint)
                .build();

        when(userStoryRepository.findById(userStory.getId())).thenReturn(Optional.of(userStory));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserStory updated = sprintService.removeUserStoryFromSprint(userStory.getId(), projectOwner.getUsername());

        assertThat(updated.getSprint()).isNull();
        verify(userStoryRepository, times(1)).save(userStory);
    }

    @Test
    void startSprint_shouldSucceed() {
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(sprintRepository.findByProjectId(project.getId())).thenReturn(List.of(sprint));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sprint started = sprintService.startSprint(sprint.getId(), projectOwner.getUsername());

        assertThat(started.getStatus()).isEqualTo(Sprint.Status.ACTIVE);
        verify(sprintRepository, times(1)).save(sprint);
    }

    @Test
    void startSprint_shouldThrowException_whenSprintNotPlanned() {
        sprint.setStatus(Sprint.Status.ACTIVE);
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.startSprint(sprint.getId(), projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only PLANNED sprints can be started");
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void startSprint_shouldThrowException_whenActiveSprintExists() {
        Sprint activeSprint = Sprint.builder()
                .id(2L)
                .name("Sprint 2")
                .status(Sprint.Status.ACTIVE)
                .project(project)
                .build();

        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(sprintRepository.findByProjectId(project.getId())).thenReturn(List.of(sprint, activeSprint));

        assertThatThrownBy(() -> sprintService.startSprint(sprint.getId(), projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already an active sprint");
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void startSprint_shouldThrowException_whenUserIsNotOwner() {
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), memberUser.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.startSprint(sprint.getId(), memberUser.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner can start sprints");
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void completeSprint_shouldSucceed() {
        sprint.setStatus(Sprint.Status.ACTIVE);
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sprint completed = sprintService.completeSprint(sprint.getId(), projectOwner.getUsername());

        assertThat(completed.getStatus()).isEqualTo(Sprint.Status.COMPLETED);
        verify(sprintRepository, times(1)).save(sprint);
    }

    @Test
    void completeSprint_shouldThrowException_whenSprintNotActive() {
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), projectOwner.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.completeSprint(sprint.getId(), projectOwner.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only ACTIVE sprints can be completed");
        verify(sprintRepository, never()).save(any(Sprint.class));
    }

    @Test
    void completeSprint_shouldThrowException_whenUserIsNotOwner() {
        sprint.setStatus(Sprint.Status.ACTIVE);
        when(sprintRepository.findById(sprint.getId())).thenReturn(Optional.of(sprint));
        when(projectService.getProjectById(project.getId(), memberUser.getUsername())).thenReturn(project);

        assertThatThrownBy(() -> sprintService.completeSprint(sprint.getId(), memberUser.getUsername()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner can complete sprints");
        verify(sprintRepository, never()).save(any(Sprint.class));
    }
}
