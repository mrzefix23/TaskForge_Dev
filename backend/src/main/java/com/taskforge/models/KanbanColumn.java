package com.taskforge.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité représentant une colonne personnalisée du tableau Kanban.
 * Permet aux utilisateurs de créer des colonnes avec des statuts personnalisés
 * pour organiser leurs User Stories selon leur workflow.
 */
@Entity
@Table(name = "kanban_columns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanColumn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String status;
    
    @Column(name = "column_order", nullable = false)
    private Integer order;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
