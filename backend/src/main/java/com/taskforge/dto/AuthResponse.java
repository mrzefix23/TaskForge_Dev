package com.taskforge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    @Schema(description = "Nom d'utilisateur", example = "johndoe")
    private String username;
    @Schema(description = "Adresse email de l'utilisateur", example = "johndoe@example.com")
    private String email;
    @Schema(description = "Jeton d'authentification", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    @Schema(description = "Message d'erreur en cas d'échec de l'authentification", example = "Nom d'utilisateur ou mot de passe incorrect")
    private String error;
}