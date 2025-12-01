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

/**
 * Composant affichant la liste des projets de l'utilisateur.
 * Permet la navigation vers les détails, l'édition ou la suppression.
 */
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

  /**
   * Charge la liste des projets associés à l'utilisateur courant au chargement.
   */
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

  /**
   * Redirige vers la page de création.
   */
  createProject(): void {
    this.router.navigate(['/projects/create']);
  }

  /**
   * Ouvre la vue détaillée d'un projet (Kanban/Board).
   * @param projectId ID du projet.
   */
  openProject(projectId: number): void {
    this.router.navigate(['/projects', projectId]);
  }

  /**
   * Ouvre le formulaire d'édition.
   * Utilise stopPropagation() pour éviter d'ouvrir le projet en cliquant sur le bouton éditer.
   * @param projectId ID du projet.
   * @param event Événement du clic.
   */
  editProject(projectId: number, event: MouseEvent): void {
    event.stopPropagation(); // Empêche le déclenchement de openProject
    this.router.navigate(['/projects/edit', projectId]);
  }

  /**
   * Supprime un projet après confirmation.
   * Met à jour la liste locale en cas de succès pour éviter un rechargement.
   * @param projectId ID du projet.
   * @param event Événement du clic.
   */
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
        // Mise à jour optimiste de l'UI
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