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

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.models.Project;
import com.taskforge.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projets", description = "API pour la gestion des projets")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Operation(summary = "Créer un nouveau projet", description="Crée un nouveau projet pour l'utilisateur authentifié.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projet créé avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest createProjectRequest, Principal principal) {
        if(principal == null || !createProjectRequest.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Project project = projectService.createProject(createProjectRequest);
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Mettre à jour un projet existant", description="Met à jour un projet existant pour l'utilisateur authentifié.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projet mis à jour avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable Long projectId, @RequestBody CreateProjectRequest updateRequest, Principal principal){
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        Project updatedProject = projectService.updateProject(projectId, principal.getName(), updateRequest);
        
        return ResponseEntity.ok(updatedProject);

    }

    @Operation(summary = "Récupérer un projet par son ID", description="Récupère un projet spécifique pour l'utilisateur authentifié.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projet récupéré avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long projectId, Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        Project project = projectService.getProjectById(projectId, principal.getName());
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Récupérer tous les projets de l'utilisateur", description="Récupère tous les projets associés à l'utilisateur authentifié.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projets récupérés avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/myprojects")
    public ResponseEntity<List<Project>> getMyProjects(Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        List<Project> projects = projectService.getProjectsByUsername(principal.getName());
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Supprimer un projet", description="Supprime un projet spécifique pour l'utilisateur authentifié.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Projet supprimé avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId, Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        projectService.deleteProject(projectId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}