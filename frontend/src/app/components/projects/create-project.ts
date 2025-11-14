import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../header/header';

@Component({
  selector: 'app-create-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './create-project.html',
  styleUrl: './create-project.css'
})
export class CreateProjectComponent {
  projectForm;
  success = false;
  error = '';
  loading = false;

  get members() {
    return this.projectForm.get('members') as FormArray;
  }

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      members: this.fb.array([])
    });
  }

  addMember() {
    this.members.push(this.fb.control('', Validators.required));
  }

  removeMember(index: number) {
    this.members.removeAt(index);
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
      members: this.members.value
        .filter((m: string) => !!m && m !== username)
        .map((username: string) => ({ username }))
    };

    this.http.post('/api/projects', payload, {
      // Use token for authorization
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.success = true;
        this.projectForm.reset();
        this.members.clear();
        this.loading = false;
      },
      error: (err) => {
        this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la création du projet.';
        this.loading = false;
      }
    });
  }
}
