package com.taskforge.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Entité représentant une tâche technique.
 * Une tâche est une sous-unité de travail liée à une User Story spécifique.
 * Elle possède un statut, une priorité et peut être assignée à un utilisateur.
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Schema(description = "Identifiant unique de la tâche", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "Titre de la tâche", example = "Implémenter l'authentification OAuth2")
    @Column(nullable = false)
    private String title;
    
    @Schema(description = "Description détaillée de la tâche", example = "Mettre en place OAuth2 avec JWT pour sécuriser les endpoints")
    @Column(length = 2000)
    private String description;
    
    @Schema(description = "Priorité de la tâche", example = "HIGH")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Schema(description = "Statut de la tâche", example = "TODO")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Schema(description = "User Story associée à la tâche")
    @ManyToOne
    @JoinColumn(name = "user_story_id", nullable = false)
    @JsonBackReference
    private UserStory userStory;
    
    @Schema(description = "Utilisateur assigné à la tâche", example = "john.doe")
    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id")
    @JsonIgnore
    private User assignedTo;
    
    @Schema(description = "Priorité de la tâche", example = "HIGH")
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    @Schema(description = "Statut de la tâche", example = "TODO")
    public enum Status {
        TODO, IN_PROGRESS, DONE
    }
}