package com.taskforge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.AuthResponse;
import com.taskforge.dto.LoginRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;

/**
 * Contrôleur REST gérant l'authentification des utilisateurs.
 * Fournit des points de terminaison pour l'inscription et la connexion.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Enregistre un nouvel utilisateur dans le système.
     *
     * @param request Les informations d'inscription (nom d'utilisateur, email, mot de passe).
     * @return Une réponse contenant le token d'authentification généré.
     */
    @Operation(summary = "Enregistrer un nouvel utilisateur", description="Permet à un nouvel utilisateur de s'enregistrer.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur enregistré avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Call the AuthService to register the user
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authentifie un utilisateur existant.
     *
     * @param request Les identifiants de connexion (email/username et mot de passe).
     * @return Une réponse contenant le token d'authentification si les identifiants sont valides.
     */
    @Operation(summary = "Authentifier un utilisateur", description="Permet à un utilisateur existant de se connecter.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur authentifié avec succès"),
        @ApiResponse(responseCode = "401", description = "Non autorisé")  
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Call the AuthService to authenticate the user
        return ResponseEntity.ok(authService.login(request));
    }

}
