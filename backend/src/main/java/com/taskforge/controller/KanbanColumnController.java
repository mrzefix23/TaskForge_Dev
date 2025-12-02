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

import com.taskforge.dto.CreateKanbanColumnRequest;
import com.taskforge.models.KanbanColumn;
import com.taskforge.service.KanbanColumnService;

/**
 * Contrôleur REST pour la gestion des colonnes Kanban.
 * Permet de créer, récupérer, mettre à jour et supprimer des colonnes personnalisées.
 */
@RestController
@RequestMapping("/api/kanban-columns")
public class KanbanColumnController {
    
    @Autowired
    private KanbanColumnService kanbanColumnService;
    
    /**
     * Crée une nouvelle colonne Kanban personnalisée.
     *
     * @param request   Les informations de la colonne.
     * @param principal L'utilisateur authentifié.
     * @return La colonne créée.
     */
    @PostMapping
    public ResponseEntity<KanbanColumn> createKanbanColumn(
            @RequestBody CreateKanbanColumnRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        KanbanColumn column = kanbanColumnService.createKanbanColumn(request, principal.getName());
        return ResponseEntity.ok(column);
    }
    
    /**
     * Récupère toutes les colonnes d'un projet.
     *
     * @param projectId L'identifiant du projet.
     * @param principal L'utilisateur authentifié.
     * @return La liste des colonnes triées par ordre.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<KanbanColumn>> getColumnsByProject(
            @PathVariable Long projectId,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        List<KanbanColumn> columns = kanbanColumnService.getColumnsByProject(projectId, principal.getName());
        return ResponseEntity.ok(columns);
    }
    
    /**
     * Met à jour une colonne Kanban existante.
     *
     * @param columnId  L'identifiant de la colonne.
     * @param request   Les nouvelles informations.
     * @param principal L'utilisateur authentifié.
     * @return La colonne mise à jour.
     */
    @PutMapping("/{columnId}")
    public ResponseEntity<KanbanColumn> updateKanbanColumn(
            @PathVariable Long columnId,
            @RequestBody CreateKanbanColumnRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        KanbanColumn column = kanbanColumnService.updateKanbanColumn(columnId, request, principal.getName());
        return ResponseEntity.ok(column);
    }
    
    /**
     * Supprime une colonne Kanban personnalisée.
     *
     * @param columnId  L'identifiant de la colonne.
     * @param principal L'utilisateur authentifié.
     * @return Une réponse vide avec le statut 204 No Content.
     */
    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteKanbanColumn(
            @PathVariable Long columnId,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        kanbanColumnService.deleteKanbanColumn(columnId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
