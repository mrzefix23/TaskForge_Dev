import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../header/header';
import { UserStoryFormComponent } from './user-story-form/user-story-form';
import { TaskFormComponent } from './task-form/task-form';
import { Project, Sprint, Task, UserStory } from '../../models/kanban.models';
import { ProjectService } from '../../services/project.service';
import { UserStoryService } from '../../services/user-story.service';
import { TaskService } from '../../services/task.service';
import { KanbanHelpers } from './kanban.helpers';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterModule, HeaderComponent, UserStoryFormComponent, TaskFormComponent],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css']
})
export class KanbanComponent implements OnInit {
  project: Project | null = null;
  userStories: UserStory[] = [];
  sprints: Sprint[] = [];
  selectedSprintFilter: string = 'all'; // 'all', 'backlog', or sprint ID
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

  // Modal states for delete confirmations
  showDeleteStoryModal = false;
  showDeleteTaskModal = false;
  storyToDelete: UserStory | null = null;
  taskToDelete: { taskId: number, userStoryId: number } | null = null;
  deleteError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private userStoryService: UserStoryService,
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProjectDetails(+projectId);
      this.loadUserStories(+projectId);
      this.loadSprints(+projectId);
    } else {
      this.error = "ID de projet non trouvé.";
      this.loading = false;
    }
  }

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

  getStoriesByStatus(status: 'TODO' | 'IN_PROGRESS' | 'DONE'): UserStory[] {
    return this.getFilteredUserStories().filter(story => story.status === status);
  }

  goToSprintManagement(): void {
    if (this.project) {
      this.router.navigate(['/projects', this.project.id, 'sprints']);
    }
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
    const payload = {
      ...formValue,
      projectId: this.project.id,
      status: 'TODO'
    };

    this.userStoryService.create(payload).subscribe({
      next: (newStory: UserStory) => {
        this.userStories.push({ ...newStory, showTasks: false, tasks: [] });
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

    this.userStoryService.update(this.currentEditingStory.id, formValue).subscribe({
      next: (updatedStory: UserStory) => {
        const index = this.userStories.findIndex(s => s.id === updatedStory.id);
        if (index !== -1) {
          this.userStories[index] = { ...updatedStory, showTasks: this.userStories[index].showTasks, tasks: this.userStories[index].tasks };
        }
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

  deleteUserStory(storyId: number, event: MouseEvent): void {
    event.stopPropagation();
    const story = this.userStories.find(s => s.id === storyId);
    if (story) {
      this.storyToDelete = story;
      this.showDeleteStoryModal = true;
      this.deleteError = null;
    }
  }

  confirmDeleteUserStory(): void {
    if (!this.storyToDelete) return;

    this.userStoryService.delete(this.storyToDelete.id).subscribe({
      next: () => {
        this.userStories = this.userStories.filter(s => s.id !== this.storyToDelete!.id);
        this.closeDeleteStoryModal();
      },
      error: (err: any) => {
        this.deleteError = 'Erreur lors de la suppression de la User Story.';
        console.error(err);
      }
    });
  }

  closeDeleteStoryModal(): void {
    this.showDeleteStoryModal = false;
    this.storyToDelete = null;
    this.deleteError = null;
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
    if (!this.currentEditingTask) {
      console.error('Aucune tâche en cours d\'édition');
      return;
    }
    
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

  deleteTask(taskId: number, userStoryId: number, event: MouseEvent): void {
    event.stopPropagation();
    this.taskToDelete = { taskId, userStoryId };
    this.showDeleteTaskModal = true;
    this.deleteError = null;
  }

  confirmDeleteTask(): void {
    if (!this.taskToDelete) return;

    this.taskService.delete(this.taskToDelete.taskId).subscribe({
      next: () => {
        const story = this.userStories.find(s => s.id === this.taskToDelete!.userStoryId);
        if (story && story.tasks) {
          story.tasks = story.tasks.filter(t => t.id !== this.taskToDelete!.taskId);
        }
        this.closeDeleteTaskModal();
      },
      error: (err: any) => {
        this.deleteError = 'Erreur lors de la suppression de la tâche.';
        console.error(err);
      }
    });
  }

  closeDeleteTaskModal(): void {
    this.showDeleteTaskModal = false;
    this.taskToDelete = null;
    this.deleteError = null;
  }

  getPriorityLabel(priority: string): string {
    return KanbanHelpers.getPriorityLabel(priority);
  }

  getStatusLabel(status: string): string {
    return KanbanHelpers.getStatusLabel(status);
  }

  getTaskCountLabel(story: UserStory): string {
    return KanbanHelpers.getTaskCountLabel(story.tasks?.length || 0);
  }
}
