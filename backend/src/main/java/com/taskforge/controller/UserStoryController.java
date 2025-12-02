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

import com.taskforge.dto.CreateUserStoryRequest;
import com.taskforge.dto.UpdateUserStoryStatusRequest;
import com.taskforge.models.UserStory;
import com.taskforge.service.UserStoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Contrôleur REST pour la gestion des User Stories.
 * Fournit des points de terminaison pour créer, récupérer, mettre à jour et supprimer des User Stories
 * liées aux projets.
 */
@RestController
@RequestMapping("/api/user-stories")
@Tag(name = "User Story Management", description = "API de gestion des User Stories liées aux projets")
@SecurityRequirement(name = "bearerAuth")
public class UserStoryController {
    
    @Autowired
    private UserStoryService userStoryService;
    
    /**
     * Crée une nouvelle User Story.
     *
     * @param request   Les informations nécessaires à la création de la User Story.
     * @param principal L'utilisateur authentifié effectuant la requête.
     * @return La User Story créée avec le statut 200 OK, ou 403 Forbidden si non authentifié.
     */
    @Operation(summary = "Créer une nouvelle User Story")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User Story créée avec succès"),
        @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @PostMapping
    public ResponseEntity<UserStory> createUserStory(
            @RequestBody CreateUserStoryRequest request, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.createUserStory(request, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    /**
     * Récupère la liste des User Stories associées à un projet spécifique.
     *
     * @param projectId L'identifiant du projet.
     * @param principal L'utilisateur authentifié.
     * @return Une liste de User Stories appartenant au projet.
     */
    @Operation(summary = "Récupérer les User Stories d'un projet")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User Stories récupérées avec succès"),
        @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<UserStory>> getUserStoriesByProject(
            @PathVariable Long projectId, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        List<UserStory> userStories = userStoryService.getUserStoriesByProject(projectId, principal.getName());
        return ResponseEntity.ok(userStories);
    }
    
    /**
     * Récupère une User Story spécifique par son identifiant.
     *
     * @param userStoryId L'identifiant de la User Story.
     * @param principal   L'utilisateur authentifié.
     * @return La User Story demandée.
     */
    @Operation(summary = "Récupérer une User Story par son identifiant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User Story récupérée avec succès"),
        @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @GetMapping("/{userStoryId}")
    public ResponseEntity<UserStory> getUserStoryById(
            @PathVariable Long userStoryId, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.getUserStoryById(userStoryId, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    /**
     * Met à jour une User Story existante.
     *
     * @param userStoryId L'identifiant de la User Story à modifier.
     * @param request     Les nouvelles informations de la User Story.
     * @param principal   L'utilisateur authentifié.
     * @return La User Story mise à jour.
     */
    @Operation(summary = "Mettre à jour une User Story existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User Story mise à jour avec succès"),
        @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @PutMapping("/{userStoryId}")
    public ResponseEntity<UserStory> updateUserStory(
            @PathVariable Long userStoryId,
            @RequestBody CreateUserStoryRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.updateUserStory(userStoryId, request, principal.getName());
        return ResponseEntity.ok(userStory);
    }
    
    /**
     * Supprime une User Story.
     *
     * @param userStoryId L'identifiant de la User Story à supprimer.
     * @param principal   L'utilisateur authentifié.
     * @return Une réponse vide avec le statut 204 No Content en cas de succès.
     */
    @Operation(summary = "Supprimer une User Story")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User Story supprimée avec succès"),
        @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @DeleteMapping("/{userStoryId}")
    public ResponseEntity<Void> deleteUserStory(
            @PathVariable Long userStoryId, 
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        userStoryService.deleteUserStory(userStoryId, principal.getName());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Met à jour uniquement le statut d'une User Story (drag & drop).
     *
     * @param userStoryId L'identifiant de la User Story.
     * @param request     Le nouveau statut.
     * @param principal   L'utilisateur authentifié.
     * @return La User Story avec le statut mis à jour.
     */
    @Operation(summary = "Mettre à jour le statut d'une User Story")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut de la User Story mis à jour avec succès"),
        @ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @PutMapping("/{userStoryId}/status")
    public ResponseEntity<UserStory> updateUserStoryStatus(
            @PathVariable Long userStoryId,
            @RequestBody UpdateUserStoryStatusRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        UserStory userStory = userStoryService.updateUserStoryStatus(userStoryId, request.getStatus(), principal.getName());
        return ResponseEntity.ok(userStory);
    }
}