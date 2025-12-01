package com.taskforge.models;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

/**
 * Entité représentant un projet.
 * Un projet est créé par un propriétaire et peut avoir plusieurs membres.
 * Il sert de conteneur pour les User Stories et les tâches.
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Schema(description = "Identifiant unique du projet", example = "1")
    @Id
    @GeneratedValue
    private Long id;

    @Schema(description = "Nom du projet", example = "Projet Alpha")
    @Column(nullable = false)
    private String name;

    @Schema(description = "Description du projet", example = "Ce projet concerne le développement de l'application Alpha.")
    @Column(nullable=true)
    private String description;

    @Schema(description = "Propriétaire du projet", implementation = User.class)
    @ManyToOne
    @JoinColumn(name="owner_id", nullable=false, foreignKey = @ForeignKey(name = "fk_project_owner"))
    private User owner;

    @Schema(description = "Membres du projet", implementation = User.class)
    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members;

}