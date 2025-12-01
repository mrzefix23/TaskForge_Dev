package com.taskforge.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.CreateVersionRequest;
import com.taskforge.exceptions.DuplicateProjectNameException;
import com.taskforge.models.Project;
import com.taskforge.models.UserStory;
import com.taskforge.models.Version;
import com.taskforge.security.JwtFilter;
import com.taskforge.service.JwtService;
import com.taskforge.service.VersionService;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour le contrôleur des versions (VersionController).
 * Utilise MockMvc pour simuler les requêtes HTTP et Mockito pour simuler le service sous-jacent.
 */
@WebMvcTest(VersionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VersionService versionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtFilter jwtFilter;

    private Project testProject;
    private Version testVersion;
    private Version testVersion2;
    private UserStory testUserStory;
    private CreateVersionRequest createVersionRequest;

    @BeforeEach
    void setUp() {
        // Initialisation du projet de test
        testProject = Project.builder()
                .id(1L)
                .name("Projet Test")
                .description("Description du projet test")
                .build();

        // Initialisation de la version de test
        testVersion = Version.builder()
                .id(1L)
                .title("Version Initiale")
                .description("Première version du projet")
                .versionNumber("1.0.0")
                .status(Version.VersionStatus.PLANNED)
                .project(testProject)
                .build();

        // Initialisation d'une deuxième version
        testVersion2 = Version.builder()
                .id(2L)
                .title("Version Majeure")
                .description("Deuxième version avec nouvelles fonctionnalités")
                .versionNumber("2.0.0")
                .status(Version.VersionStatus.IN_PROGRESS)
                .project(testProject)
                .build();

        // Initialisation de la User Story de test
        testUserStory = UserStory.builder()
                .id(1L)
                .title("US Test")
                .description("Description de la US")
                .project(testProject)
                .build();

        // Initialisation de la requête de création
        createVersionRequest = CreateVersionRequest.builder()
                .title("Nouvelle Version")
                .description("Description de la nouvelle version")
                .versionNumber("1.1.0")
                .projectId(1L)
                .build();
    }

    /* ==================== Tests pour les endpoints ==================== */

    @Nested
    @DisplayName("GET /api/versions/project/{projectId}")
    class GetVersionsByProject {
        @Test
        @DisplayName("Devrait retourner la liste des versions pour un projet existant")
        void shouldReturnVersionsForExistingProject() throws Exception {
            List<Version> versions = Arrays.asList(testVersion, testVersion2);

            when(versionService.getVersionsByProject(1L)).thenReturn(versions);

            mockMvc.perform(get("/api/versions/project/{projectId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(testVersion.getId().intValue())))
                    .andExpect(jsonPath("$[1].id", is(testVersion2.getId().intValue())));

            verify(versionService, times(1)).getVersionsByProject(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/versions/{id}")
    class GetVersionById {
        @Test
        @DisplayName("Devrait retourner une version existante par son ID")
        void shouldReturnVersionById() throws Exception {
            when(versionService.getVersionById(1L)).thenReturn(testVersion);
            mockMvc.perform(get("/api/versions/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testVersion.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(testVersion.getTitle())));
        }
    }

    @Nested
    @DisplayName("POST /api/versions")
    class CreateVersion {
        @Test
        @DisplayName("Devrait créer une nouvelle version avec des données valides")
        void shouldCreateNewVersion() throws Exception {
            when(versionService.createVersion(any(CreateVersionRequest.class))).thenReturn(testVersion);
            mockMvc.perform(post("/api/versions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createVersionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(testVersion.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(testVersion.getTitle())));
        }
    }

    @Nested
    @DisplayName("PUT /api/versions/{id}")
    class UpdateVersion {
        @Test
        @DisplayName("Devrait mettre à jour une version existante")
        void shouldUpdateExistingVersion() throws Exception {
            when(versionService.updateVersion(eq(1L), any(CreateVersionRequest.class))).thenReturn(testVersion);
            mockMvc.perform(put("/api/versions/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createVersionRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testVersion.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(testVersion.getTitle())));
        }
    }

    @Nested
    @DisplayName("PUT /api/versions/{id}/status")
    class UpdateVersionStatus {
        @Test
        @DisplayName("Devrait mettre à jour le statut d'une version existante")
        void shouldUpdateVersionStatus() throws Exception {
            when(versionService.updateVersionStatus(1L, Version.VersionStatus.RELEASED)).thenReturn(testVersion);
            mockMvc.perform(put("/api/versions/{id}/status", 1L)
                    .param("status", "RELEASED")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testVersion.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(testVersion.getTitle())));
        }
    }

    @Nested
    @DisplayName("DELETE /api/versions/{id}")
    class DeleteVersion {
        @Test
        @DisplayName("Devrait supprimer une version existante")
        void shouldDeleteExistingVersion() throws Exception {
            doNothing().when(versionService).deleteVersion(1L);
            mockMvc.perform(delete("/api/versions/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(versionService, times(1)).deleteVersion(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/versions/{versionId}/user-stories/{userStoryId}")
    class AssignUserStoryToVersion {
        @Test
        @DisplayName("Devrait assigner une User Story à une version")
        void shouldAssignUserStoryToVersion() throws Exception {
            when(versionService.assignUserStoryToVersion(1L, 1L)).thenReturn(testUserStory);
            mockMvc.perform(post("/api/versions/{versionId}/user-stories/{userStoryId}", 1L, 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testUserStory.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(testUserStory.getTitle())));
        }
    }

    @Nested
    @DisplayName("DELETE /api/versions/{versionId}/user-stories/{userStoryId}")
    class RemoveUserStoryFromVersion {
        @Test
        @DisplayName("Devrait retirer une User Story d'une version")
        void shouldRemoveUserStoryFromVersion() throws Exception {
            when(versionService.removeUserStoryFromVersion(1L)).thenReturn(testUserStory);
            mockMvc.perform(delete("/api/versions/{versionId}/user-stories/{userStoryId}", 1L, 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(testUserStory.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(testUserStory.getTitle())));
        }
    }

    @Nested
    @DisplayName("GET /api/versions/{versionId}/user-stories")
    class GetUserStoriesByVersion {
        @Test
        @DisplayName("Devrait retourner les User Stories associées à une version")
        void shouldReturnUserStoriesByVersion() throws Exception {
            List<UserStory> userStories = Collections.singletonList(testUserStory);
            when(versionService.getUserStoriesByVersion(1L)).thenReturn(userStories);
            mockMvc.perform(get("/api/versions/{versionId}/user-stories", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(testUserStory.getId().intValue())))
                    .andExpect(jsonPath("$[0].title", is(testUserStory.getTitle())));
        }
    }
}