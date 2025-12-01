package com.taskforge.service;

import com.taskforge.dto.CreateVersionRequest;
import com.taskforge.exceptions.DuplicateProjectNameException;
import com.taskforge.models.Project;
import com.taskforge.models.Version;
import com.taskforge.models.UserStory;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserStoryRepository;
import com.taskforge.repositories.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe VersionService.
 * Utilise Mockito pour simuler les dépendances.
 */
@ExtendWith(MockitoExtension.class)
public class VersionServiceTest {

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserStoryRepository userStoryRepository;

    @InjectMocks
    private VersionService versionService;

    private Project testProject;
    private Version testVersion;
    private UserStory testUserStory;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .name("Projet Test")
                .build();

        testVersion = Version.builder()
                .id(1L)
                .title("Version 1.0")
                .versionNumber("1.0")
                .project(testProject)
                .build();
        
        testUserStory = UserStory.builder()
                .id(1L)
                .title("User Story Test")
                .version(testVersion)
                .project(testProject)
                .build();
    }

    @Test
    void getVersionById_shouldReturnVersion_whenExists() {
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        Version found = versionService.getVersionById(1L);
        assertThat(found).isEqualTo(testVersion);
    }

    @Test
    void getVersionById_shouldThrowException_whenNotExists() {
        when(versionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> versionService.getVersionById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Version non trouvée avec l'ID: 1");
    }

    @Test
    void createVersion_shouldCreateVersion_whenDataIsValid() {
        CreateVersionRequest request = CreateVersionRequest.builder()
                .projectId(testProject.getId())
                .title("Version 1.0")
                .versionNumber("1.0")
                .description("Description de la version")
                .build(); 
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(versionRepository.existsByProjectIdAndVersionNumber(testProject.getId(), "1.0")).thenReturn(false);
        when(versionRepository.findByProjectIdOrderByIdDesc(testProject.getId())).thenReturn(new ArrayList<>());
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        Version created = versionService.createVersion(request);
        assertThat(created).isEqualTo(testVersion);
    }

    @Test
    void createVersion_shouldThrowException_whenVersionNumberExists() {
        CreateVersionRequest request = CreateVersionRequest.builder()
                .projectId(testProject.getId())
                .title("Version 1.0")
                .versionNumber("1.0")
                .description("Description de la version")
                .build();
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(versionRepository.existsByProjectIdAndVersionNumber(testProject.getId(), "1.0")).thenReturn(true);
        assertThatThrownBy(() -> versionService.createVersion(request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("Une version avec ce numéro existe déjà pour ce projet");
    }

    @Test
    void createVersion_shouldThrowException_whenTitleExists() {
        CreateVersionRequest request = CreateVersionRequest.builder()
                .projectId(testProject.getId())
                .title("Version 1.0")
                .versionNumber("1.0")
                .description("Description de la version")
                .build();
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(versionRepository.existsByProjectIdAndVersionNumber(testProject.getId(), "1.0")).thenReturn(false);
        List<Version> existingVersions = new ArrayList<>();
        existingVersions.add(Version.builder()
                .id(2L)
                .title("Version 1.0")
                .versionNumber("0.9")
                .project(testProject)
                .build());
        when(versionRepository.findByProjectIdOrderByIdDesc(testProject.getId())).thenReturn(existingVersions);
        assertThatThrownBy(() -> versionService.createVersion(request)) 
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("Une version avec ce titre existe déjà pour ce projet");
    }

    @Test
    void updateVersion_shouldUpdateVersion_whenDataIsValid() {
        CreateVersionRequest request = CreateVersionRequest.builder()
                .title("Version 1.1")
                .versionNumber("1.1")
                .description("Mise à jour de la version")
                .build();
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        when(versionRepository.existsByProjectIdAndVersionNumber(testProject.getId(), "1.1")).thenReturn(false);
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        Version updated = versionService.updateVersion(1L, request);
        assertThat(updated.getTitle()).isEqualTo("Version 1.1");
        assertThat(updated.getVersionNumber()).isEqualTo("1.1");
        assertThat(updated.getDescription()).isEqualTo("Mise à jour de la version");
    }

    @Test
    void updateVersion_shouldThrowException_whenVersionNumberExists() {
        CreateVersionRequest request = CreateVersionRequest.builder()
                .title("Version 1.1")
                .versionNumber("1.1")
                .description("Mise à jour de la version")
                .build();
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        when(versionRepository.existsByProjectIdAndVersionNumber(testProject.getId(), "1.1")).thenReturn(true);
        assertThatThrownBy(() -> versionService.updateVersion(1L, request))
                .isInstanceOf(DuplicateProjectNameException.class)
                .hasMessageContaining("Une version avec ce numéro existe déjà pour ce projet");
    }

    @Test
    void updateVersionStatus_shouldUpdateStatusAndSetReleaseDate_whenStatusIsReleased() {
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        when(versionRepository.save(any(Version.class))).thenReturn(testVersion);
        Version updated = versionService.updateVersionStatus(1L, Version.VersionStatus.RELEASED);
        assertThat(updated.getStatus()).isEqualTo(Version.VersionStatus.RELEASED);
        assertThat(updated.getReleaseDate()).isNotNull();
    }

    @Test
    void assignUserStoryToVersion_shouldAssignUserStory() {
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(testUserStory);
        UserStory updated = versionService.assignUserStoryToVersion(1L, 1L);
        assertThat(updated.getVersion()).isEqualTo(testVersion);
    }

    @Test
    void removeUserStoryFromVersion_shouldRemoveUserStoryVersion() {
        when(userStoryRepository.findById(1L)).thenReturn(Optional.of(testUserStory));
        when(userStoryRepository.save(any(UserStory.class))).thenReturn(testUserStory);
        UserStory updated = versionService.removeUserStoryFromVersion(1L);
        assertThat(updated.getVersion()).isNull();
    }

    @Test
    void deleteVersion_shouldDeleteVersionAndUnassignUserStories() {
        List<UserStory> userStories = new ArrayList<>();
        userStories.add(testUserStory);
        when(versionRepository.findById(1L)).thenReturn(Optional.of(testVersion));
        when(userStoryRepository.findByVersionId(1L)).thenReturn(userStories);
        versionService.deleteVersion(1L);
        verify(userStoryRepository, times(1)).save(testUserStory);
        verify(versionRepository, times(1)).delete(testVersion);
    }
}