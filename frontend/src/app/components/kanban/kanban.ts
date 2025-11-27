import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HeaderComponent } from '../header/header';
import { UserStoryFormComponent } from './user-story-form/user-story-form';

interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
  members: { username: string }[];
  projectId: number;
}

interface UserStory {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string }[];
}

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, HeaderComponent, UserStoryFormComponent],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css']
})
export class KanbanComponent implements OnInit {
  project: Project | null = null;
  userStories: UserStory[] = [];
  loading = true;
  error: string | null = null;
  
  showCreateStoryModal = false;
  showEditStoryModal = false;
  userStoryError: string | null = null;
  editUserStoryError: string | null = null;
  currentEditingStory: UserStory | null = null;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProjectDetails(+projectId);
      this.loadUserStories(+projectId);
    } else {
      this.error = "ID de projet non trouvé.";
      this.loading = false;
    }
  }

  loadProjectDetails(projectId: number): void {
    const token = localStorage.getItem('token');
    this.http.get<Project>(`/api/projects/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.project = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du projet.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  loadUserStories(projectId: number): void {
    const token = localStorage.getItem('token');
    this.http.get<UserStory[]>(`/api/user-stories/project/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.userStories = data;
      },
      error: (err) => {
        this.error = (this.error ? this.error + ' ' : '') + 'Erreur lors du chargement des user stories.';
        console.error(err);
      }
    });
  }

  getStoriesByStatus(status: 'TODO' | 'IN_PROGRESS' | 'DONE'): UserStory[] {
    return this.userStories.filter(story => story.status === status);
  }

  openCreateStoryModal(): void {
    this.showCreateStoryModal = true;
    this.userStoryError = null;
  }

  closeCreateStoryModal(): void {
    this.showCreateStoryModal = false;
    this.userStoryError = null;
  }

  onCreateUserStory(formValue: any): void {
    if (!this.project) return;
    
    this.userStoryError = null;
    const token = localStorage.getItem('token');
    const payload = {
      ...formValue,
      projectId: this.project.id,
      status: 'TODO'
    };

    this.http.post<UserStory>('/api/user-stories', payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (newStory) => {
        this.userStories.push(newStory);
        this.closeCreateStoryModal();
      },
      error: (err) => {
        if (err.status === 400 && err.error?.message) {
          this.userStoryError = err.error.message;
        } else {
          this.userStoryError = 'Erreur lors de la création de la user story.';
        }
        console.error(err);
      }
    });
  }

  openEditStoryModal(story: UserStory, event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.currentEditingStory = story;
    this.showEditStoryModal = true;
    this.editUserStoryError = null;
  }

  closeEditStoryModal(): void {
    this.showEditStoryModal = false;
    this.currentEditingStory = null;
    this.editUserStoryError = null;
  }

  onEditUserStory(formValue: any): void {
    if (!this.currentEditingStory) return;
    
    this.editUserStoryError = null;
    const token = localStorage.getItem('token');
    const payload = {
      ...formValue,
    };

    this.http.put<UserStory>(`/api/user-stories/${this.currentEditingStory.id}`, payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (updatedStory) => {
        const index = this.userStories.findIndex(s => s.id === updatedStory.id);
        if (index !== -1) {
          this.userStories[index] = updatedStory;
        }
        this.closeEditStoryModal();
      },
      error: (err) => {
        if (err.status === 400 && err.error?.message) {
          this.editUserStoryError = err.error.message;
        } else {
          this.editUserStoryError = 'Erreur lors de la mise à jour de la user story.';
        }
        console.error(err);
      }
    });
  }

  deleteUserStory(storyId: number, event: MouseEvent): void {
    event.stopPropagation();
    
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette User Story ?')) {
      return;
    }

    const token = localStorage.getItem('token');
    this.http.delete(`/api/user-stories/${storyId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.userStories = this.userStories.filter(s => s.id !== storyId);
      },
      error: (err) => {
        alert('Erreur lors de la suppression de la User Story.');
        console.error(err);
      }
    });
  }

  getPriorityLabel(priority: string): string {
    const labels: { [key: string]: string } = {
      'LOW': 'Basse',
      'MEDIUM': 'Moyenne',
      'HIGH': 'Haute'
    };
    return labels[priority] || priority;
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'TODO': 'À Faire',
      'IN_PROGRESS': 'En Cours',
      'DONE': 'Terminé'
    };
    return labels[status] || status;
  }
}
