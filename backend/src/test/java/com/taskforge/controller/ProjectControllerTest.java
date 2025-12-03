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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
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
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UserDto;

/**
 * Tests d'intégration pour le contrôleur de projets (ProjectController).
 * Vérifie les opérations CRUD sur les projets ainsi que les règles de sécurité associées.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Prépare l'environnement de test avant chaque exécution.
     * Nettoie la base de données et crée un utilisateur de test par défaut.
     */
    @BeforeEach
    void setup() throws Exception {
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("DELETE FROM user_story_assignees");
        jdbcTemplate.execute("DELETE FROM user_stories");
        jdbcTemplate.execute("DELETE FROM sprints");
        jdbcTemplate.execute("DELETE FROM kanban_columns");
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");        
        jdbcTemplate.execute("DELETE FROM users");        

        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("testuser@example.com");
        userRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
    }

    /**
     * Vérifie qu'un utilisateur authentifié peut créer un projet avec succès.
     */
    @Test
    void createProject_shouldReturnCreatedProject() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setDescription("Project Description");
        request.setUser(UserDto.builder().username("testuser").build());
        request.setMembers(List.of());

        mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description").value("Project Description"));
    }

    /**
     * Vérifie qu'il est interdit de créer un projet au nom d'un autre utilisateur.
     */
    @Test
    void createProject_shouldReturnForbidden_WhenUsernameMismatch() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Hacked Project");
        request.setUser(UserDto.builder().username("testuser").build());
        request.setMembers(List.of());

        mockMvc.perform(post("/api/projects")
                .with(user("hacker")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    /**
     * Vérifie que la création de projet échoue si l'utilisateur n'est pas authentifié.
     */
    @Test
    void createProject_shouldFail_WhenPrincipalIsNull() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Project");
        request.setUser(UserDto.builder().username("testuser").build());

        mockMvc.perform(post("/api/projects")
                .with(anonymous()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError()); 
    }

    /**
     * Vérifie que l'utilisateur peut récupérer la liste de ses projets.
     */
    @Test
    void getProjectsByUsername_shouldReturnUserProjects() throws Exception {
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Test Project");
        createRequest.setDescription("Description");
        createRequest.setUser(UserDto.builder().username("testuser").build());
        createRequest.setMembers(List.of());

        mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/myprojects")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    /**
     * Vérifie que la récupération des projets échoue sans authentification.
     */
    @Test
    void getMyProjects_shouldFail_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/projects/myprojects")
                .with(anonymous()))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Vérifie qu'un utilisateur peut récupérer les détails d'un projet spécifique auquel il a accès.
     */
    @Test
    void getProjectById_shouldReturnProject() throws Exception {
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Specific Project");
        createRequest.setDescription("Specific Description");
        createRequest.setUser(UserDto.builder().username("testuser").build());
        createRequest.setMembers(List.of());

        String createResponse = mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/projects/" + projectId)
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Specific Project"));
    }

    /**
     * Vérifie que la récupération d'un projet par ID échoue sans authentification.
     */
    @Test
    void getProjectById_shouldFail_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/projects/999")
                .with(anonymous()))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Vérifie qu'un utilisateur peut mettre à jour les informations d'un projet dont il est propriétaire.
     */
    @Test
    void updateProject_shouldChangeNameAndDescription() throws Exception {
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Original Name");
        createRequest.setDescription("Original Description");
        createRequest.setUser(UserDto.builder().username("testuser").build());
        createRequest.setMembers(List.of());

        String createResponse = mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");

        mockMvc.perform(put("/api/projects/" + projectId)
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    /**
     * Vérifie que la mise à jour d'un projet échoue sans authentification.
     */
    @Test
    void updateProject_shouldFail_WhenNotAuthenticated() throws Exception {
        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Update");
        
        mockMvc.perform(put("/api/projects/1")
                .with(anonymous())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Vérifie qu'un utilisateur peut supprimer un projet dont il est propriétaire.
     */
    @Test
    void deleteProject_shouldRemoveProject() throws Exception {
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Project to Delete");
        createRequest.setDescription("Will be deleted");
        createRequest.setUser(UserDto.builder().username("testuser").build());
        createRequest.setMembers(List.of());

        String createResponse = mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/api/projects/" + projectId)
                .with(user("testuser")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/myprojects")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Vérifie que la suppression d'un projet échoue sans authentification.
     */
    @Test
    void deleteProject_shouldFail_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/projects/1")
                .with(anonymous()))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Tests pour les branches principal == null
     */
    @Test
    void getProjectById_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProjects_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/projects/myprojects"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProject_withoutAuthentication_shouldReturnForbidden() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Updated Project");
        request.setDescription("Updated");
        request.setMembers(List.of());

        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProject_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test pour la branche de validation du username dans createProject
     */
    @Test
    @WithMockUser(username = "testuser")
    void createProject_withMismatchedUsername_shouldReturnForbidden() throws Exception {
        // testuser est déjà créé dans setup()
        
        // Créer une requête avec un username différent de l'utilisateur authentifié
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Forbidden Project");
        request.setDescription("Should be forbidden");
        request.setMembers(List.of());
        
        UserDto differentUser = UserDto.builder()
                .username("otheruser")  // Différent de "testuser"
                .build();
        request.setUser(differentUser);

        mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests pour les erreurs du service
     */
    @Test
    @WithMockUser(username = "testuser")
    void getProjectById_withNonExistentId_shouldReturnNotFound() throws Exception {
        // testuser déjà créé dans setup()
        
        mockMvc.perform(get("/api/projects/99999")
                .with(user("testuser")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProject_withNonExistentId_shouldReturnNotFound() throws Exception {
        // testuser déjà créé dans setup()
        
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Updated Project");
        request.setDescription("Updated");
        request.setMembers(List.of());
        UserDto user = UserDto.builder().username("testuser").build();
        request.setUser(user);

        mockMvc.perform(put("/api/projects/99999")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "unauthorized")
    void updateProject_asNonOwner_shouldReturnForbidden() throws Exception {
        // Créer le projet en tant que testuser (déjà enregistré dans setup())
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Test Project");
        createRequest.setDescription("Description");
        createRequest.setMembers(List.of());
        UserDto owner = UserDto.builder().username("testuser").build();
        createRequest.setUser(owner);

        String createResponse = mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        // Créer l'utilisateur non autorisé
        RegisterRequest unauthorizedRequest = new RegisterRequest();
        unauthorizedRequest.setUsername("unauthorized");
        unauthorizedRequest.setEmail("unauthorized@example.com");
        unauthorizedRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unauthorizedRequest)))
                .andExpect(status().isOk());

        // Tenter de mettre à jour en tant qu'utilisateur non autorisé
        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Hacked Project");
        updateRequest.setDescription("Hacked");
        updateRequest.setMembers(List.of());
        UserDto hacker = UserDto.builder().username("unauthorized").build();
        updateRequest.setUser(hacker);

        mockMvc.perform(put("/api/projects/" + projectId)
                .with(user("unauthorized"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteProject_withNonExistentId_shouldReturnNotFound() throws Exception {
        // testuser déjà créé dans setup()
        
        mockMvc.perform(delete("/api/projects/99999")
                .with(user("testuser")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "unauthorized")
    void deleteProject_asNonOwner_shouldReturnForbidden() throws Exception {
        // Créer le projet en tant que testuser (déjà enregistré dans setup())
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Test Project");
        createRequest.setDescription("Description");
        createRequest.setMembers(List.of());
        UserDto owner = UserDto.builder().username("testuser").build();
        createRequest.setUser(owner);

        String createResponse = mockMvc.perform(post("/api/projects")
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        // Créer l'utilisateur non autorisé
        RegisterRequest unauthorizedRequest = new RegisterRequest();
        unauthorizedRequest.setUsername("unauthorized");
        unauthorizedRequest.setEmail("unauthorized@example.com");
        unauthorizedRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unauthorizedRequest)))
                .andExpect(status().isOk());

        // Tenter de supprimer en tant qu'utilisateur non autorisé
        mockMvc.perform(delete("/api/projects/" + projectId)
                .with(user("unauthorized")))
                .andExpect(status().isForbidden());
    }
}
