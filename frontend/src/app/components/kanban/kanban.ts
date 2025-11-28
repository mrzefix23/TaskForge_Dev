import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HeaderComponent } from '../header/header';
import { UserStoryFormComponent } from './user-story-form/user-story-form';
import { TaskFormComponent } from './task-form/task-form';

interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
  members: { username: string }[];
}

interface Task {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string };
  userStory: { id: number };
}

interface UserStory {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string }[];
  tasks?: Task[];
  showTasks?: boolean;
}

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, HeaderComponent, UserStoryFormComponent, TaskFormComponent],
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

  showCreateTaskModal = false;
  showEditTaskModal = false;
  taskError: string | null = null;
  editTaskError: string | null = null;
  currentEditingTask: Task | null = null;
  currentUserStoryId: number | null = null;

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
        this.userStories = data.map(story => ({ ...story, showTasks: false, tasks: [] }));
        this.userStories.forEach(story => this.loadTasksForStory(story.id));
      },
      error: (err) => {
        this.error = (this.error ? this.error + ' ' : '') + 'Erreur lors du chargement des user stories.';
        console.error(err);
      }
    });
  }

  loadTasksForStory(userStoryId: number): void {
    const token = localStorage.getItem('token');
    this.http.get<Task[]>(`/api/tasks/user-story/${userStoryId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (tasks) => {
        const story = this.userStories.find(s => s.id === userStoryId);
        if (story) {
          story.tasks = tasks;
        }
      },
      error: (err) => {
        console.error('Erreur lors du chargement des tâches:', err);
      }
    });
  }

  getStoriesByStatus(status: 'TODO' | 'IN_PROGRESS' | 'DONE'): UserStory[] {
    return this.userStories.filter(story => story.status === status);
  }

  toggleTasks(story: UserStory, event: MouseEvent): void {
    event.stopPropagation();
    story.showTasks = !story.showTasks;
  }

  // User Story methods
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
        this.userStories.push({ ...newStory, showTasks: false, tasks: [] });
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
          this.userStories[index] = { ...updatedStory, showTasks: this.userStories[index].showTasks, tasks: this.userStories[index].tasks };
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
    
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette User Story et toutes ses tâches ?')) {
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

  // Task methods
  openCreateTaskModal(userStoryId: number, event: MouseEvent): void {
    event.stopPropagation();
    this.currentUserStoryId = userStoryId;
    this.showCreateTaskModal = true;
    this.taskError = null;
  }

  closeCreateTaskModal(): void {
    this.showCreateTaskModal = false;
    this.currentUserStoryId = null;
    this.taskError = null;
  }

  onCreateTask(formValue: any): void {
    if (!this.currentUserStoryId) return;
    
    this.taskError = null;
    const token = localStorage.getItem('token');
    const payload = {
      ...formValue,
      userStoryId: this.currentUserStoryId
    };

    this.http.post<Task>('/api/tasks', payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (newTask) => {
        const story = this.userStories.find(s => s.id === this.currentUserStoryId);
        if (story && story.tasks) {
          story.tasks.push(newTask);
        }
        this.closeCreateTaskModal();
      },
      error: (err) => {
        if (err.status === 400 && err.error?.message) {
          this.taskError = err.error.message;
        } else {
          this.taskError = 'Erreur lors de la création de la tâche.';
        }
        console.error(err);
      }
    });
  }

  openEditTaskModal(task: Task, event: MouseEvent): void {
    event.stopPropagation();
    console.log('Task à éditer:', task);
    this.currentEditingTask = task;
    this.showEditTaskModal = true;
    this.editTaskError = null;
  }

  closeEditTaskModal(): void {
    this.showEditTaskModal = false;
    this.currentEditingTask = null;
    this.editTaskError = null;
  }

  onEditTask(formValue: any): void {
    console.log('onEditTask appelé avec:', formValue);
    
    if (!this.currentEditingTask) {
      console.error('Aucune tâche en cours d\'édition');
      return;
    }

    console.log('currentEditingTask:', this.currentEditingTask);
    
    this.editTaskError = null;
    const token = localStorage.getItem('token');
    
    // Trouver la user story qui contient cette tâche
    let userStoryId: number;
    
    if (this.currentEditingTask.userStory && this.currentEditingTask.userStory.id) {
      userStoryId = this.currentEditingTask.userStory.id;
    } else {
      // Chercher la user story dans la liste
      const story = this.userStories.find(s => 
        s.tasks && s.tasks.some(t => t.id === this.currentEditingTask!.id)
      );
      
      if (!story) {
        console.error('User story non trouvée pour la tâche');
        this.editTaskError = 'Erreur: User story non trouvée';
        return;
      }
      
      userStoryId = story.id;
    }
    
    const payload = {
      ...formValue,
      userStoryId: userStoryId
    };

    console.log('Payload envoyé:', payload);

    this.http.put<Task>(`/api/tasks/${this.currentEditingTask.id}`, payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (updatedTask) => {
        console.log('Tâche mise à jour avec succès:', updatedTask);
        const story = this.userStories.find(s => s.id === userStoryId);
        if (story && story.tasks) {
          const taskIndex = story.tasks.findIndex(t => t.id === updatedTask.id);
          if (taskIndex !== -1) {
            story.tasks[taskIndex] = updatedTask;
          }
        }
        this.closeEditTaskModal();
      },
      error: (err) => {
        console.error('Erreur lors de la mise à jour:', err);
        if (err.status === 400 && err.error?.message) {
          this.editTaskError = err.error.message;
        } else {
          this.editTaskError = 'Erreur lors de la mise à jour de la tâche.';
        }
      }
    });
  }

  deleteTask(taskId: number, userStoryId: number, event: MouseEvent): void {
    event.stopPropagation();
    
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette tâche ?')) {
      return;
    }

    const token = localStorage.getItem('token');
    this.http.delete(`/api/tasks/${taskId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        const story = this.userStories.find(s => s.id === userStoryId);
        if (story && story.tasks) {
          story.tasks = story.tasks.filter(t => t.id !== taskId);
        }
      },
      error: (err) => {
        alert('Erreur lors de la suppression de la tâche.');
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
      'TODO': '📋 À Faire',
      'IN_PROGRESS': '⏳ En Cours',
      'DONE': '✅ Terminé'
    };
    return labels[status] || status;
  }

  getTaskCountLabel(story: UserStory): string {
    if (!story.tasks || story.tasks.length === 0) return 'Aucune tâche';
    return `${story.tasks.length} tâche${story.tasks.length > 1 ? 's' : ''}`;
  }
}
