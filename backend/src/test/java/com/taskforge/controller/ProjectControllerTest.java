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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.dto.UserDto;
import com.taskforge.repositories.UserRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws JsonProcessingException, Exception {
        jdbcTemplate.execute("DELETE FROM project_members");
        jdbcTemplate.execute("DELETE FROM projects");
        userRepository.deleteAll();
        RegisterRequest request = new RegisterRequest("owner", "owner@mail.com", "password"); 
         
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner")
    void createProject_shouldReturnOk() throws Exception {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setName("Test Project");
        req.setDescription("Description");
        req.setUser(UserDto.builder().username("owner").build());
        req.setMembers(List.of());

        mockMvc.perform(post("/api/projects")
                .principal(() -> "owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "owner")
    void createProject_withMembers_shouldReturnOk() throws Exception {
        // Register a member user
        RegisterRequest memberRequest = new RegisterRequest("member1", "member1@mail.com", "password");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRequest)))
                .andExpect(status().isOk());

        // Prepare project creation request with members
        CreateProjectRequest req = new CreateProjectRequest();
        req.setName("Project With Members");
        req.setDescription("Project including members");
        req.setUser(UserDto.builder().username("owner").build());
        req.setMembers(List.of(UserDto.builder().username("member1").build()));

        mockMvc.perform(post("/api/projects")
                .principal(() -> "owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}