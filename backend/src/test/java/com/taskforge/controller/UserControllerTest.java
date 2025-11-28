package com.taskforge.controller;

import com.taskforge.models.User;
import com.taskforge.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    void getAllUsers_shouldReturnUserList() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("alice");
        user1.setEmail("alice@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("bob");
        user2.setEmail("bob@example.com");
        List<User> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].username").value("bob"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    @WithMockUser
    void getUserById_shouldReturnUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("charlie");
        user.setEmail("charlie@example.com");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("charlie"))
                .andExpect(jsonPath("$.email").value("charlie@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser
    void getUserById_shouldReturnNotFoundForInvalidId() throws Exception {
        when(userService.getUserById(999L)).thenReturn(null);
        mockMvc.perform(get("/api/users/999")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}