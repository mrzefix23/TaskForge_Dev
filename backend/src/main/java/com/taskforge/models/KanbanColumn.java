package com.taskforge.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Entité représentant une colonne personnalisée du tableau Kanban.
 * Permet aux utilisateurs de créer des colonnes avec des statuts personnalisés
 * pour organiser leurs User Stories selon leur workflow.
 */
@Entity
@Table(name = "kanban_columns", uniqueConstraints = {
    @UniqueConstraint(name = "uk_status_project", columnNames = {"status", "project_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanColumn {
    
    @Schema(description = "Identifiant unique de la colonne Kanban", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "Nom de la colonne Kanban", example = "À faire")
    @Column(nullable = false)
    private String name;
    
    @Schema(description = "Statut associé à la colonne Kanban", example = "TODO")
    @Column(nullable = false)
    private String status;
    
    @Schema(description = "Ordre de la colonne Kanban dans le tableau", example = "1")
    @Column(name = "column_order", nullable = false)
    private Integer order;
    
    @Schema(description = "Projet auquel appartient la colonne Kanban")
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Schema(description = "Indique si la colonne Kanban est une colonne par défaut", example = "false")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
