package com.taskforge.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UpdateUserStoryStatusRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.models.UserStory;

/**
 * Tests d'intégration pour le contrôleur des User Stories.
 * Couvre les cas nominaux et les cas limites (authentification manquante).
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

    @BeforeEach
    void setup() throws Exception {
        // Nettoyage complet
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM user_story_assignees");
        jdbcTemplate.execute("DELETE FROM user_stories");
        jdbcTemplate.execute("DELETE FROM sprints");
        jdbcTemplate.execute("DELETE FROM kanban_columns");
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");
        jdbcTemplate.execute("DELETE FROM users");

        // Création des utilisateurs
        registerUser("owner");
        registerUser("member");
        registerUser("unauthorized");

        // Création du projet
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("Test Project for User Stories");
        projectRequest.setDescription("Project for testing user stories");
        projectRequest.setUser(UserDto.builder().username("owner").build());
        projectRequest.setMembers(List.of(UserDto.builder().username("member").build()));

        String projectResponse = mockMvc.perform(post("/api/projects")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        projectId = objectMapper.readTree(projectResponse).get("id").asLong();
    }

    // --- TESTS NOMINAUX (Happy Path) ---

    @Test
    @WithMockUser(username = "owner")
    void createUserStory_shouldReturnCreatedUserStory() throws Exception {
        CreateUserStoryRequest request = createRequest("Test Story", "TODO");

        mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Story"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @WithMockUser(username = "owner")
    void getUserStoriesByProject_shouldReturnList() throws Exception {
        createUserStory("Story 1", "TODO");
        createUserStory("Story 2", "DONE");

        mockMvc.perform(get("/api/user-stories/project/" + projectId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "owner")
    void getUserStoryById_shouldReturnStory() throws Exception {
        Long id = createUserStory("Specific Story", "IN_PROGRESS");

        mockMvc.perform(get("/api/user-stories/" + id)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Specific Story"));
    }

    @Test
    @WithMockUser(username = "owner")
    void updateUserStory_shouldUpdateFields() throws Exception {
        Long id = createUserStory("Original", "TODO");

        CreateUserStoryRequest updateRequest = createRequest("Updated", "DONE");

        mockMvc.perform(put("/api/user-stories/" + id)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "owner")
    void deleteUserStory_shouldReturnNoContent() throws Exception {
        Long id = createUserStory("To Delete", "TODO");

        mockMvc.perform(delete("/api/user-stories/" + id)
                .with(user("owner")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user-stories/" + id)
                .with(user("owner")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "owner")
    void updateUserStoryStatus_shouldUpdateStatusOnly() throws Exception {
        Long id = createUserStory("Story Status", "TODO");

        UpdateUserStoryStatusRequest request = new UpdateUserStoryStatusRequest();
        request.setStatus("DONE");

        mockMvc.perform(put("/api/user-stories/" + id + "/status")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    // --- TESTS BRANCHES : AUTHENTIFICATION MANQUANTE (Principal == null) ---
    // Ces tests sont cruciaux pour la couverture des blocs "if (principal == null)"

    @Test
    void createUserStory_noAuth_shouldReturnForbidden() throws Exception {
        CreateUserStoryRequest request = createRequest("Unauthorized", "TODO");

        mockMvc.perform(post("/api/user-stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserStoriesByProject_noAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/user-stories/project/" + projectId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserStoryById_noAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/user-stories/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserStory_noAuth_shouldReturnForbidden() throws Exception {
        CreateUserStoryRequest request = createRequest("Update", "TODO");

        mockMvc.perform(put("/api/user-stories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUserStory_noAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/user-stories/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserStoryStatus_noAuth_shouldReturnForbidden() throws Exception {
        UpdateUserStoryStatusRequest request = new UpdateUserStoryStatusRequest();
        request.setStatus("DONE");

        mockMvc.perform(put("/api/user-stories/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- TESTS BRANCHES : ERREURS MÉTIER & DROITS ---

    @Test
    @WithMockUser(username = "owner")
    void createUserStory_duplicateTitle_shouldReturnBadRequest() throws Exception {
        createUserStory("Duplicate", "TODO");
        CreateUserStoryRequest request = createRequest("Duplicate", "TODO");

        mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "unauthorized")
    void createUserStory_nonMember_shouldReturnForbidden() throws Exception {
        CreateUserStoryRequest request = createRequest("Hacker", "TODO");

        mockMvc.perform(post("/api/user-stories")
                .with(user("unauthorized"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void updateUserStory_notFound_shouldReturnNotFound() throws Exception {
        CreateUserStoryRequest request = createRequest("Ghost", "TODO");

        mockMvc.perform(put("/api/user-stories/99999")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- HELPER METHODS ---

    private void registerUser(String username) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(username + "@example.com");
        request.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private CreateUserStoryRequest createRequest(String title, String status) {
        CreateUserStoryRequest request = new CreateUserStoryRequest();
        request.setTitle(title);
        request.setDescription("Description");
        request.setProjectId(projectId);
        request.setPriority(UserStory.Priority.MEDIUM);
        request.setStatus(status);
        return request;
    }

    private Long createUserStory(String title, String status) throws Exception {
        CreateUserStoryRequest request = createRequest(title, status);
        String response = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}