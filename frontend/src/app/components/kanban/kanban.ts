import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HeaderComponent } from '../header/header';

interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
  members: { username: string }[];
}

interface UserStory {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string };
}

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, HeaderComponent, ReactiveFormsModule],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css']
})
export class KanbanComponent implements OnInit {
  project: Project | null = null;
  userStories: UserStory[] = [];
  loading = true;
  error: string | null = null;
  
  showCreateStoryModal = false;
  userStoryForm: FormGroup;
  userStoryError: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.userStoryForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM', Validators.required],
      assignedToUsername: ['']
    });
  }

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
    this.userStoryForm.reset({ priority: 'MEDIUM', assignedToUsername: '' });
    this.userStoryError = null;
  }

  onUserStorySubmit(): void {
    if (this.userStoryForm.invalid || !this.project) {
      return;
    }
    this.userStoryError = null;

    const token = localStorage.getItem('token');
    const payload = {
      ...this.userStoryForm.value,
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
}
