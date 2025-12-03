package com.taskforge.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.dto.CreateKanbanColumnRequest;
import com.taskforge.models.KanbanColumn;
import com.taskforge.models.Project;
import com.taskforge.repositories.KanbanColumnRepository;

/**
 * Service gérant la logique métier liée aux colonnes Kanban.
 * Permet de créer, récupérer, mettre à jour et supprimer des colonnes personnalisées.
 */
@Service
public class KanbanColumnService {
    
    @Autowired
    private KanbanColumnRepository kanbanColumnRepository;
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * Crée une nouvelle colonne Kanban personnalisée.
     *
     * @param request  Les détails de la colonne à créer.
     * @param username Le nom d'utilisateur effectuant la création.
     * @return La colonne Kanban créée.
     */
    @Transactional
    public KanbanColumn createKanbanColumn(CreateKanbanColumnRequest request, String username) {
        // Vérifier l'accès au projet
        Project project = projectService.getProjectById(request.getProjectId(), username);
        
        // Vérifier que le statut n'existe pas déjà
        if (kanbanColumnRepository.existsByStatusAndProjectId(request.getStatus(), project.getId())) {
            throw new RuntimeException("Une colonne avec ce statut existe déjà pour ce projet");
        }
        
        KanbanColumn column = KanbanColumn.builder()
                .name(request.getName())
                .status(request.getStatus().toUpperCase().replace(" ", "_"))
                .order(request.getOrder())
                .project(project)
                .isDefault(false)
                .build();
        
        return kanbanColumnRepository.save(column);
    }
    
    /**
     * Récupère toutes les colonnes d'un projet, triées par ordre.
     *
     * @param projectId L'identifiant du projet.
     * @param username  Le nom d'utilisateur effectuant la requête.
     * @return La liste des colonnes Kanban.
     */
    public List<KanbanColumn> getColumnsByProject(Long projectId, String username) {
        // Vérifier l'accès au projet
        projectService.getProjectById(projectId, username);
        
        return kanbanColumnRepository.findByProjectIdOrderByOrderAsc(projectId);
    }
    
    /**
     * Met à jour une colonne Kanban existante.
     *
     * @param columnId L'identifiant de la colonne.
     * @param request  Les nouvelles informations de la colonne.
     * @param username Le nom d'utilisateur effectuant la modification.
     * @return La colonne mise à jour.
     */
    @Transactional
    public KanbanColumn updateKanbanColumn(Long columnId, CreateKanbanColumnRequest request, String username) {
        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Colonne Kanban non trouvée"));
        
        // Vérifier l'accès au projet
        projectService.getProjectById(column.getProject().getId(), username);
        
        // Toujours permettre la modification du nom
        column.setName(request.getName());
        
        // Pour les colonnes par défaut, ne pas modifier le statut ni l'ordre
        if (!column.getIsDefault()) {
            column.setOrder(request.getOrder());
            // Le statut n'est pas modifié car il sert de clé
        }
        
        return kanbanColumnRepository.save(column);
    }
    
    /**
     * Supprime une colonne Kanban personnalisée.
     *
     * @param columnId L'identifiant de la colonne.
     * @param username Le nom d'utilisateur effectuant la suppression.
     */
    @Transactional
    public void deleteKanbanColumn(Long columnId, String username) {
        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Colonne Kanban non trouvée"));
        
        // Vérifier l'accès au projet
        projectService.getProjectById(column.getProject().getId(), username);
        
        // Ne pas permettre la suppression des colonnes par défaut
        if (column.getIsDefault()) {
            throw new RuntimeException("Les colonnes par défaut ne peuvent pas être supprimées");
        }
        
        kanbanColumnRepository.delete(column);
    }
    
    /**
     * Initialise les colonnes par défaut pour un nouveau projet.
     *
     * @param project Le projet pour lequel créer les colonnes par défaut.
     */
    @Transactional
    public void initializeDefaultColumns(Project project) {
        // Créer les trois colonnes par défaut : TODO, IN_PROGRESS, DONE
        KanbanColumn todoColumn = KanbanColumn.builder()
                .name("À faire")
                .status("TODO")
                .order(1)
                .project(project)
                .isDefault(true)
                .build();
        
        KanbanColumn inProgressColumn = KanbanColumn.builder()
                .name("En cours")
                .status("IN_PROGRESS")
                .order(2)
                .project(project)
                .isDefault(true)
                .build();
        
        KanbanColumn doneColumn = KanbanColumn.builder()
                .name("Terminé")
                .status("DONE")
                .order(3)
                .project(project)
                .isDefault(true)
                .build();
        
        kanbanColumnRepository.save(todoColumn);
        kanbanColumnRepository.save(inProgressColumn);
        kanbanColumnRepository.save(doneColumn);
    }
}
