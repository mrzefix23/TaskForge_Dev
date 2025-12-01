package com.taskforge.dto;

import com.taskforge.models.Sprint.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintResponse {
    @Schema(description = "ID du sprint", example = "1")
    private Long id;
    
    @Schema(description = "Nom du sprint", example = "Sprint 1")
    private String name;
    
    @Schema(description = "Date de début du sprint", example = "2025-12-01")
    private LocalDate startDate;
    
    @Schema(description = "Date de fin du sprint", example = "2025-12-15")
    private LocalDate endDate;
    
    @Schema(description = "Statut du sprint", example = "ACTIVE")
    private Status status;
    
    @Schema(description = "ID du projet", example = "1")
    private Long projectId;
}
