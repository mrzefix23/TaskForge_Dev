import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../header/header';

interface Project {
  id: number;
  name: string;
  description: string;
  members: { username: string }[];
}

@Component({
  selector: 'app-edit-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule, HeaderComponent],
  templateUrl: './edit-project.html',
  styleUrls: ['../create-project/create-project.css']
})
export class EditProjectComponent implements OnInit {
  projectForm;
  success = false;
  error = '';
  loading = false;
  projectId: number | null = null;
  initialLoading = true;

  get members() {
    return this.projectForm.get('members') as FormArray;
  }

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      members: this.fb.array([])
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.projectId = +id;
      this.loadProjectData();
    } else {
      this.error = "ID de projet non trouvé.";
      this.initialLoading = false;
    }
  }

  loadProjectData(): void {
    const token = localStorage.getItem('token');
    this.http.get<Project>(`/api/projects/${this.projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (project) => {
        this.projectForm.patchValue({
          name: project.name,
          description: project.description
        });
        const ownerUsername = localStorage.getItem('username');
        project.members
          .filter(member => member.username !== ownerUsername)
          .forEach(member => {
            this.members.push(this.fb.control(member.username, Validators.required));
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

    const token = localStorage.getItem('token');
    const ownerUsername = localStorage.getItem('username');

    const payload = {
      name: this.projectForm.value.name,
      description: this.projectForm.value.description,
      members: this.members.value
        .filter((m: string) => !!m)
        .map((username: string) => ({ username }))
    };

    this.http.put(`/api/projects/${this.projectId}`, payload, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        setTimeout(() => {
          this.router.navigate(['/myprojects']);
        }, 1000);
      },
      error: (err) => {
        this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la mise à jour du projet.';
        this.loading = false;
      }
    });
  }
}
