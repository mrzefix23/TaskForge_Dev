package com.taskforge.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Entité représentant une User Story.
 * Une User Story décrit une fonctionnalité ou un besoin du point de vue de l'utilisateur final.
 * Elle est liée à un projet et peut être décomposée en plusieurs tâches techniques.
 */
@Entity
@Table(name = "user_stories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStory {
    
    @Schema(description = "Identifiant unique de la User Story", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "Titre de la User Story", example = "En tant qu'utilisateur, je veux pouvoir m'authentifier")
    @Column(nullable = false)
    private String title;
    
    @Schema(description = "Description détaillée de la User Story", example = "L'utilisateur doit pouvoir se connecter avec un nom d'utilisateur et un mot de passe")
    @Column(length = 2000)
    private String description;
    
    @Schema(description = "Priorité de la User Story", example = "HIGH")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Schema(description = "Statut de la User Story", example = "TODO")
    @Column(nullable = false)
    private String status;
    
    @Schema(description = "Projet auquel appartient la User Story")
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Schema(description = "Sprint auquel appartient la User Story")
    @ManyToOne
    @JoinColumn(name = "sprint_id", nullable = true)
    private Sprint sprint;
    
    @Schema(description = "Utilisateurs assignés à la User Story")
    @ManyToMany
    @JoinTable(
        name = "user_story_assignees",
        joinColumns = @JoinColumn(name = "user_story_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedTo = new HashSet<>();
    
    @Schema(description = "Tâches associées à la User Story")
    @OneToMany(mappedBy = "userStory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Task> tasks;

    @Schema(description = "Version associée à la User Story")
    @ManyToOne
    @JoinColumn(name = "version_id", nullable = true)
    @JsonIgnoreProperties({"userStories", "project"})
    private Version version;

    @Schema(description = "Colonne Kanban dans laquelle se trouve la User Story")
    @ManyToOne
    @JoinColumn(name = "kanban_column_id", nullable = true)
    @JsonIgnoreProperties({"project"})
    private KanbanColumn kanbanColumn;
    
    @Schema(description = "Priorité de la User Story", example = "HIGH")
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}