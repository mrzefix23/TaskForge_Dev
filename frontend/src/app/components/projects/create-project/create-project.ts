import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../header/header';

interface User {
  username: string;
}

/**
 * Composant de page permettant la création d'un nouveau projet.
 * Gère le formulaire, la sélection des membres et l'appel API de création.
 */
@Component({
  selector: 'app-create-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './create-project.html',
  styleUrls: ['./create-project.css']
})
export class CreateProjectComponent implements OnInit {
  
  /** Formulaire réactif contenant les détails du projet. */
  projectForm;

  /** Indique si le projet a été créé avec succès. */
  success = false;

  /** Message d'erreur en cas d'échec de l'opération. */
  error = '';

  /** État de chargement pendant l'appel API. */
  loading = false;

  /** Liste de tous les utilisateurs disponibles pour être ajoutés au projet. */
  allUsers: User[] = [];

  /** Nom de l'utilisateur actuellement connecté (créateur). */
  currentUsername: string | null = null;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    // Initialisation du formulaire
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      members: [[]] // Tableau pour stocker les membres sélectionnés
    });
  }

  /**
   * Initialisation du composant.
   * Récupère le nom de l'utilisateur courant et charge la liste des membres potentiels.
   */
  ngOnInit(): void {
    this.currentUsername = localStorage.getItem('username');
    this.loadAllUsers();
  }

  /**
   * Récupère la liste de tous les utilisateurs depuis l'API.
   * Filtre la liste pour exclure l'utilisateur courant (il est automatiquement propriétaire).
   */
  loadAllUsers(): void {
    const token = localStorage.getItem('token');
    this.http.get<User[]>('/api/users', {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (users) => {
        // On ne propose pas l'utilisateur courant dans la liste des membres à ajouter
        this.allUsers = users.filter(u => u.username !== this.currentUsername);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        this.error = 'Impossible de charger la liste des membres potentiels.';
      }
    });
  }

  /**
   * Soumet le formulaire de création de projet.
   * Construit le payload attendu par le backend et gère la réponse.
   * Redirige vers la liste des projets après un court délai en cas de succès.
   */
  onSubmit() {
    if (this.projectForm.invalid) return;
    
    this.loading = true;
    this.error = '';
    this.success = false;

    const username = localStorage.getItem('username');
    const token = localStorage.getItem('token');
    
    if (!username) {
      this.error = 'Utilisateur non authentifié.';
      this.loading = false;
      return;
    }

    // Construction de l'objet JSON pour l'API
    const payload = {
      name: this.projectForm.value.name,
      description: this.projectForm.value.description,
      user: { username }, // Le créateur (Owner)
      // Transformation de la liste de strings en liste d'objets { username: string }
      members: (this.projectForm.value.members ?? []).map((username: string) => ({ username }))
    };

    this.http.post('https://taskforge-dev.onrender.com/api/projects', payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        // Délai pour laisser l'utilisateur voir le message de succès
        setTimeout(() => {
          this.router.navigate(['/projects']); 
        }, 1000);
      },
      error: (err) => {
        this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la création du projet.';
        this.loading = false;
      }
    });
  }

  /**
   * Navigue vers la page de liste des projets.
   */
  goBack(): void {
    this.router.navigate(['/projects']);
  }
}