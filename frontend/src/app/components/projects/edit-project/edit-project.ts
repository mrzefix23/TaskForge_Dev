import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
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
      this.loadProject(this.projectId);
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

  loadProject(projectId: number): void {
    const token = localStorage.getItem('token');
    this.http.get<any>(`/api/projects/${projectId}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe({
      next: (data) => {
        this.projectForm.patchValue({
          name: data.name,
          description: data.description
        });

        this.members.clear();
        if (data.owner) {
          const ownerControl = this.fb.control(data.owner.username, Validators.required);
          ownerControl.disable();
          this.members.push(ownerControl);
        }

        if (data.members && data.members.length > 0) {
          data.members
            .filter((m: any) => m.username !== data.owner?.username)
            .forEach((m: any) => {
              this.members.push(this.fb.control(m.username, Validators.required));
            });
        }

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
        .filter((m: string) => !!m && m !== ownerUsername)
        .map((username: string) => ({ username }))
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
