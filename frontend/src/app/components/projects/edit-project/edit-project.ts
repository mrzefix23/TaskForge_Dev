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

@Component({
  selector: 'app-edit-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './edit-project.html',
  styleUrls: ['../create-project/create-project.css']
})
export class EditProjectComponent implements OnInit {
  projectForm: FormGroup;
  success = false;
  error = '';
  loading = false;
  initialLoading = true;
  projectId: number = 0;
  allUsers: User[] = [];
  currentUsername: string | null = null;
  ownerUsername: string | null = null;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      members: [[]]
    });
  }

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

  goBack(): void {
    if (this.projectId) {
      this.router.navigate(['/projects', this.projectId]);
    } else {
      this.router.navigate(['/projects']);
    }
  }

  loadAllUsersAndProject(): void {
    const token = localStorage.getItem('token');
    this.http.get<User[]>('/api/users', {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (users) => {
        this.allUsers = users;
        this.loadProject(this.projectId); // Load project after users are loaded
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        this.error = 'Impossible de charger la liste des membres potentiels.';
        this.initialLoading = false;
      }
    });
  }

  loadProject(projectId: number): void {
    const token = localStorage.getItem('token');
    this.http.get<Project>(`/api/projects/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.ownerUsername = data.owner.username;
        this.allUsers = this.allUsers.filter(u => u.username !== this.ownerUsername);

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

  onSubmit() {
    if (this.projectForm.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = false;

    const token = localStorage.getItem('token');

    const payload = {
      name: this.projectForm.value.name,
      description: this.projectForm.value.description,
      members: this.projectForm.value.members.map((username: string) => ({ username }))
    };

    this.http.put(`/api/projects/${this.projectId}`, payload, {
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
