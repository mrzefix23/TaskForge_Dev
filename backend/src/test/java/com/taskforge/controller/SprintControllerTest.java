package com.taskforge.controller;

import java.time.LocalDate;
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
import com.taskforge.dto.CreateSprintRequest;
import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.models.Sprint;
import com.taskforge.models.UserStory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SprintControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long projectId;
    private Long sprintId;

    @BeforeEach
    void setup() throws Exception {
        // Nettoyer la base de données
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM user_story_assignees");
        jdbcTemplate.execute("DELETE FROM user_stories");
        jdbcTemplate.execute("DELETE FROM sprints");
        jdbcTemplate.execute("DELETE FROM kanban_columns");
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");
        jdbcTemplate.execute("DELETE FROM users");

        // Créer l'utilisateur owner
        RegisterRequest ownerRequest = new RegisterRequest();
        ownerRequest.setUsername("owner");
        ownerRequest.setEmail("owner@example.com");
        ownerRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
                .andExpect(status().isOk());

        // Créer un membre
        RegisterRequest memberRequest = new RegisterRequest();
        memberRequest.setUsername("member");
        memberRequest.setEmail("member@example.com");
        memberRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isOk());

        // Créer le projet
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("Test Project");
        projectRequest.setDescription("Project for testing sprints");
        projectRequest.setUser(UserDto.builder().username("owner").build());
        projectRequest.setMembers(List.of(UserDto.builder().username("member").build()));

        String projectResponse = mockMvc.perform(post("/api/projects")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        projectId = objectMapper.readTree(projectResponse).get("id").asLong();

        // Supprimer les colonnes Kanban par défaut
        jdbcTemplate.execute("DELETE FROM kanban_columns WHERE project_id = " + projectId);

        // Créer un sprint de base pour certains tests
        CreateSprintRequest sprintRequest = new CreateSprintRequest();
        sprintRequest.setName("Sprint 1");
        sprintRequest.setStartDate(LocalDate.now());
        sprintRequest.setEndDate(LocalDate.now().plusWeeks(2));
        sprintRequest.setStatus(Sprint.Status.PLANNED);
        sprintRequest.setProjectId(projectId);

        String sprintResponse = mockMvc.perform(post("/api/sprints")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sprintRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        sprintId = objectMapper.readTree(sprintResponse).get("id").asLong();
    }

    @Test
    @WithMockUser(username = "owner")
    void createSprint_shouldReturnCreatedSprint() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 2");
        request.setStartDate(LocalDate.now().plusWeeks(2));
        request.setEndDate(LocalDate.now().plusWeeks(4));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        mockMvc.perform(post("/api/sprints")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 2"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.project.id").value(projectId));
    }

    @Test
    @WithMockUser(username = "member")
    void createSprint_asNonOwner_shouldReturnError() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 3");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(2));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        mockMvc.perform(post("/api/sprints")
                .with(user("member"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void createSprint_withInvalidDates_shouldReturnError() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Invalid Sprint");
        request.setStartDate(LocalDate.now().plusWeeks(2));
        request.setEndDate(LocalDate.now()); // End before start
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        mockMvc.perform(post("/api/sprints")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "owner")
    void createSprint_withDuplicateName_shouldReturnError() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1"); // Same as existing sprint
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(2));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        mockMvc.perform(post("/api/sprints")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "owner")
    void getSprintsByProject_shouldReturnAllSprints() throws Exception {
        mockMvc.perform(get("/api/sprints/project/" + projectId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Sprint 1"));
    }

    @Test
    @WithMockUser(username = "member")
    void getSprintsByProject_asMember_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/sprints/project/" + projectId)
                .with(user("member")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "owner")
    void getSprintById_shouldReturnSprint() throws Exception {
        mockMvc.perform(get("/api/sprints/" + sprintId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sprintId))
                .andExpect(jsonPath("$.name").value("Sprint 1"));
    }

    @Test
    @WithMockUser(username = "owner")
    void getSprintById_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/sprints/99999")
                .with(user("owner")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "owner")
    void updateSprint_shouldReturnUpdatedSprint() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1 Updated");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(3));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        mockMvc.perform(put("/api/sprints/" + sprintId)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 1 Updated"));
    }

    @Test
    @WithMockUser(username = "member")
    void updateSprint_asNonOwner_shouldReturnError() throws Exception {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 1 Updated");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(3));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        mockMvc.perform(put("/api/sprints/" + sprintId)
                .with(user("member"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void deleteSprint_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/sprints/" + sprintId)
                .with(user("owner")))
                .andExpect(status().isOk());

        // Vérifier que le sprint a été supprimé
        mockMvc.perform(get("/api/sprints/" + sprintId)
                .with(user("owner")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "member")
    void deleteSprint_asNonOwner_shouldReturnError() throws Exception {
        mockMvc.perform(delete("/api/sprints/" + sprintId)
                .with(user("member")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void assignUserStoryToSprint_shouldReturnUserStory() throws Exception {
        // Créer une user story
        CreateUserStoryRequest usRequest = new CreateUserStoryRequest();
        usRequest.setTitle("User Story 1");
        usRequest.setDescription("Description");
        usRequest.setPriority(UserStory.Priority.MEDIUM);
        usRequest.setStatus("TODO");
        usRequest.setProjectId(projectId);

        String usResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userStoryId = objectMapper.readTree(usResponse).get("id").asLong();

        // Assigner la user story au sprint
        mockMvc.perform(post("/api/sprints/" + sprintId + "/user-stories/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userStoryId))
                .andExpect(jsonPath("$.sprint.id").value(sprintId));
    }

    @Test
    @WithMockUser(username = "owner")
    void removeUserStoryFromSprint_shouldReturnUserStory() throws Exception {
        // Créer et assigner une user story
        CreateUserStoryRequest usRequest = new CreateUserStoryRequest();
        usRequest.setTitle("User Story 2");
        usRequest.setDescription("Description");
        usRequest.setPriority(UserStory.Priority.MEDIUM);
        usRequest.setStatus("TODO");
        usRequest.setProjectId(projectId);

        String usResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userStoryId = objectMapper.readTree(usResponse).get("id").asLong();

        mockMvc.perform(post("/api/sprints/" + sprintId + "/user-stories/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isOk());

        // Retirer la user story du sprint
        mockMvc.perform(delete("/api/sprints/user-stories/" + userStoryId + "/sprint")
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userStoryId))
                .andExpect(jsonPath("$.sprint").doesNotExist());
    }

    @Test
    @WithMockUser(username = "owner")
    void getUserStoriesBySprint_shouldReturnUserStories() throws Exception {
        // Créer et assigner une user story
        CreateUserStoryRequest usRequest = new CreateUserStoryRequest();
        usRequest.setTitle("User Story 3");
        usRequest.setDescription("Description");
        usRequest.setPriority(UserStory.Priority.MEDIUM);
        usRequest.setStatus("TODO");
        usRequest.setProjectId(projectId);

        String usResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userStoryId = objectMapper.readTree(usResponse).get("id").asLong();

        mockMvc.perform(post("/api/sprints/" + sprintId + "/user-stories/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isOk());

        // Récupérer les user stories du sprint
        mockMvc.perform(get("/api/sprints/" + sprintId + "/user-stories")
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(userStoryId));
    }

    @Test
    @WithMockUser(username = "owner")
    void getBacklogUserStories_shouldReturnUnassignedUserStories() throws Exception {
        // Créer une user story non assignée
        CreateUserStoryRequest usRequest = new CreateUserStoryRequest();
        usRequest.setTitle("Backlog Story");
        usRequest.setDescription("Description");
        usRequest.setPriority(UserStory.Priority.MEDIUM);
        usRequest.setStatus("TODO");
        usRequest.setProjectId(projectId);

        mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usRequest)))
                .andExpect(status().isOk());

        // Récupérer le backlog
        mockMvc.perform(get("/api/sprints/project/" + projectId + "/backlog")
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Backlog Story"));
    }

    @Test
    @WithMockUser(username = "owner")
    void startSprint_shouldReturnActiveSprint() throws Exception {
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "member")
    void startSprint_asNonOwner_shouldReturnError() throws Exception {
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                .with(user("member")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void startSprint_whenAlreadyActive_shouldReturnError() throws Exception {
        // Démarrer le premier sprint
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                .with(user("owner")))
                .andExpect(status().isOk());

        // Créer un deuxième sprint
        CreateSprintRequest request = new CreateSprintRequest();
        request.setName("Sprint 2");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusWeeks(2));
        request.setStatus(Sprint.Status.PLANNED);
        request.setProjectId(projectId);

        String response = mockMvc.perform(post("/api/sprints")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long sprint2Id = objectMapper.readTree(response).get("id").asLong();

        // Essayer de démarrer le deuxième sprint
        mockMvc.perform(post("/api/sprints/" + sprint2Id + "/start")
                .with(user("owner")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "owner")
    void completeSprint_shouldReturnCompletedSprint() throws Exception {
        // Démarrer le sprint d'abord
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                .with(user("owner")))
                .andExpect(status().isOk());

        // Terminer le sprint
        mockMvc.perform(post("/api/sprints/" + sprintId + "/complete")
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "member")
    void completeSprint_asNonOwner_shouldReturnError() throws Exception {
        // Démarrer le sprint d'abord
        mockMvc.perform(post("/api/sprints/" + sprintId + "/start")
                .with(user("owner")))
                .andExpect(status().isOk());

        // Essayer de terminer en tant que membre
        mockMvc.perform(post("/api/sprints/" + sprintId + "/complete")
                .with(user("member")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner")
    void completeSprint_whenNotActive_shouldReturnError() throws Exception {
        // Essayer de terminer un sprint qui n'est pas actif
        mockMvc.perform(post("/api/sprints/" + sprintId + "/complete")
                .with(user("owner")))
                .andExpect(status().isBadRequest());
    }
}

