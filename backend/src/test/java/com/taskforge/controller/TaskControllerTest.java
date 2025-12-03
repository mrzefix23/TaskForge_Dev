package com.taskforge.controller;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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
import com.taskforge.dto.CreateTaskRequest;
import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.models.Task;
import com.taskforge.models.UserStory;

/**
 * Tests d'intégration pour le contrôleur des tâches (TaskController).
 * Vérifie les opérations CRUD sur les tâches au sein des User Stories.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Long projectId;
    private Long userStoryId;

    /**
     * Prépare l'environnement de test avant chaque exécution.
     * Nettoie la base de données, crée un utilisateur, un projet et une user story de test.
     */
    @BeforeEach
    void setup() throws Exception {
        // Nettoyer la base de données
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM user_story_assignees");
        jdbcTemplate.execute("DELETE FROM user_stories");
        jdbcTemplate.execute("DELETE FROM kanban_columns");
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");
        jdbcTemplate.execute("DELETE FROM users");

        // Créer un utilisateur propriétaire
        RegisterRequest ownerRequest = new RegisterRequest();
        ownerRequest.setUsername("owner");
        ownerRequest.setEmail("owner@example.com");
        ownerRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
                .andExpect(status().isOk());

        // Créer un second utilisateur pour les assignations
        RegisterRequest memberRequest = new RegisterRequest();
        memberRequest.setUsername("member");
        memberRequest.setEmail("member@example.com");
        memberRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isOk());

        // Créer un projet
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("Test Project for Tasks");
        projectRequest.setDescription("Project for testing tasks");
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

        // Créer une user story
        CreateUserStoryRequest userStoryRequest = new CreateUserStoryRequest();
        userStoryRequest.setTitle("Test User Story");
        userStoryRequest.setDescription("User Story for testing tasks");
        userStoryRequest.setProjectId(projectId);
        userStoryRequest.setPriority(UserStory.Priority.MEDIUM);
        userStoryRequest.setStatus("TODO");

        String userStoryResponse = mockMvc.perform(post("/api/user-stories")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userStoryRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        userStoryId = objectMapper.readTree(userStoryResponse).get("id").asLong();
    }

    /**
     * Vérifie qu'un utilisateur authentifié peut créer une tâche.
     */
    @Test
    @WithMockUser(username = "owner")
    void createTask_shouldReturnCreatedTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Test Task");
        request.setDescription("This is a test task");
        request.setUserStoryId(userStoryId);
        request.setPriority(Task.Priority.HIGH);
        request.setStatus(Task.Status.TODO);
        request.setAssignedToUsername("member");

        mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("This is a test task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    /**
     * Vérifie qu'on ne peut pas créer une tâche avec un titre dupliqué dans la même user story.
     */
    @Test
    @WithMockUser(username = "owner")
    void createTask_withDuplicateTitle_shouldReturnBadRequest() throws Exception {
        // Créer la première tâche
        CreateTaskRequest request1 = new CreateTaskRequest();
        request1.setTitle("Duplicate Task");
        request1.setDescription("First task");
        request1.setUserStoryId(userStoryId);
        request1.setPriority(Task.Priority.MEDIUM);
        request1.setStatus(Task.Status.TODO);

        mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Tenter de créer une seconde tâche avec le même titre
        CreateTaskRequest request2 = new CreateTaskRequest();
        request2.setTitle("Duplicate Task");
        request2.setDescription("Second task");
        request2.setUserStoryId(userStoryId);
        request2.setPriority(Task.Priority.LOW);
        request2.setStatus(Task.Status.TODO);

        mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Vérifie qu'on peut créer une tâche sans assignation.
     */
    @Test
    @WithMockUser(username = "owner")
    void createTask_withoutAssignee_shouldSucceed() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Unassigned Task");
        request.setDescription("Task without assignee");
        request.setUserStoryId(userStoryId);
        request.setPriority(Task.Priority.MEDIUM);
        request.setStatus(Task.Status.TODO);

        mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Unassigned Task"))
                .andExpect(jsonPath("$.assignedTo").doesNotExist());
    }

    /**
     * Vérifie qu'on peut récupérer une tâche par son ID.
     */
    @Test
    @WithMockUser(username = "owner")
    void getTask_shouldReturnTask() throws Exception {
        // Créer une tâche
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task to Get");
        request.setDescription("Task for testing get");
        request.setUserStoryId(userStoryId);
        request.setPriority(Task.Priority.HIGH);
        request.setStatus(Task.Status.IN_PROGRESS);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Récupérer la tâche
        mockMvc.perform(get("/api/tasks/" + taskId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Task to Get"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    /**
     * Vérifie qu'une tentative de récupération d'une tâche inexistante retourne 404.
     */
    @Test
    @WithMockUser(username = "owner")
    void getTask_withNonExistentId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/99999")
                .with(user("owner")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Vérifie qu'on peut récupérer toutes les tâches d'une user story.
     */
    @Test
    @WithMockUser(username = "owner")
    void getTasksByUserStory_shouldReturnAllTasks() throws Exception {
        // Créer plusieurs tâches
        for (int i = 1; i <= 3; i++) {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Task " + i);
            request.setDescription("Description " + i);
            request.setUserStoryId(userStoryId);
            request.setPriority(Task.Priority.MEDIUM);
            request.setStatus(Task.Status.TODO);

            mockMvc.perform(post("/api/tasks")
                    .with(user("owner"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Récupérer toutes les tâches
        mockMvc.perform(get("/api/tasks/user-story/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Task 1", "Task 2", "Task 3")));
    }

    /**
     * Vérifie qu'on peut récupérer une liste vide de tâches pour une user story sans tâches.
     */
    @Test
    @WithMockUser(username = "owner")
    void getTasksByUserStory_withNoTasks_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/tasks/user-story/" + userStoryId)
                .with(user("owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Vérifie qu'on peut mettre à jour une tâche.
     */
    @Test
    @WithMockUser(username = "owner")
    void updateTask_shouldReturnUpdatedTask() throws Exception {
        // Créer une tâche
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Original Task");
        createRequest.setDescription("Original description");
        createRequest.setUserStoryId(userStoryId);
        createRequest.setPriority(Task.Priority.LOW);
        createRequest.setStatus(Task.Status.TODO);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Mettre à jour la tâche
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated description");
        updateRequest.setUserStoryId(userStoryId);
        updateRequest.setPriority(Task.Priority.HIGH);
        updateRequest.setStatus(Task.Status.DONE);
        updateRequest.setAssignedToUsername("member");

        mockMvc.perform(put("/api/tasks/" + taskId)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    /**
     * Vérifie qu'on ne peut pas mettre à jour une tâche avec un titre déjà utilisé.
     */
    @Test
    @WithMockUser(username = "owner")
    void updateTask_withDuplicateTitle_shouldReturnBadRequest() throws Exception {
        // Créer deux tâches
        CreateTaskRequest request1 = new CreateTaskRequest();
        request1.setTitle("Task 1");
        request1.setDescription("First task");
        request1.setUserStoryId(userStoryId);
        request1.setPriority(Task.Priority.MEDIUM);
        request1.setStatus(Task.Status.TODO);

        mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        CreateTaskRequest request2 = new CreateTaskRequest();
        request2.setTitle("Task 2");
        request2.setDescription("Second task");
        request2.setUserStoryId(userStoryId);
        request2.setPriority(Task.Priority.MEDIUM);
        request2.setStatus(Task.Status.TODO);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long task2Id = objectMapper.readTree(createResponse).get("id").asLong();

        // Tenter de mettre à jour Task 2 avec le titre de Task 1
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Task 1");
        updateRequest.setDescription("Updated task 2");
        updateRequest.setUserStoryId(userStoryId);
        updateRequest.setPriority(Task.Priority.HIGH);
        updateRequest.setStatus(Task.Status.TODO);

        mockMvc.perform(put("/api/tasks/" + task2Id)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Vérifie qu'on peut mettre à jour une tâche avec le même titre (pas de changement).
     */
    @Test
    @WithMockUser(username = "owner")
    void updateTask_withSameTitle_shouldSucceed() throws Exception {
        // Créer une tâche
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Task to Update");
        createRequest.setDescription("Original description");
        createRequest.setUserStoryId(userStoryId);
        createRequest.setPriority(Task.Priority.MEDIUM);
        createRequest.setStatus(Task.Status.TODO);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Mettre à jour avec le même titre mais une description différente
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Task to Update");
        updateRequest.setDescription("New description");
        updateRequest.setUserStoryId(userStoryId);
        updateRequest.setPriority(Task.Priority.HIGH);
        updateRequest.setStatus(Task.Status.IN_PROGRESS);

        mockMvc.perform(put("/api/tasks/" + taskId)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task to Update"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    /**
     * Vérifie qu'on ne peut pas mettre à jour une tâche inexistante.
     */
    @Test
    @WithMockUser(username = "owner")
    void updateTask_withNonExistentId_shouldReturnBadRequest() throws Exception {
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Non-existent Task");
        updateRequest.setDescription("Description");
        updateRequest.setUserStoryId(userStoryId);
        updateRequest.setPriority(Task.Priority.MEDIUM);
        updateRequest.setStatus(Task.Status.TODO);

        mockMvc.perform(put("/api/tasks/99999")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Vérifie qu'on peut supprimer une tâche.
     */
    @Test
    @WithMockUser(username = "owner")
    void deleteTask_shouldReturnNoContent() throws Exception {
        // Créer une tâche
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task to Delete");
        request.setDescription("This task will be deleted");
        request.setUserStoryId(userStoryId);
        request.setPriority(Task.Priority.LOW);
        request.setStatus(Task.Status.TODO);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Supprimer la tâche
        mockMvc.perform(delete("/api/tasks/" + taskId)
                .with(user("owner")))
                .andExpect(status().isNoContent());

        // Vérifier que la tâche n'existe plus
        mockMvc.perform(get("/api/tasks/" + taskId)
                .with(user("owner")))
                .andExpect(status().isNotFound());
    }

    /**
     * Vérifie qu'on ne peut pas supprimer une tâche inexistante.
     */
    @Test
    @WithMockUser(username = "owner")
    void deleteTask_withNonExistentId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/99999")
                .with(user("owner")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Vérifie qu'on peut changer l'assignation d'une tâche.
     */
    @Test
    @WithMockUser(username = "owner")
    void updateTask_changeAssignee_shouldSucceed() throws Exception {
        // Créer une tâche assignée à member
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Task to Reassign");
        createRequest.setDescription("Task for reassignment test");
        createRequest.setUserStoryId(userStoryId);
        createRequest.setPriority(Task.Priority.MEDIUM);
        createRequest.setStatus(Task.Status.TODO);
        createRequest.setAssignedToUsername("member");

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Réassigner à owner
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Task to Reassign");
        updateRequest.setDescription("Task for reassignment test");
        updateRequest.setUserStoryId(userStoryId);
        updateRequest.setPriority(Task.Priority.MEDIUM);
        updateRequest.setStatus(Task.Status.IN_PROGRESS);
        updateRequest.setAssignedToUsername("owner");

        mockMvc.perform(put("/api/tasks/" + taskId)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    /**
     * Vérifie qu'on peut retirer l'assignation d'une tâche.
     */
    @Test
    @WithMockUser(username = "owner")
    void updateTask_removeAssignee_shouldSucceed() throws Exception {
        // Créer une tâche assignée
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Task with Assignee");
        createRequest.setDescription("Task that will be unassigned");
        createRequest.setUserStoryId(userStoryId);
        createRequest.setPriority(Task.Priority.MEDIUM);
        createRequest.setStatus(Task.Status.TODO);
        createRequest.setAssignedToUsername("member");

        String createResponse = mockMvc.perform(post("/api/tasks")
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // Retirer l'assignation
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Task with Assignee");
        updateRequest.setDescription("Task that will be unassigned");
        updateRequest.setUserStoryId(userStoryId);
        updateRequest.setPriority(Task.Priority.MEDIUM);
        updateRequest.setStatus(Task.Status.TODO);
        // assignedToUsername = null

        mockMvc.perform(put("/api/tasks/" + taskId)
                .with(user("owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").doesNotExist());
    }
}

