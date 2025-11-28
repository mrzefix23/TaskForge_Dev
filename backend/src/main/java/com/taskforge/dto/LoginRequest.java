package com.taskforge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "Nom d'utilisateur de l'utilisateur", example = "johndoe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Mot de passe de l'utilisateur", example = "password123")
    @NotBlank(message = "Password is required")
    private String password;
}