import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  loginForm;
  success = false;
  error = '';
  token = '';

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.http.post<{token: string, username: string}>('/auth/login', this.loginForm.value).subscribe({
        next: (res) => {
          this.success = true;
          this.error = '';
          this.token = res.token;
          localStorage.setItem('token', res.token);
          localStorage.setItem('username', res.username); // Store username in localStorage
          this.loginForm.reset();
          this.router.navigate(['/accueil']); // Redirect to accueil on successful login
        },
        error: (err: any) => {
          this.error = typeof err.error === 'string' ? err.error : 'Erreur lors de la connexion.';
          this.success = false;
        }
      });
    }
  }
}
