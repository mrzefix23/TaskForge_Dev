package com.taskforge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Schema(description = "Nom d'utilisateur de l'utilisateur", example = "johndoe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Adresse email de l'utilisateur", example = "johndoe@example.com")
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Mot de passe de l'utilisateur", example = "password123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}