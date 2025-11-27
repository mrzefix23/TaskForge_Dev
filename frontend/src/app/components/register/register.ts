import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  registerForm;

  success = false;
  error = '';
  showPassword = false;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.http.post('/api/auth/register', this.registerForm.value).subscribe({
        next: () => {
          this.success = true;
          this.error = '';
          this.registerForm.reset();
          setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1000);
        },
        error: (err: any) => {
          this.error = err.error?.message || err.error;
          this.success = false;
        }
      });
    }
  }
}