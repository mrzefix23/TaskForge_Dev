package com.taskforge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    @Schema(description = "ID du projet", example = "1")
    private Long id;
    @Schema(description = "Nom du projet", example = "Nouveau Projet")
    private String name;
    @Schema(description = "Description du projet", example = "Description détaillée du projet")
    private String description;
    @Schema(description = "Utilisateur propriétaire du projet")
    private UserDto owner;
}

