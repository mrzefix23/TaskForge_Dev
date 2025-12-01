package com.taskforge.dto;

import com.taskforge.models.Sprint.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSprintRequest {
    @Schema(description = "Nom du sprint", example = "Sprint 1")
    private String name;
    
    @Schema(description = "Date de début du sprint", example = "2025-12-01")
    private LocalDate startDate;
    
    @Schema(description = "Date de fin du sprint", example = "2025-12-15")
    private LocalDate endDate;
    
    @Schema(description = "Statut du sprint", example = "PLANNED")
    private Status status;
    
    @Schema(description = "ID du projet", example = "1")
    private Long projectId;
}
