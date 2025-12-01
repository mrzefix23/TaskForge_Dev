import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../header/header';
import { SprintService, Sprint, UserStory } from '../../services/sprint.service';

interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
}

@Component({
  selector: 'app-sprint-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent],
  templateUrl: './sprint-management.html',
  styleUrls: ['./sprint-management.css']
})
export class SprintManagementComponent implements OnInit {
  projectId!: number;
  project: Project | null = null;
  sprints: Sprint[] = [];
  backlogStories: UserStory[] = [];
  selectedSprint: Sprint | null = null;
  sprintStories: UserStory[] = [];
  
  // Modal states
  showCreateModal = false;
  showEditModal = false;
  showDeleteModal = false;
  sprintToDelete: Sprint | null = null;
  
  // Form data
  sprintForm = {
    name: '',
    startDate: '',
    endDate: '',
    status: 'PLANNED' as 'PLANNED' | 'ACTIVE' | 'COMPLETED'
  };
  
  error: string | null = null;
  success: string | null = null;
  currentUsername: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sprintService: SprintService
  ) {
    const username = localStorage.getItem('username');
    if (username) {
      this.currentUsername = username;
    }
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.projectId = +params['id'];
      this.loadProject();
      this.loadSprints();
      this.loadBacklog();
    });
  }

  loadProject(): void {
    const token = localStorage.getItem('token');
    fetch(`/api/projects/${this.projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(data => {
        this.project = data;
      })
      .catch(err => {
        this.error = 'Erreur lors du chargement du projet';
        console.error(err);
      });
  }

  loadSprints(): void {
    this.sprintService.getSprintsByProject(this.projectId).subscribe({
      next: (sprints) => {
        this.sprints = sprints;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des sprints';
        console.error(err);
      }
    });
  }

  loadBacklog(): void {
    this.sprintService.getBacklogUserStories(this.projectId).subscribe({
      next: (stories) => {
        this.backlogStories = stories;
      },
      error: (err) => {
        console.error('Erreur lors du chargement du backlog', err);
      }
    });
  }

  selectSprint(sprint: Sprint): void {
    this.selectedSprint = sprint;
    this.loadSprintStories(sprint.id);
  }

  loadSprintStories(sprintId: number): void {
    this.sprintService.getUserStoriesBySprint(sprintId).subscribe({
      next: (stories) => {
        this.sprintStories = stories;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des user stories du sprint', err);
      }
    });
  }

  openCreateModal(): void {
    this.resetForm();
    this.showCreateModal = true;
    this.error = null;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.resetForm();
    this.error = null;
  }

  openEditModal(sprint: Sprint): void {
    this.sprintForm = {
      name: sprint.name,
      startDate: sprint.startDate,
      endDate: sprint.endDate,
      status: sprint.status
    };
    this.selectedSprint = sprint;
    this.showEditModal = true;
    this.error = null;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedSprint = null;
    this.resetForm();
  }

  openDeleteModal(sprint: Sprint): void {
    this.sprintToDelete = sprint;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.sprintToDelete = null;
  }

  resetForm(): void {
    this.sprintForm = {
      name: '',
      startDate: '',
      endDate: '',
      status: 'PLANNED'
    };
  }

  createSprint(): void {
    if (!this.validateForm()) return;

    const request = {
      ...this.sprintForm,
      projectId: this.projectId
    };

    this.sprintService.createSprint(request).subscribe({
      next: () => {
        this.success = 'Sprint créé avec succès';
        this.loadSprints();
        this.closeCreateModal();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la création du sprint';
      }
    });
  }

  updateSprint(): void {
    if (!this.selectedSprint || !this.validateForm()) return;

    const request = {
      ...this.sprintForm,
      projectId: this.projectId
    };

    this.sprintService.updateSprint(this.selectedSprint.id, request).subscribe({
      next: () => {
        this.success = 'Sprint mis à jour avec succès';
        this.loadSprints();
        if (this.selectedSprint) {
          this.loadSprintStories(this.selectedSprint.id);
        }
        this.closeEditModal();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la mise à jour du sprint';
      }
    });
  }

  deleteSprint(): void {
    if (!this.sprintToDelete) return;

    this.sprintService.deleteSprint(this.sprintToDelete.id).subscribe({
      next: () => {
        this.success = 'Sprint supprimé avec succès';
        this.loadSprints();
        this.loadBacklog();
        if (this.selectedSprint?.id === this.sprintToDelete?.id) {
          this.selectedSprint = null;
          this.sprintStories = [];
        }
        this.closeDeleteModal();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la suppression du sprint';
        this.closeDeleteModal();
      }
    });
  }

  assignStoryToSprint(storyId: number, sprintId: number): void {
    this.sprintService.assignUserStoryToSprint(sprintId, storyId).subscribe({
      next: () => {
        this.loadBacklog();
        if (this.selectedSprint?.id === sprintId) {
          this.loadSprintStories(sprintId);
        }
        this.success = 'User story assignée au sprint';
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'assignation';
      }
    });
  }

  removeStoryFromSprint(storyId: number): void {
    this.sprintService.removeUserStoryFromSprint(storyId).subscribe({
      next: () => {
        this.loadBacklog();
        if (this.selectedSprint) {
          this.loadSprintStories(this.selectedSprint.id);
        }
        this.success = 'User story retirée du sprint';
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors du retrait';
      }
    });
  }

  validateForm(): boolean {
    if (!this.sprintForm.name || !this.sprintForm.startDate || !this.sprintForm.endDate) {
      this.error = 'Tous les champs sont obligatoires';
      return false;
    }

    if (new Date(this.sprintForm.endDate) < new Date(this.sprintForm.startDate)) {
      this.error = 'La date de fin doit être après la date de début';
      return false;
    }

    return true;
  }

  isOwner(): boolean {
    return this.project?.owner.username === this.currentUsername;
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PLANNED': return 'badge-planned';
      case 'ACTIVE': return 'badge-active';
      case 'COMPLETED': return 'badge-completed';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PLANNED': return 'Planifié';
      case 'ACTIVE': return 'Actif';
      case 'COMPLETED': return 'Terminé';
      default: return status;
    }
  }

  getPriorityBadgeClass(priority: string): string {
    switch (priority) {
      case 'HIGH': return 'badge-high';
      case 'MEDIUM': return 'badge-medium';
      case 'LOW': return 'badge-low';
      default: return '';
    }
  }

  goToKanban(): void {
    this.router.navigate(['/projects', this.projectId]);
  }
}
