import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../header/header';

interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
  members: { username: string }[];
}

interface User {
  username: string;
}

/**
 * Composant permettant de modifier un projet existant.
 * Réutilise les styles du formulaire de création pour la cohérence visuelle.
 */
@Component({
  selector: 'app-edit-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './edit-project.html',
  styleUrls: ['../create-project/create-project.css'] // Réutilisation du CSS de création
})
export class EditProjectComponent implements OnInit {
  projectForm: FormGroup;
  success = false;
  error = '';
  loading = false;
  initialLoading = true;
  projectId: number = 0;
  
  /** Liste complète des utilisateurs (sauf le propriétaire actuel). */
  allUsers: User[] = [];
  
  currentUsername: string | null = null;
  ownerUsername: string | null = null;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Initialisation du formulaire
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      members: [[]]
    });
  }

  /**
   * Initialisation : Récupère l'ID du projet depuis l'URL
   * et lance la chaîne de chargement des données.
   */
  ngOnInit(): void {
    this.currentUsername = localStorage.getItem('username');
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.projectId = +id;
      this.loadAllUsersAndProject();
    } else {
      this.initialLoading = false;
    }
  }

  /**
   * Retourne à la vue précédente (Détail du projet si ID connu, sinon Liste).
   */
  goBack(): void {
    if (this.projectId) {
      this.router.navigate(['/projects']);
    } else {
      this.router.navigate(['/projects']);
    }
  }

  /**
   * Orchestre le chargement des données.
   * Charge D'ABORD les utilisateurs, PUIS le projet pour s'assurer
   * que la liste des membres est disponible lors du remplissage du formulaire (patchValue).
   */
  loadAllUsersAndProject(): void {
    const token = localStorage.getItem('token');
    this.http.get<User[]>('https://taskforge-dev.onrender.com/api/users', {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (users) => {
        this.allUsers = users;
        this.loadProject(this.projectId); // Chaînage séquentiel
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        this.error = 'Impossible de charger la liste des membres potentiels.';
        this.initialLoading = false;
      }
    });
  }

  /**
   * Charge les détails du projet et pré-remplit le formulaire.
   * Filtre le propriétaire hors de la liste des membres sélectionnables.
   * @param projectId L'identifiant du projet à charger.
   */
  loadProject(projectId: number): void {
    const token = localStorage.getItem('token');
    this.http.get<Project>(`https://taskforge-dev.onrender.com/api/projects/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.ownerUsername = data.owner.username;
        
        // Retire le propriétaire de la liste des membres potentiels
        this.allUsers = this.allUsers.filter(u => u.username !== this.ownerUsername);

        // Mise à jour du formulaire avec les données actuelles
        this.projectForm.patchValue({
          name: data.name,
          description: data.description,
          members: data.members
            .filter(m => m.username !== this.ownerUsername)
            .map(m => m.username)
        });

        this.initialLoading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du projet.';
        this.initialLoading = false;
        console.error(err);
      }
    });
  }

  /**
   * Soumet les modifications du projet au serveur.
   * Redirige vers la liste des projets en cas de succès.
   */
  onSubmit() {
    if (this.projectForm.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = false;

    const token = localStorage.getItem('token');

    // Construction du payload (objet de mise à jour)
    const payload = {
      name: this.projectForm.value.name,
      description: this.projectForm.value.description,
      // Transformation de la liste de strings en objets utilisateurs
      members: this.projectForm.value.members.map((username: string) => ({ username }))
    };

    this.http.put(`https://taskforge-dev.onrender.com/api/projects/${this.projectId}`, payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/projects']);
        }, 1000);
      },
      error: (err) => {
        this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la mise à jour du projet.';
        this.loading = false;
      }
    });
  }
}