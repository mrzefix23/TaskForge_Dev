package com.taskforge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.CreateVersionRequest;
import com.taskforge.models.UserStory;
import com.taskforge.models.Version;
import com.taskforge.service.VersionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Versions", description = "Gestion des versions de livraison")
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Obtenir toutes les versions d'un projet")
    public ResponseEntity<List<Version>> getVersionsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(versionService.getVersionsByProject(projectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une version par son ID")
    public ResponseEntity<Version> getVersionById(@PathVariable Long id) {
        return ResponseEntity.ok(versionService.getVersionById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle version")
    public ResponseEntity<Version> createVersion(@RequestBody CreateVersionRequest request) {
        Version created = versionService.createVersion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une version")
    public ResponseEntity<Version> updateVersion(@PathVariable Long id, @RequestBody CreateVersionRequest request) {
        return ResponseEntity.ok(versionService.updateVersion(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une version")
    public ResponseEntity<Version> updateVersionStatus(
            @PathVariable Long id,
            @RequestParam Version.VersionStatus status) {
        return ResponseEntity.ok(versionService.updateVersionStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une version")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id) {
        versionService.deleteVersion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{versionId}/user-stories/{userStoryId}")
    @Operation(summary = "Associer une User Story à une version")
    public ResponseEntity<UserStory> assignUserStoryToVersion(
            @PathVariable Long versionId,
            @PathVariable Long userStoryId) {
        return ResponseEntity.ok(versionService.assignUserStoryToVersion(versionId, userStoryId));
    }

    @DeleteMapping("/{versionId}/user-stories/{userStoryId}")
    @Operation(summary = "Retirer une User Story d'une version")
    public ResponseEntity<UserStory> removeUserStoryFromVersion(
            @PathVariable Long versionId,
            @PathVariable Long userStoryId) {
        return ResponseEntity.ok(versionService.removeUserStoryFromVersion(userStoryId));
    }

    @GetMapping("/{versionId}/user-stories")
    @Operation(summary = "Obtenir les User Stories d'une version")
    public ResponseEntity<List<UserStory>> getUserStoriesByVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(versionService.getUserStoriesByVersion(versionId));
    }
}