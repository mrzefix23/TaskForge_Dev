import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../header/header';

interface Project {
  id: number;
  name: string;
  description: string;
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
}
