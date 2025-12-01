package com.taskforge.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.CreateSprintRequest;
import com.taskforge.dto.SprintResponse;
import com.taskforge.models.Sprint;
import com.taskforge.models.UserStory;
import com.taskforge.service.SprintService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/sprints")
@Tag(name = "Sprints", description = "API de gestion des sprints")
public class SprintController {
    
    @Autowired
    private SprintService sprintService;
    
    @PostMapping
    @Operation(summary = "Créer un nouveau sprint", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Sprint> createSprint(
            @RequestBody CreateSprintRequest request,
            Principal principal) {
        Sprint sprint = sprintService.createSprint(request, principal.getName());
        return ResponseEntity.ok(sprint);
    }
    
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Obtenir tous les sprints d'un projet", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des sprints récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    public ResponseEntity<List<SprintResponse>> getSprintsByProject(
            @PathVariable Long projectId,
            Principal principal) {
        List<SprintResponse> sprints = sprintService.getSprintsByProject(projectId, principal.getName());
        return ResponseEntity.ok(sprints);
    }
    
    @GetMapping("/{sprintId}")
    @Operation(summary = "Obtenir un sprint par son ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Sprint non trouvé")
    })
    public ResponseEntity<Sprint> getSprintById(
            @PathVariable Long sprintId,
            Principal principal) {
        Sprint sprint = sprintService.getSprintById(sprintId, principal.getName());
        return ResponseEntity.ok(sprint);
    }
    
    @PutMapping("/{sprintId}")
    @Operation(summary = "Mettre à jour un sprint", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Sprint non trouvé")
    })
    public ResponseEntity<Sprint> updateSprint(
            @PathVariable Long sprintId,
            @RequestBody CreateSprintRequest request,
            Principal principal) {
        Sprint sprint = sprintService.updateSprint(sprintId, request, principal.getName());
        return ResponseEntity.ok(sprint);
    }
    
    @DeleteMapping("/{sprintId}")
    @Operation(summary = "Supprimer un sprint", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sprint supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Sprint non trouvé")
    })
    public ResponseEntity<Void> deleteSprint(
            @PathVariable Long sprintId,
            Principal principal) {
        sprintService.deleteSprint(sprintId, principal.getName());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{sprintId}/user-stories/{userStoryId}")
    @Operation(summary = "Assigner une user story à un sprint", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User story assignée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Sprint ou user story non trouvé")
    })
    public ResponseEntity<UserStory> assignUserStoryToSprint(
            @PathVariable Long sprintId,
            @PathVariable Long userStoryId,
            Principal principal) {
        UserStory userStory = sprintService.assignUserStoryToSprint(userStoryId, sprintId, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    @DeleteMapping("/user-stories/{userStoryId}/sprint")
    @Operation(summary = "Retirer une user story d'un sprint (retour au backlog)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User story retirée du sprint avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "User story non trouvée")
    })
    public ResponseEntity<UserStory> removeUserStoryFromSprint(
            @PathVariable Long userStoryId,
            Principal principal) {
        UserStory userStory = sprintService.removeUserStoryFromSprint(userStoryId, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    @GetMapping("/{sprintId}/user-stories")
    @Operation(summary = "Obtenir toutes les user stories d'un sprint", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des user stories récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Sprint non trouvé")
    })
    public ResponseEntity<List<UserStory>> getUserStoriesBySprint(
            @PathVariable Long sprintId,
            Principal principal) {
        List<UserStory> userStories = sprintService.getUserStoriesBySprint(sprintId, principal.getName());
        return ResponseEntity.ok(userStories);
    }
    
    @GetMapping("/project/{projectId}/backlog")
    @Operation(summary = "Obtenir toutes les user stories du backlog (sans sprint)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Backlog récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    public ResponseEntity<List<UserStory>> getBacklogUserStories(
            @PathVariable Long projectId,
            Principal principal) {
        List<UserStory> userStories = sprintService.getBacklogUserStories(projectId, principal.getName());
        return ResponseEntity.ok(userStories);
    }
}
