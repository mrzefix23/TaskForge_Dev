import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';  
import { ProjectService } from './project.service';
import { Project } from './project.model';

@Component({
  selector: 'app-projects-list',
  standalone: true,
  imports: [CommonModule, RouterLink], 
  templateUrl: './projects-list.html',
  styleUrl: './projects-list.css',
})
export class ProjectsList implements OnInit {
  
  projects: Project[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(private projectService: ProjectService) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getMyProjects().subscribe({
      next: (data) => {
        this.projects = data;
        this.isLoading = false;
        console.log('Projets reçus :', data);
      },
      error: (error) => {
        console.error('Erreur API :', error);
        this.errorMessage = 'Impossible de charger vos projets.';
        this.isLoading = false;
      }
    });
  }
}