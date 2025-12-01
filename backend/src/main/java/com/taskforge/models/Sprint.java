package com.taskforge.models;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
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

@Entity
@Table(name = "sprints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {

    @Schema(description = "Identifiant unique du sprint", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nom du sprint", example = "Sprint 1")
    @Column(nullable = false)
    private String name;

    @Schema(description = "Date de début du sprint", example = "2025-12-01")
    @Column(nullable = false)
    private LocalDate startDate;

    @Schema(description = "Date de fin du sprint", example = "2025-12-15")
    @Column(nullable = false)
    private LocalDate endDate;

    @Schema(description = "Statut du sprint", example = "ACTIVE")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Schema(description = "Projet auquel appartient le sprint", implementation = Project.class)
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sprint_project"))
    private Project project;

    public enum Status {
        PLANNED, ACTIVE, COMPLETED
    }
}
