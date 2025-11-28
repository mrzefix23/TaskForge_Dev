package com.taskforge.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.util.List;

@Data
public class CreateProjectRequest {

    @Schema(description = "Nom du projet", example = "Nouveau Projet")
    private String name;
    @Schema(description = "Description du projet", example = "Description détaillée du projet")
    private String description;
    @Schema(description = "Utilisateur propriétaire du projet")
    private UserDto user;
    @Schema(description = "Liste des membres du projet")
    private List<UserDto> members;
}