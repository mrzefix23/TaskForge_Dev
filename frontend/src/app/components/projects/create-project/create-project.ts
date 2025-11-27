import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../header/header';

interface User {
  username: string;
}

@Component({
  selector: 'app-create-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './create-project.html',
  styleUrls: ['./create-project.css']
})
export class CreateProjectComponent implements OnInit {
  projectForm;
  success = false;
  error = '';
  loading = false;
  allUsers: User[] = [];
  currentUsername: string | null = null;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      members: [[]]
    });
  }

  ngOnInit(): void {
    this.currentUsername = localStorage.getItem('username');
    this.loadAllUsers();
  }

  loadAllUsers(): void {
    const token = localStorage.getItem('token');
    this.http.get<User[]>('/api/users', {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (users) => {
        this.allUsers = users.filter(u => u.username !== this.currentUsername);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        this.error = 'Impossible de charger la liste des membres potentiels.';
      }
    });
  }

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

    const payload = {
      name: this.projectForm.value.name,
      description: this.projectForm.value.description,
      user: { username },
      members: (this.projectForm.value.members ?? []).map((username: string) => ({ username }))
    };

    this.http.post('/api/projects', payload, {
      // Use token for authorization
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/projects']); // Redirection vers la liste moderne
        }, 1000);
      },
      error: (err) => {
        this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la création du projet.';
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/projects']);
  }
}
