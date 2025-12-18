import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';

/**
 * Composant d'inscription (Register).
 * Gère le formulaire de création de compte utilisateur, la validation des champs
 * (email, mot de passe) et la communication avec l'API.
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  
  /** Groupe de formulaire réactif. */
  registerForm;

  /** Indique si l'inscription a réussi pour afficher le message de succès. */
  success = false;

  /** Message d'erreur retourné par l'API en cas d'échec. */
  error = '';

  /** Bascule pour afficher/masquer le mot de passe en clair. */
  showPassword = false;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    // Initialisation du formulaire avec des règles de validation strictes
    this.registerForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      // Mot de passe : requis, minimum 8 caractères
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  /**
   * Soumet le formulaire d'inscription.
   * En cas de succès : affiche un message et redirige vers le login après 1 seconde.
   * En cas d'erreur : affiche le message d'erreur à l'utilisateur.
   */
  onSubmit() {
    if (this.registerForm.valid) {
      this.http.post('https://taskforge-dev.onrender.com/api/auth/register', this.registerForm.value).subscribe({
        next: () => {
          this.success = true;
          this.error = '';
          this.registerForm.reset();
          
          // Redirection différée vers la page de connexion pour l'UX
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1000);
        },
        error: (err: any) => {
          // Gestion des erreurs API (ex: email déjà utilisé)
          this.error = err.error?.message || err.error;
          this.success = false;
        }
      });
    }
  }
}