package com.taskforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.repositories.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import com.taskforge.models.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskforge.dto.LoginRequest;

/**
 * Tests d'intégration pour le contrôleur d'authentification (AuthController).
 * Ces tests vérifient le bon fonctionnement des endpoints d'inscription et de connexion
 * en simulant des requêtes HTTP via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    /**
     * Teste le scénario d'inscription réussie.
     * Vérifie que l'appel à /api/auth/register avec des données valides retourne un statut 200 OK
     * et une réponse contenant le nom d'utilisateur, l'email et un token JWT.
     */
    @Test
    public void testRegisterShouldReturnAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest("damien", "damien@example.com", "damien_password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("damien"))
                .andExpect(jsonPath("$.email").value("damien@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * Teste le scénario de connexion réussie.
     * Inscrit d'abord un utilisateur, puis tente de se connecter avec les mêmes identifiants.
     * Vérifie que la réponse contient les informations de l'utilisateur et un token JWT.
     */
    @Test
    public void testLoginShouldReturnAuthResponse() throws Exception {
        // First, register a user to ensure they exist
        RegisterRequest registerRequest = new RegisterRequest("nolan", "nolan@example.com", "nolan_password");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Now, attempt to log in
        LoginRequest loginRequest = new LoginRequest("nolan", "nolan_password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nolan"))
                .andExpect(jsonPath("$.email").value("nolan@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * Teste le scénario d'échec de connexion avec un mauvais mot de passe.
     * Inscrit un utilisateur, puis tente de se connecter avec un mot de passe erroné.
     * Vérifie que l'API retourne un statut 401 Unauthorized.
     */
    @Test
    public void testLoginShouldFailWithWrongPassword() throws Exception {
        // Register a user
        RegisterRequest registerRequest = new RegisterRequest("alice", "alice@example.com", "alice_password");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Attempt to log in with wrong password
        LoginRequest loginRequest = new LoginRequest("alice", "wrong_password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}