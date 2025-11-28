import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HeaderComponent } from '../header/header';
import { UserStoryFormComponent } from './user-story-form/user-story-form';
import { TaskFormComponent } from './task-form/task-form';

// Types
interface User {
  id: number;
  username: string;
}

interface Task {
  id: number;
  title: string;
  description?: string;
  status: string;
  priority: string;
  assignedTo?: User;
}

interface UserStory {
  id: number;
  title: string;
  description?: string;
  status: string;
  priority: string;
  assignedTo?: User[];
  tasks?: Task[];
  showTasks?: boolean;
}

interface Project {
  id: number;
  name: string;
  description?: string;
  owner: User;
  members: User[];
  userStories?: UserStory[];
  customColumns?: Array<{ id: string; label: string }>;
}

interface KanbanColumn {
  id: string;
  label: string;
  isDefault: boolean;
}

@Component({
  selector: 'app-kanban',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent, UserStoryFormComponent, TaskFormComponent],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css']
})
export class KanbanComponent implements OnInit {
  project: Project | null = null;
  loading = false;
  error = '';

  // Colonnes par défaut
  defaultColumns: KanbanColumn[] = [
    { id: 'TODO', label: 'À faire', isDefault: true },
    { id: 'IN_PROGRESS', label: 'En cours', isDefault: true },
    { id: 'DONE', label: 'Terminé', isDefault: true }
  ];

  // Colonnes personnalisées
  customColumns: KanbanColumn[] = [];

  // Edition de colonne
  editingColumnId: string | null = null;
  editedColumnLabel = '';

  // Modals
  showCreateStoryModal = false;
  showEditStoryModal = false;
  showCreateTaskModal = false;
  showEditTaskModal = false;

  currentEditingStory: UserStory | null = null;
  currentEditingTask: Task | null = null;
  currentStoryIdForTask: number | null = null;

  userStoryError = '';
  editUserStoryError = '';
  taskError = '';
  editTaskError = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProject(Number(projectId));
    }
  }

  get allColumns(): KanbanColumn[] {
    return [...this.defaultColumns, ...this.customColumns];
  }

  loadProject(id: number): void {
    this.loading = true;
    this.error = '';

    // Simulation API
    setTimeout(() => {
      this.project = {
        id,
        name: 'Projet TaskForge',
        description: 'Gestion de projet agile',
        owner: { id: 1, username: 'admin' },
        members: [
          { id: 2, username: 'alice' },
          { id: 3, username: 'bob' }
        ],
        customColumns: [],
        userStories: [
          {
            id: 1,
            title: 'Interface utilisateur',
            description: 'Créer l\'interface du dashboard',
            status: 'TODO',
            priority: 'HIGH',
            assignedTo: [{ id: 2, username: 'alice' }],
            tasks: [],
            showTasks: false
          }
        ]
      };

      if (this.project.customColumns) {
        this.customColumns = this.project.customColumns.map(col => ({
          ...col,
          isDefault: false
        }));
      }

      this.loading = false;
    }, 500);
  }

  // --- Gestion des Colonnes ---

  addColumn(): void {
    const newId = `CUSTOM_${Date.now()}`;
    const newColumn: KanbanColumn = {
      id: newId,
      label: 'Nouvelle colonne',
      isDefault: false
    };
    this.customColumns.push(newColumn);
    
    // Activer l'édition immédiatement
    setTimeout(() => this.startEditColumnLabel(newColumn), 0);
  }

  isColumnDeletable(columnId: string): boolean {
    const column = this.allColumns.find(c => c.id === columnId);
    return column ? !column.isDefault : false;
  }

  deleteColumn(columnId: string): void {
    const column = this.allColumns.find(c => c.id === columnId);
    if (!column || column.isDefault) return;

    const hasStories = this.getStoriesByStatus(columnId).length > 0;
    if (hasStories) {
      if (!confirm('Cette colonne contient des user stories. Voulez-vous vraiment la supprimer ?')) {
        return;
      }
    }

    this.customColumns = this.customColumns.filter(c => c.id !== columnId);
    
    // Déplacer les stories orphelines vers TODO
    if (this.project?.userStories) {
      this.project.userStories.forEach(story => {
        if (story.status === columnId) {
          story.status = 'TODO';
        }
      });
    }
  }

  startEditColumnLabel(column: KanbanColumn): void {
    this.editingColumnId = column.id;
    this.editedColumnLabel = column.label;
    
    setTimeout(() => {
      const input = document.getElementById(`column-label-${column.id}`) as HTMLInputElement;
      if (input) {
        input.focus();
        input.select();
      }
    }, 0);
  }

  saveColumnLabel(column: KanbanColumn): void {
    if (!this.editedColumnLabel.trim()) return;

    const col = this.allColumns.find(c => c.id === column.id);
    if (col) {
      col.label = this.editedColumnLabel.trim();
    }
    this.editingColumnId = null;
    this.editedColumnLabel = '';
  }

  cancelEditColumnLabel(): void {
    this.editingColumnId = null;
    this.editedColumnLabel = '';
  }

  getStoriesByStatus(status: string): UserStory[] {
    if (!this.project?.userStories) return [];
    return this.project.userStories.filter(story => story.status === status);
  }

  // --- Drag & Drop ---

  onDragStart(event: DragEvent, story: UserStory): void {
    if (event.dataTransfer) {
      event.dataTransfer.setData('text/plain', story.id.toString());
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault(); // Nécessaire pour autoriser le drop
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  onDrop(event: DragEvent, newStatus: string): void {
    event.preventDefault();
    const storyId = Number(event.dataTransfer?.getData('text/plain'));
    
    if (this.project?.userStories) {
      const story = this.project.userStories.find(s => s.id === storyId);
      if (story && story.status !== newStatus) {
        story.status = newStatus;
      }
    }
  }

  // --- User Stories & Tasks (Méthodes existantes adaptées) ---

  openCreateStoryModal(): void {
    this.showCreateStoryModal = true;
    this.userStoryError = '';
  }

  closeCreateStoryModal(): void {
    this.showCreateStoryModal = false;
  }

  onCreateUserStory(formData: any): void {
    if (!this.project) return;
    const newStory: UserStory = {
      id: Date.now(),
      title: formData.title,
      description: formData.description,
      status: formData.status || 'TODO',
      priority: formData.priority,
      assignedTo: formData.assignedTo || [],
      tasks: [],
      showTasks: false
    };
    if (!this.project.userStories) this.project.userStories = [];
    this.project.userStories.push(newStory);
    this.closeCreateStoryModal();
  }

  openEditStoryModal(story: UserStory, event: Event): void {
    event.stopPropagation();
    this.currentEditingStory = { ...story };
    this.showEditStoryModal = true;
  }

  closeEditStoryModal(): void {
    this.showEditStoryModal = false;
    this.currentEditingStory = null;
  }

  onEditUserStory(formData: any): void {
    if (!this.project?.userStories || !this.currentEditingStory) return;
    const index = this.project.userStories.findIndex(s => s.id === this.currentEditingStory!.id);
    if (index !== -1) {
      this.project.userStories[index] = { ...this.project.userStories[index], ...formData };
    }
    this.closeEditStoryModal();
  }

  deleteUserStory(storyId: number, event: Event): void {
    event.stopPropagation();
    if (!confirm('Supprimer cette story ?')) return;
    if (this.project?.userStories) {
      this.project.userStories = this.project.userStories.filter(s => s.id !== storyId);
    }
  }

  toggleTasks(story: UserStory, event: Event): void {
    event.stopPropagation();
    story.showTasks = !story.showTasks;
  }

  getTaskCountLabel(story: UserStory): string {
    const count = story.tasks?.length || 0;
    return count === 0 ? 'Aucune tâche' : `${count} tâche${count > 1 ? 's' : ''}`;
  }

  openCreateTaskModal(storyId: number, event: Event): void {
    event.stopPropagation();
    this.currentStoryIdForTask = storyId;
    this.showCreateTaskModal = true;
  }

  closeCreateTaskModal(): void {
    this.showCreateTaskModal = false;
    this.currentStoryIdForTask = null;
  }

  onCreateTask(formData: any): void {
    if (!this.project?.userStories || this.currentStoryIdForTask === null) return;
    const story = this.project.userStories.find(s => s.id === this.currentStoryIdForTask);
    if (!story) return;
    const newTask: Task = {
      id: Date.now(),
      title: formData.title,
      description: formData.description,
      status: formData.status || 'TODO',
      priority: formData.priority,
      assignedTo: formData.assignedTo
    };
    if (!story.tasks) story.tasks = [];
    story.tasks.push(newTask);
    this.closeCreateTaskModal();
  }

  openEditTaskModal(task: Task, event: Event): void {
    event.stopPropagation();
    this.currentEditingTask = { ...task };
    this.showEditTaskModal = true;
  }

  closeEditTaskModal(): void {
    this.showEditTaskModal = false;
    this.currentEditingTask = null;
  }

  onEditTask(formData: any): void {
    if (!this.project?.userStories || !this.currentEditingTask) return;
    for (const story of this.project.userStories) {
      if (!story.tasks) continue;
      const taskIndex = story.tasks.findIndex(t => t.id === this.currentEditingTask!.id);
      if (taskIndex !== -1) {
        story.tasks[taskIndex] = { ...story.tasks[taskIndex], ...formData };
        break;
      }
    }
    this.closeEditTaskModal();
  }

  deleteTask(taskId: number, storyId: number, event: Event): void {
    event.stopPropagation();
    if (!confirm('Supprimer cette tâche ?')) return;
    const story = this.project?.userStories?.find(s => s.id === storyId);
    if (story?.tasks) {
      story.tasks = story.tasks.filter(t => t.id !== taskId);
    }
  }

  getPriorityLabel(priority: string): string {
    const labels: Record<string, string> = { 'LOW': 'Basse', 'MEDIUM': 'Moyenne', 'HIGH': 'Haute' };
    return labels[priority] || priority;
  }

  getStatusLabel(status: string): string {
    const col = this.allColumns.find(c => c.id === status);
    return col ? col.label : status;
  }
}
