import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  registerForm;

  success = false;
  error = '';

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.http.post('/auth/register', this.registerForm.value, {responseType: 'text'}).subscribe({
        next: () => {
          this.success = true;
          this.error = '';
          this.registerForm.reset();
        },
        error: (err: any) => {
          this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la création du compte.';
          this.success = false;
        }
      });
    }
  }
}