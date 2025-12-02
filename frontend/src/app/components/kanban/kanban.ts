import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { HeaderComponent } from '../header/header';
import { UserStoryFormComponent } from './user-story-form/user-story-form';
import { TaskFormComponent } from './task-form/task-form';
import { Project, Sprint, Task, UserStory, KanbanColumn } from '../../models/kanban.models';
import { ProjectService } from '../../services/project.service';
import { UserStoryService } from '../../services/user-story.service';
import { TaskService } from '../../services/task.service';
import { KanbanColumnService } from '../../services/kanban-column.service';
import { KanbanHelpers } from './kanban.helpers';

/**
 * Composant principal du tableau Kanban.
 * Gère l'affichage et la manipulation des user stories, tâches et colonnes Kanban.
 * Permet le drag & drop, la création/édition/suppression d'éléments et la gestion des sprints.
 */
@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterModule, HeaderComponent, UserStoryFormComponent, TaskFormComponent, DragDropModule],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css']
})
export class KanbanComponent implements OnInit {
  /** Projet actuellement affiché. */
  project: Project | null = null;
  
  /** Liste des user stories du projet. */
  userStories: UserStory[] = [];
  
  /** Liste des sprints disponibles pour le filtrage. */
  sprints: Sprint[] = [];
  
  /** Colonnes Kanban personnalisées du projet. */
  kanbanColumns: KanbanColumn[] = [];
  
  /** Filtre de sprint sélectionné ('all', 'backlog' ou ID de sprint). */
  selectedSprintFilter: string = 'all';
  
  /** État de chargement des données. */
  loading = true;
  
  /** Message d'erreur de chargement. */
  error: string | null = null;

  /** État d'affichage de la modale d'ajout de colonne. */
  showAddColumnModal = false;
  
  /** Nom de la nouvelle colonne à créer. */
  newColumnName = '';
  
  /** Ordre de la nouvelle colonne. */
  newColumnOrder = 4;
  
  /** Message d'erreur lors de la création de colonne. */
  columnError: string | null = null;

  /** État d'affichage de la modale de renommage de colonne. */
  showRenameColumnModal = false;
  
  /** ID de la colonne en cours de renommage. */
  renameColumnId: number | null = null;
  
  /** Nouveau nom pour la colonne en cours de renommage. */
  renameColumnName = '';
  
  /** Ordre de la colonne en cours de renommage. */
  renameColumnOrder = 1;
  
  /** Message d'erreur lors du renommage de colonne. */
  renameError: string | null = null;

  /** État d'affichage de la modale de création de user story. */
  showCreateStoryModal = false;
  
  /** État d'affichage de la modale d'édition de user story. */
  showEditStoryModal = false;
  
  /** Message d'erreur lors de la création de user story. */
  userStoryError: string | null = null;
  
  /** Message d'erreur lors de l'édition de user story. */
  editUserStoryError: string | null = null;
  
  /** User story actuellement en cours d'édition. */
  currentEditingStory: UserStory | null = null;

  /** État d'affichage de la modale de création de tâche. */
  showCreateTaskModal = false;
  
  /** État d'affichage de la modale d'édition de tâche. */
  showEditTaskModal = false;
  
  /** Message d'erreur lors de la création de tâche. */
  taskError: string | null = null;
  
  /** Message d'erreur lors de l'édition de tâche. */
  editTaskError: string | null = null;
  
  /** Tâche actuellement en cours d'édition. */
  currentEditingTask: Task | null = null;
  
  /** ID de la user story pour laquelle créer une tâche. */
  currentUserStoryId: number | null = null;

  /** État d'affichage de la modale de suppression de user story. */
  showDeleteStoryModal = false;
  
  /** État d'affichage de la modale de suppression de tâche. */
  showDeleteTaskModal = false;
  
  /** État d'affichage de la modale de suppression de colonne. */
  showDeleteColumnModal = false;
  
  /** User story en attente de suppression. */
  storyToDelete: UserStory | null = null;
  
  /** Tâche en attente de suppression (avec son ID de user story). */
  taskToDelete: { taskId: number, userStoryId: number } | null = null;
  
  /** Colonne en attente de suppression. */
  columnToDelete: KanbanColumn | null = null;
  
  /** Message d'erreur lors d'une suppression. */
  deleteError: string | null = null;

  /** Notification globale affichée en haut de la page. */
  notification: { message: string, type: 'success' | 'error' | 'info' } | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private userStoryService: UserStoryService,
    private taskService: TaskService,
    private kanbanColumnService: KanbanColumnService
  ) {}

  /**
   * Initialise le composant en chargeant les données du projet.
   * Récupère l'ID du projet depuis la route et charge toutes les données nécessaires.
   */
  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProjectDetails(+projectId);
      this.loadUserStories(+projectId);
      this.loadSprints(+projectId);
      this.loadKanbanColumns(+projectId);
    } else {
      this.error = "ID de projet non trouvé.";
      this.loading = false;
    }
  }

  /**
   * Charge les détails du projet.
   * @param projectId - ID du projet à charger.
   */
  loadProjectDetails(projectId: number): void {
    this.projectService.getById(projectId).subscribe({
      next: (data: Project) => {
        this.project = data;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Erreur lors du chargement du projet.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  /**
   * Charge les user stories du projet.
   * Pour chaque user story, initialise la liste des tâches et l'état d'affichage.
   * @param projectId - ID du projet.
   */
  loadUserStories(projectId: number): void {
    this.userStoryService.getByProject(projectId).subscribe({
      next: (data: UserStory[]) => {
        this.userStories = data.map((story: UserStory) => ({ ...story, showTasks: false, tasks: [] }));
        this.userStories.forEach(story => this.loadTasksForStory(story.id));
      },
      error: (err: any) => {
        this.error = (this.error ? this.error + ' ' : '') + 'Erreur lors du chargement des user stories.';
        console.error(err);
      }
    });
  }

  /**
   * Charge les tâches associées à une user story.
   * @param userStoryId - ID de la user story.
   */
  loadTasksForStory(userStoryId: number): void {
    this.taskService.getByUserStory(userStoryId).subscribe({
      next: (tasks: Task[]) => {
        const story = this.userStories.find(s => s.id === userStoryId);
        if (story) {
          story.tasks = tasks;
        }
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des tâches:', err);
      }
    });
  }

  /**
   * Charge les sprints du projet pour le filtrage.
   * @param projectId - ID du projet.
   */
  loadSprints(projectId: number): void {
    this.projectService.getSprintsByProject(projectId).subscribe({
      next: (data: Sprint[]) => {
        this.sprints = data;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des sprints:', err);
      }
    });
  }

  /**
   * Retourne les user stories filtrées selon le sprint sélectionné.
   * @returns Liste des user stories filtrées.
   */
  getFilteredUserStories(): UserStory[] {
    if (this.selectedSprintFilter === 'all') {
      return this.userStories;
    } else if (this.selectedSprintFilter === 'backlog') {
      return this.userStories.filter(story => !story.sprint);
    } else {
      const sprintId = parseInt(this.selectedSprintFilter);
      return this.userStories.filter(story => story.sprint?.id === sprintId);
    }
  }

  /**
   * Retourne les user stories filtrées par statut (colonne).
   * @param status - Statut de la colonne.
   * @returns Liste des user stories pour cette colonne.
   */
  getStoriesByStatus(status: string): UserStory[] {
    return this.getFilteredUserStories().filter(story => story.status === status);
  }

  /**
   * Charge les colonnes Kanban personnalisées du projet.
   * @param projectId - ID du projet.
   */
  loadKanbanColumns(projectId: number): void {
    this.kanbanColumnService.getByProject(projectId).subscribe({
      next: (columns: KanbanColumn[]) => {
        this.kanbanColumns = columns;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des colonnes Kanban:', err);
      }
    });
  }

  /**
   * Retourne la liste des IDs de drop lists connectées pour le drag & drop.
   * Permet de déplacer les user stories entre toutes les colonnes.
   * @returns Liste des IDs de colonnes.
   */
  getConnectedDropLists(): string[] {
    return this.kanbanColumns.map(col => `column-${col.status}`);
  }

  /**
   * Gère le dépôt d'une user story après drag & drop.
   * Met à jour le statut de la user story dans la base de données.
   * En cas d'erreur, annule le déplacement.
   * @param event - Événement de drag & drop CDK.
   * @param targetStatus - Statut de la colonne de destination.
   */
  onDrop(event: CdkDragDrop<UserStory[]>, targetStatus: string): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const story = event.previousContainer.data[event.previousIndex];
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
      
      // Update status in backend
      this.userStoryService.updateStatus(story.id, targetStatus).subscribe({
        next: (updatedStory: UserStory) => {
          const index = this.userStories.findIndex(s => s.id === updatedStory.id);
          if (index !== -1) {
            this.userStories[index] = {
              ...updatedStory,
              showTasks: this.userStories[index].showTasks,
              tasks: this.userStories[index].tasks
            };
          }
        },
        error: (err: any) => {
          console.error('Erreur lors de la mise à jour du statut:', err);
          this.showNotification('Erreur lors du déplacement de la user story.', 'error');
          // Revert the change on error
          transferArrayItem(
            event.container.data,
            event.previousContainer.data,
            event.currentIndex,
            event.previousIndex
          );
        }
      });
    }
  }

  /**
   * Change le statut d'une user story via le sélecteur de statut.
   * @param story - User story à modifier.
   * @param newStatus - Nouveau statut.
   * @param event - Événement de clic (optionnel).
   */
  changeStatus(story: UserStory, newStatus: string, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    
    this.userStoryService.updateStatus(story.id, newStatus).subscribe({
      next: (updatedStory: UserStory) => {
        const index = this.userStories.findIndex(s => s.id === updatedStory.id);
        if (index !== -1) {
          this.userStories[index] = {
            ...updatedStory,
            showTasks: this.userStories[index].showTasks,
            tasks: this.userStories[index].tasks
          };
        }
      },
      error: (err: any) => {
        console.error('Erreur lors de la mise à jour du statut:', err);
        this.showNotification('Erreur lors du changement de statut de la user story.', 'error');
      }
    });
  }

  /**
   * Navigue vers la page de gestion des sprints du projet.
   */
  goToSprintManagement(): void {
    if (this.project) {
      this.router.navigate(['/projects', this.project.id, 'sprints']);
    }
  }

  /**
   * Navigue vers la page de gestion des versions du projet.
   */
  goToVersionManagement(): void {
    if (this.project && this.project.id) {
      this.router.navigate(['/projects', this.project.id, 'versions']);
    }
  }

  /**
   * Bascule l'affichage des tâches d'une user story.
   * @param story - User story concernée.
   * @param event - Événement de clic.
   */
  toggleTasks(story: UserStory, event: MouseEvent): void {
    event.stopPropagation();
    story.showTasks = !story.showTasks;
  }

  // ========== User Story Modal Methods ==========

  /**
   * Ouvre la modale de création de user story.
   */
  openCreateStoryModal(): void {
    this.showCreateStoryModal = true;
    this.userStoryError = null;
  }

  /**
   * Ferme la modale de création de user story.
   */
  closeCreateStoryModal(): void {
    this.showCreateStoryModal = false;
    this.userStoryError = null;
  }

  /**
   * Crée une nouvelle user story.
   * Affiche une notification de succès ou d'erreur.
   * @param formValue - Données du formulaire.
   */
  onCreateUserStory(formValue: any): void {
    if (!this.project) return;

    this.userStoryError = null;
    const payload = {
      ...formValue,
      projectId: this.project.id,
      status: 'TODO'
    };

    this.userStoryService.create(payload).subscribe({
      next: (newStory: UserStory) => {
        this.userStories.push({ ...newStory, showTasks: false, tasks: [] });
        this.showNotification('User story créée avec succès.', 'success');
        this.closeCreateStoryModal();
      },
      error: (err: any) => {
        if (err.status === 400 && err.error?.message) {
          this.userStoryError = err.error.message;
        } else {
          this.userStoryError = 'Erreur lors de la création de la user story.';
        }
        console.error(err);
      }
    });
  }

  /**
   * Ouvre la modale d'édition pour une user story.
   * @param story - User story à éditer.
   * @param event - Événement de clic (optionnel).
   */
  openEditStoryModal(story: UserStory, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.currentEditingStory = story;
    this.showEditStoryModal = true;
    this.editUserStoryError = null;
  }

  /**
   * Ferme la modale d'édition de user story.
   */
  closeEditStoryModal(): void {
    this.showEditStoryModal = false;
    this.currentEditingStory = null;
    this.editUserStoryError = null;
  }

  /**
   * Met à jour une user story existante.
   * Affiche une notification de succès ou d'erreur.
   * @param formValue - Données du formulaire.
   */
  onEditUserStory(formValue: any): void {
    if (!this.currentEditingStory) return;

    this.editUserStoryError = null;

    this.userStoryService.update(this.currentEditingStory.id, formValue).subscribe({
      next: (updatedStory: UserStory) => {
        const index = this.userStories.findIndex(s => s.id === updatedStory.id);
        if (index !== -1) {
          this.userStories[index] = { 
            ...updatedStory, 
            showTasks: this.userStories[index].showTasks, 
            tasks: this.userStories[index].tasks 
          };
        }
        this.showNotification('User story mise à jour avec succès.', 'success');
        this.closeEditStoryModal();
      },
      error: (err: any) => {
        if (err.status === 400 && err.error?.message) {
          this.editUserStoryError = err.error.message;
        } else {
          this.editUserStoryError = 'Erreur lors de la mise à jour de la user story.';
        }
        console.error(err);
      }
    });
  }

  /**
   * Prépare la suppression d'une user story (ouvre la modale de confirmation).
   * @param storyId - ID de la user story à supprimer.
   * @param event - Événement de clic.
   */
  deleteUserStory(storyId: number, event: MouseEvent): void {
    event.stopPropagation();
    const story = this.userStories.find(s => s.id === storyId);
    if (story) {
      this.storyToDelete = story;
      this.showDeleteStoryModal = true;
      this.deleteError = null;
    }
  }

  /**
   * Confirme et exécute la suppression d'une user story.
   * Affiche une notification de succès ou d'erreur.
   */
  confirmDeleteUserStory(): void {
    if (!this.storyToDelete) return;

    this.userStoryService.delete(this.storyToDelete.id).subscribe({
      next: () => {
        this.userStories = this.userStories.filter(s => s.id !== this.storyToDelete!.id);
        this.showNotification('User story supprimée avec succès.', 'success');
        this.closeDeleteStoryModal();
      },
      error: (err: any) => {
        this.deleteError = 'Erreur lors de la suppression de la User Story.';
        console.error(err);
      }
    });
  }

  /**
   * Ferme la modale de suppression de user story.
   */
  closeDeleteStoryModal(): void {
    this.showDeleteStoryModal = false;
    this.storyToDelete = null;
    this.deleteError = null;
  }

  // ========== Task Modal Methods ==========

  /**
   * Ouvre la modale de création de tâche pour une user story.
   * @param userStoryId - ID de la user story parente.
   * @param event - Événement de clic.
   */
  openCreateTaskModal(userStoryId: number, event: MouseEvent): void {
    event.stopPropagation();
    this.currentUserStoryId = userStoryId;
    this.showCreateTaskModal = true;
    this.taskError = null;
  }

  /**
   * Ferme la modale de création de tâche.
   */
  closeCreateTaskModal(): void {
    this.showCreateTaskModal = false;
    this.currentUserStoryId = null;
    this.taskError = null;
  }

  /**
   * Crée une nouvelle tâche pour une user story.
   * Affiche une notification de succès ou d'erreur.
   * @param formValue - Données du formulaire.
   */
  onCreateTask(formValue: any): void {
    if (!this.currentUserStoryId) return;

    this.taskError = null;
    const payload = {
      ...formValue,
      userStoryId: this.currentUserStoryId
    };

    this.taskService.create(payload).subscribe({
      next: (newTask: Task) => {
        const story = this.userStories.find(s => s.id === this.currentUserStoryId);
        if (story && story.tasks) {
          story.tasks.push(newTask);
        }
        this.showNotification('Tâche créée avec succès.', 'success');
        this.closeCreateTaskModal();
      },
      error: (err: any) => {
        if (err.status === 400 && err.error?.message) {
          this.taskError = err.error.message;
        } else {
          this.taskError = 'Erreur lors de la création de la tâche.';
        }
        console.error(err);
      }
    });
  }

  /**
   * Ouvre la modale d'édition pour une tâche.
   * @param task - Tâche à éditer.
   * @param event - Événement de clic.
   */
  openEditTaskModal(task: Task, event: MouseEvent): void {
    event.stopPropagation();
    console.log('Task à éditer:', task);
    this.currentEditingTask = task;
    this.showEditTaskModal = true;
    this.editTaskError = null;
  }

  /**
   * Ferme la modale d'édition de tâche.
   */
  closeEditTaskModal(): void {
    this.showEditTaskModal = false;
    this.currentEditingTask = null;
    this.editTaskError = null;
  }

  /**
   * Met à jour une tâche existante.
   * Affiche une notification de succès ou d'erreur.
   * @param formValue - Données du formulaire.
   */
  onEditTask(formValue: any): void {
    console.log('onEditTask appelé avec:', formValue);

    if (!this.currentEditingTask) {
      console.error('Aucune tâche en cours d\'édition');
      return;
    }

    console.log('currentEditingTask:', this.currentEditingTask);

    this.editTaskError = null;
    
    // Trouver la user story qui contient cette tâche
    const userStoryId = this.currentEditingTask.userStory?.id || 
      this.userStories.find(s => s.tasks?.some(t => t.id === this.currentEditingTask!.id))?.id;
    
    if (!userStoryId) {
      console.error('User story non trouvée pour la tâche');
      this.editTaskError = 'Erreur: User story non trouvée';
      return;
    }

    const payload = {
      ...formValue,
      userStoryId: userStoryId
    };

    this.taskService.update(this.currentEditingTask.id, payload).subscribe({
      next: (updatedTask: Task) => {
        const story = this.userStories.find(s => s.id === userStoryId);
        if (story && story.tasks) {
          const taskIndex = story.tasks.findIndex(t => t.id === updatedTask.id);
          if (taskIndex !== -1) {
            story.tasks[taskIndex] = updatedTask;
          }
        }
        this.showNotification('Tâche mise à jour avec succès.', 'success');
        this.closeEditTaskModal();
      },
      error: (err: any) => {
        console.error('Erreur lors de la mise à jour:', err);
        if (err.status === 400 && err.error?.message) {
          this.editTaskError = err.error.message;
        } else {
          this.editTaskError = 'Erreur lors de la mise à jour de la tâche.';
        }
      }
    });
  }

  /**
   * Prépare la suppression d'une tâche (ouvre la modale de confirmation).
   * @param taskId - ID de la tâche à supprimer.
   * @param userStoryId - ID de la user story parente.
   * @param event - Événement de clic.
   */
  deleteTask(taskId: number, userStoryId: number, event: MouseEvent): void {
    event.stopPropagation();
    this.taskToDelete = { taskId, userStoryId };
    this.showDeleteTaskModal = true;
    this.deleteError = null;
  }

  /**
   * Confirme et exécute la suppression d'une tâche.
   * Affiche une notification de succès ou d'erreur.
   */
  confirmDeleteTask(): void {
    if (!this.taskToDelete) return;

    this.taskService.delete(this.taskToDelete.taskId).subscribe({
      next: () => {
        const story = this.userStories.find(s => s.id === this.taskToDelete!.userStoryId);
        if (story && story.tasks) {
          story.tasks = story.tasks.filter(t => t.id !== this.taskToDelete!.taskId);
        }
        this.showNotification('Tâche supprimée avec succès.', 'success');
        this.closeDeleteTaskModal();
      },
      error: (err: any) => {
        this.deleteError = 'Erreur lors de la suppression de la tâche.';
        console.error(err);
      }
    });
  }

  /**
   * Ferme la modale de suppression de tâche.
   */
  closeDeleteTaskModal(): void {
    this.showDeleteTaskModal = false;
    this.taskToDelete = null;
    this.deleteError = null;
  }

  // ========== Notification Methods ==========

  /**
   * Affiche une notification globale en haut de la page.
   * La notification disparaît automatiquement après 5 secondes.
   * @param message - Message à afficher.
   * @param type - Type de notification (success, error, info).
   */
  showNotification(message: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.notification = { message, type };
    setTimeout(() => {
      this.notification = null;
    }, 5000); // Masquer après 5 secondes
  }

  /**
   * Ferme manuellement la notification globale.
   */
  closeNotification(): void {
    this.notification = null;
  }

  // ========== Helper Methods ==========

  /**
   * Retourne le libellé traduit d'une priorité.
   * @param priority - Code de priorité.
   * @returns Libellé de la priorité.
   */
  getPriorityLabel(priority: string): string {
    return KanbanHelpers.getPriorityLabel(priority);
  }

  /**
   * Retourne le libellé traduit d'un statut.
   * @param status - Code de statut.
   * @returns Libellé du statut.
   */
  getStatusLabel(status: string): string {
    return KanbanHelpers.getStatusLabel(status);
  }

  /**
   * Retourne le libellé du nombre de tâches d'une user story.
   * @param story - User story concernée.
   * @returns Libellé (ex: "3 tâches").
   */
  getTaskCountLabel(story: UserStory): string {
    return KanbanHelpers.getTaskCountLabel(story.tasks?.length || 0);
  }

  // ========== Column Management Methods ==========

  /**
   * Ouvre la modale d'ajout de colonne Kanban.
   */
  openAddColumnModal(): void {
    this.showAddColumnModal = true;
    this.newColumnName = '';
    this.newColumnOrder = this.kanbanColumns.length + 1;
    this.columnError = null;
  }

  /**
   * Ferme la modale d'ajout de colonne.
   */
  closeAddColumnModal(): void {
    this.showAddColumnModal = false;
    this.newColumnName = '';
    this.columnError = null;
  }

  /**
   * Crée une nouvelle colonne Kanban personnalisée.
   * Le statut est généré automatiquement à partir du nom.
   * Affiche une notification de succès ou d'erreur.
   */
  addColumn(): void {
    if (!this.project || !this.newColumnName.trim()) {
      this.columnError = 'Le nom de la colonne est requis.';
      return;
    }

    const status = this.newColumnName.toUpperCase().replace(/\s+/g, '_');
    
    const newColumn: Partial<KanbanColumn> = {
      name: this.newColumnName,
      status: status,
      order: this.newColumnOrder,
      projectId: this.project.id
    };

    this.kanbanColumnService.create(newColumn).subscribe({
      next: (column: KanbanColumn) => {
        this.kanbanColumns.push(column);
        this.kanbanColumns.sort((a, b) => a.order - b.order);
        this.showNotification(`Colonne "${column.name}" créée avec succès.`, 'success');
        this.closeAddColumnModal();
      },
      error: (err: any) => {
        this.columnError = err.error?.message || 'Erreur lors de la création de la colonne.';
        console.error(err);
      }
    });
  }

  /**
   * Prépare la suppression d'une colonne (ouvre la modale de confirmation).
   * Les colonnes par défaut ne peuvent pas être supprimées.
   * @param column - Colonne à supprimer.
   * @param event - Événement de clic.
   */
  deleteColumn(column: KanbanColumn, event: MouseEvent): void{
    event.stopPropagation();
    
    if (column.isDefault) {
      this.showNotification('Les colonnes par défaut ne peuvent pas être supprimées.', 'error');
      return;
    }

    this.columnToDelete = column;
    this.showDeleteColumnModal = true;
    this.deleteError = null;
  }

  /**
   * Confirme et exécute la suppression d'une colonne.
   * Affiche une notification de succès ou une erreur si la colonne n'est pas vide.
   */
  confirmDeleteColumn(): void {
    if (!this.columnToDelete) return;

    this.kanbanColumnService.delete(this.columnToDelete.id).subscribe({
      next: () => {
        this.kanbanColumns = this.kanbanColumns.filter(c => c.id !== this.columnToDelete!.id);
        this.showNotification(`Colonne "${this.columnToDelete!.name}" supprimée avec succès.`, 'success');
        this.closeDeleteColumnModal();
      },
      error: (err: any) => {
        this.deleteError = 'Erreur: vous ne pouvez pas supprimer une colonne non vide.';
        console.error(err);
      }
    });
  }

  /**
   * Ferme la modale de suppression de colonne.
   */
  closeDeleteColumnModal(): void {
    this.showDeleteColumnModal = false;
    this.columnToDelete = null;
    this.deleteError = null;
  }

  /**
   * Ouvre la modale de renommage pour une colonne.
   * @param column - Colonne à renommer.
   * @param event - Événement de clic.
   */
  openRenameColumnModal(column: KanbanColumn, event: MouseEvent): void {
    event.stopPropagation();
    this.renameColumnId = column.id;
    this.renameColumnName = column.name;
    this.renameColumnOrder = column.order;
    this.showRenameColumnModal = true;
    this.renameError = null;
  }

  /**
   * Ferme la modale de renommage de colonne.
   */
  closeRenameColumnModal(): void {
    this.showRenameColumnModal = false;
    this.renameColumnId = null;
    this.renameColumnName = '';
    this.renameError = null;
  }

  /**
   * Renomme une colonne Kanban existante.
   * Affiche une notification de succès ou d'erreur.
   */
  renameColumn(): void {
    if (!this.renameColumnId || !this.renameColumnName.trim()) {
      this.renameError = 'Le nom de la colonne est requis.';
      return;
    }

    const column = this.kanbanColumns.find(c => c.id === this.renameColumnId);
    if (!column) {
      this.renameError = 'Colonne non trouvée.';
      return;
    }

    const updatedColumn: Partial<KanbanColumn> = {
      name: this.renameColumnName,
      status: column.status,
      order: this.renameColumnOrder,
      projectId: column.projectId
    };

    this.kanbanColumnService.update(this.renameColumnId, updatedColumn).subscribe({
      next: (updated: KanbanColumn) => {
        const index = this.kanbanColumns.findIndex(c => c.id === this.renameColumnId);
        if (index !== -1) {
          this.kanbanColumns[index] = updated;
        }
        this.showNotification(`Colonne renommée en "${updated.name}" avec succès.`, 'success');
        this.closeRenameColumnModal();
      },
      error: (err: any) => {
        this.renameError = err.error?.message || 'Erreur lors du renommage de la colonne.';
        console.error(err);
      }
    });
  }
}