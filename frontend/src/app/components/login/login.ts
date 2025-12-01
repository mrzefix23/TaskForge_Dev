import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

/**
 * Composant gérant la page de connexion.
 * Il permet aux utilisateurs de s'authentifier via un formulaire,
 * récupère le token JWT et redirige vers la page d'accueil.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  
  /** Groupe de formulaire réactif pour gérer username et password. */
  loginForm;

  /** Indique si la connexion a réussi (pour l'affichage UI). */
  success = false;

  /** Message d'erreur à afficher en cas d'échec de l'authentification. */
  error = '';

  /** Jeton d'authentification reçu du backend. */
  token = '';

  /** Contrôle la visibilité du mot de passe (texte clair / masqué). */
  showPassword = false;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    // Initialisation du formulaire avec validateurs requis
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  /**
   * Soumet le formulaire de connexion.
   * Si le formulaire est valide, envoie une requête POST à l'API.
   * Gère le stockage du token/username en local et la redirection vers '/accueil'.
   */
  onSubmit() {
    if (this.loginForm.valid) {
      this.http.post<{token: string, username: string}>('/api/auth/login', this.loginForm.value).subscribe({
        next: (res) => {
          this.success = true;
          this.error = '';
          this.token = res.token;
          
          // Stockage des informations de session
          localStorage.setItem('token', res.token);
          localStorage.setItem('username', res.username); 
          
          this.loginForm.reset();
          
          // Redirection vers le tableau de bord
          this.router.navigate(['/accueil']); 
        },
        error: (err: any) => {
          // Extraction du message d'erreur depuis la réponse API
          this.error = err.error?.message || err.error;
          this.success = false;
        }
      });
    }
  }
}