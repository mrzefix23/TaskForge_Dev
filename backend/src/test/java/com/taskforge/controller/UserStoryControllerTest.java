package com.taskforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.models.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur des User Stories (UserStoryController).
 * Vérifie les opérations CRUD sur les User Stories au sein d'un projet.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserStoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long projectId;

    /**
     * Prépare l'environnement de test avant chaque exécution.
     * Nettoie la base de données, crée un utilisateur propriétaire et un projet de test.
     */
    @BeforeEach
    void setup() throws Exception {
        // Nettoyer la base de données avant chaque test
        jdbcTemplate.execute("DELETE FROM user_story_assignees");
        jdbcTemplate.execute("DELETE FROM user_stories");
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");
        jdbcTemplate.execute("DELETE FROM users");

        // Créer un utilisateur propriétaire (owner)
        RegisterRequest ownerRequest = new RegisterRequest();
        ownerRequest.setUsername("owner");
        ownerRequest.setEmail("owner@example.com");
        ownerRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
                .andExpect(status().isOk());

        // Créer un projet de test avec le propriétaire
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("Test Project for User Stories");
        projectRequest.setDescription("Project for testing user stories");
        projectRequest.setUser(UserDto.builder().username("owner").build());
        projectRequest.setMembers(List.of());

        String projectResponse = mockMvc.perform(post("/api/projects")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        projectId = objectMapper.readTree(projectResponse).get("id").asLong();
    }

    /**
     * Vérifie qu'un utilisateur authentifié (propriétaire) peut créer une User Story dans son projet.
     */
    @Test
    @WithMockUser(username = "owner")
    void createUserStory_shouldReturnCreatedUserStory() throws Exception {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setTitle("Test User Story");
        request.setDescription("This is a test user story");
        request.setProjectId(projectId);
        request.setPriority(UserStory.Priority.MEDIUM);
        request.setStatus(UserStory.Status.TODO);

        mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test User Story"))
                .andExpect(jsonPath("$.description").value("This is a test user story"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    /**
     * Vérifie la récupération de la liste des User Stories associées à un projet spécifique.
     */
    @Test
    @WithMockUser(username = "owner")
    void getUserStoriesByProject_shouldReturnUserStories() throws Exception {
        // Créer plusieurs user stories pour le projet
        for (int i = 1; i <= 3; i++) {
            CreateUserStoryRequest request = new CreateUserStoryRequest();
            request.setTitle("User Story " + i);
            request.setDescription("Description for user story " + i);
            request.setProjectId(projectId);
            request.setPriority(UserStory.Priority.LOW);
            request.setStatus(UserStory.Status.TODO);

            mockMvc.perform(post("/api/user-stories")
                    .with(user("owner"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // Récupérer les user stories pour le projet
        mockMvc.perform(get("/api/user-stories/project/" + projectId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("User Story 1"))
                .andExpect(jsonPath("$[1].title").value("User Story 2"))
                .andExpect(jsonPath("$[2].title").value("User Story 3"));
    }

    /**
     * Vérifie la récupération des détails d'une User Story spécifique par son ID.
     */
    @Test
    @WithMockUser(username = "owner")
    void getUserStoryById_shouldReturnUserStory() throws Exception {
        // Créer une user story spécifique
        CreateUserStoryRequest createRequest = new CreateUserStoryRequest();
        createRequest.setTitle("Specific User Story");
        createRequest.setDescription("Detailed description");
        createRequest.setProjectId(projectId);
        createRequest.setPriority(UserStory.Priority.HIGH);
        createRequest.setStatus(UserStory.Status.IN_PROGRESS);

        String createResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userStoryId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/user-stories/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Specific User Story"))
                .andExpect(jsonPath("$.description").value("Detailed description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    /**
     * Vérifie la mise à jour des informations d'une User Story existante.
     */
    @Test
    @WithMockUser(username = "owner")
    void updateUserStory_shouldModifyFields() throws Exception {
        // Créer une user story à mettre à jour
        CreateUserStoryRequest createRequest = new CreateUserStoryRequest();
        createRequest.setTitle("Original Title");
        createRequest.setDescription("Original description");
        createRequest.setProjectId(projectId);
        createRequest.setPriority(UserStory.Priority.LOW);
        createRequest.setStatus(UserStory.Status.TODO);

        String createResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userStoryId = objectMapper.readTree(createResponse).get("id").asLong();

        CreateUserStoryRequest updateRequest = new CreateUserStoryRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated description");
        updateRequest.setPriority(UserStory.Priority.HIGH);
        updateRequest.setStatus(UserStory.Status.DONE);

        mockMvc.perform(put("/api/user-stories/" + userStoryId)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    /**
     * Vérifie la suppression d'une User Story par le propriétaire du projet.
     */
    @Test
    @WithMockUser(username = "owner")
    void deleteUserStory_shouldSucceed() throws Exception {
        // Créer une user story à supprimer
        CreateUserStoryRequest createRequest = new CreateUserStoryRequest();
        createRequest.setTitle("User Story to Delete");
        createRequest.setDescription("Will be deleted");
        createRequest.setProjectId(projectId);
        createRequest.setPriority(UserStory.Priority.MEDIUM);
        createRequest.setStatus(UserStory.Status.TODO);

        String createResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userStoryId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/user-stories/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user-stories/project/" + projectId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Vérifie que la création d'une User Story échoue si l'utilisateur n'est pas authentifié.
     */
    @Test
    void createUserStory_shouldFail_whenNotAuthenticated() throws Exception {
        // Tenter de créer une user story sans être authentifié
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setProjectId(projectId);
        request.setTitle("Unauthorized Story");
        request.setDescription("Should fail");
        request.setPriority(UserStory.Priority.LOW);
        request.setStatus(UserStory.Status.TODO);

        mockMvc.perform(post("/api/user-stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}