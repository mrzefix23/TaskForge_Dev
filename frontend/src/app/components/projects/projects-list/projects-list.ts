import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../header/header';

interface Project {
  id: number;
  name: string;
  description: string;
  owner : { username: string };
  members: { username: string }[];
}

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './projects-list.html',
  styleUrls: ['./projects-list.css']
})
export class ProjectsListComponent implements OnInit {
  projects: Project[] = [];
  error: string | null = null;
  loading = true;
  
  // Modal state for delete confirmation
  showDeleteModal = false;
  projectToDelete: Project | null = null;
  deleteError: string | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    
    this.http.get<Project[]>('/api/projects/myprojects', { 
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des projets.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  createProject(): void {
    this.router.navigate(['/projects/create']);
  }

  openProject(projectId: number): void {
    this.router.navigate(['/projects', projectId]);
  }

  editProject(projectId: number, event: MouseEvent): void {
    event.stopPropagation(); // Empêche le déclenchement de openProject
    this.router.navigate(['/projects/edit', projectId]);
  }

  deleteProject(projectId: number, event: MouseEvent): void {
    event.stopPropagation();
    const project = this.projects.find(p => p.id === projectId);
    if (project) {
      this.projectToDelete = project;
      this.showDeleteModal = true;
      this.deleteError = null;
    }
  }

  confirmDeleteProject(): void {
    if (!this.projectToDelete) return;

    const token = localStorage.getItem('token');

    this.http.delete(`/api/projects/${this.projectToDelete.id}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.projects = this.projects.filter(p => p.id !== this.projectToDelete!.id);
        this.closeDeleteModal();
      },
      error: (err) => {
        if (err.status === 403 && err.error?.message) {
          this.deleteError = err.error.message;
        } else {
          this.deleteError = 'Erreur lors de la suppression du projet.';
        }
        console.error(err);
      }
    });
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.projectToDelete = null;
    this.deleteError = null;
  }
}
