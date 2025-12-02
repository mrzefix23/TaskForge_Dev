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
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
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
