package com.taskforge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    @Schema(description = "Nom d'utilisateur de l'utilisateur", example = "johndoe")
    private String username;
    @Schema(description = "Adresse email de l'utilisateur", example = "johndoe@example.com")
    private String email;
    @Schema(description = "Mot de passe de l'utilisateur", example = "password123")
    private String password;
}
