package com.taskforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @BeforeEach
    void setup() throws Exception {
        // Nettoyer les tables avant chaque test
        jdbcTemplate.execute("DELETE FROM user_story_assignees");
        jdbcTemplate.execute("DELETE FROM user_stories");
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");        
        jdbcTemplate.execute("DELETE FROM users");        

        // Créer un utilisateur de test
        RegisterRequest userRequest = new RegisterRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("testuser@example.com");
        userRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void createProject_shouldReturnCreatedProject() throws Exception {
        // Créer un projet
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

    @Test
    void getProjectsByUsername_shouldReturnUserProjects() throws Exception {
        // Créer un projet
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

        // Récupérer les projets de l'utilisateur
        mockMvc.perform(get("/api/projects/myprojects")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"))
                .andExpect(jsonPath("$[0].description").value("Description"));
    }

    @Test
    void getProjectById_shouldReturnProject() throws Exception {
        // Créer un projet
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
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        // Récupérer le projet par ID
        mockMvc.perform(get("/api/projects/" + projectId)
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Specific Project"))
                .andExpect(jsonPath("$.description").value("Specific Description"));
    }

    @Test
    void updateProject_shouldChangeNameAndDescription() throws Exception {
        // Créer un projet
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
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        // Mettre à jour le projet
        CreateProjectRequest updateRequest = new CreateProjectRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");

        mockMvc.perform(put("/api/projects/" + projectId)
                .with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void deleteProject_shouldRemoveProject() throws Exception {
        // Créer un projet
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
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long projectId = objectMapper.readTree(createResponse).get("id").asLong();

        // Supprimer le projet
        mockMvc.perform(delete("/api/projects/" + projectId)
                .with(user("testuser")))
                .andExpect(status().isNoContent());

        // Vérifier que le projet n'existe plus
        mockMvc.perform(get("/api/projects/myprojects")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}