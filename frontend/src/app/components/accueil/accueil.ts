import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderComponent } from '../header/header';

/**
 * Composant de la page d'accueil (Dashboard).
 * Affiche le menu principal, les informations utilisateur et les raccourcis vers les projets.
 */
@Component({
  selector: 'app-accueil',
  standalone: true,
  templateUrl: './accueil.html',
  styleUrls: ['./accueil.css'],
  imports: [CommonModule, HeaderComponent]
})
export class AccueilComponent {
  
  /** Récupère le nom d'utilisateur depuis le stockage local pour l'affichage. */
  username = localStorage.getItem('username');

  /** État de visibilité du menu déroulant utilisateur. */
  menuOpen = false;

  constructor(private router: Router) {}

  /**
   * Bascule l'état d'ouverture/fermeture du menu utilisateur.
   */
  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  /**
   * Ferme le menu avec un léger délai.
   * Le délai permet d'éviter que le menu ne se ferme avant que l'action (clic) ne soit enregistrée.
   */
  closeMenu() {
    setTimeout(() => this.menuOpen = false, 100);
  }

  /**
   * Déconnecte l'utilisateur.
   * Nettoie le localStorage (token et username) et redirige vers la page de login/accueil public.
   */
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.router.navigate(['/']);
  }

  /**
   * Navigue vers la liste complète des projets.
   */
  loadProjects() {
    this.router.navigate(['/projects']);
  }

  /**
   * Navigue vers le formulaire de création de projet.
   */
  createProject() {
    this.router.navigate(['/projects/create']);
  }
}