package com.taskforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturnUserDto() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("damien");
        dto.setEmail("damien@gmail.com");
        dto.setPassword("password");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("damien"))
                .andExpect(jsonPath("$.email").value("damien@gmail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getAllUsers_shouldReturnListOfUserDto() throws Exception {
        // Create a user first to ensure there's at least one user in the system
        UserDto dto = new UserDto();
        dto.setUsername("alice");
        dto.setEmail("alice@gmail.com");
        dto.setPassword("password");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getUser_shouldReturnUserDto_whenUserExists() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("bob");
        dto.setEmail("bob@gmail.com");
        dto.setPassword("password");
        String response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value("bob@gmail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUser_shouldReturnNull_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}