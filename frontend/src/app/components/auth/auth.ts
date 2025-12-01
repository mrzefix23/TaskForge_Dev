import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

/**
 * Composant racine pour le module d'authentification.
 * Ce composant sert principalement de conteneur ("Layout") pour les pages enfants
 * (Login, Register) ou de page d'atterrissage (Landing) selon la configuration du router.
 */
@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent {
  
  /** * Année courante utilisée pour l'affichage dynamique du copyright dans le footer.
   */
  currentYear = new Date().getFullYear();
}