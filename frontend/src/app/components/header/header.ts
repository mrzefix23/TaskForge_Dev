import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

/**
 * Composant d'en-tête (Navbar) présent sur les pages de l'application.
 * Gère la navigation principale, l'affichage de l'utilisateur connecté
 * et le menu de déconnexion.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent {
  
  /** Nom de l'utilisateur récupéré depuis le stockage local. */
  username = localStorage.getItem('username');

  /** État de visibilité du menu déroulant utilisateur. */
  menuOpen = false;

  constructor(private router: Router) {}

  /**
   * Détermine la route de redirection du logo.
   * Si l'utilisateur est connecté -> Tableau de bord (/accueil).
   * Sinon -> Page d'atterrissage (/).
   */
  get homeRoute(): string {
    return this.username ? '/accueil' : '/';
  }

  /**
   * Bascule l'état ouvert/fermé du menu utilisateur.
   */
  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  /**
   * Ferme le menu avec un léger délai.
   * Ce délai (100ms) est nécessaire pour permettre au clic sur les boutons
   * du menu d'être enregistré avant que le menu ne disparaisse du DOM.
   */
  closeMenu() {
    setTimeout(() => this.menuOpen = false, 100);
  }

  /**
   * Déconnecte l'utilisateur.
   * Nettoie les données de session (Token, Username) et redirige vers la racine.
   */
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.router.navigate(['/']);
  }
}