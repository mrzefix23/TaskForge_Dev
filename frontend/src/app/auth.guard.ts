import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

/**
 * Garde de navigation (Guard) pour sécuriser les routes.
 * Vérifie si l'utilisateur possède un jeton d'authentification valide avant d'autoriser l'accès.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  
  constructor(private router: Router) {}

  /**
   * Méthode appelée par le routeur avant d'activer une route protégée.
   * * @returns {boolean} True si l'accès est autorisé (token présent), False sinon.
   * En cas de refus, redirige l'utilisateur vers la page d'erreur (ou de login).
   */
  canActivate(): boolean {
    const token = localStorage.getItem('token');
    
    if (token) {
      return true;
    }
    
    // Redirection si l'utilisateur n'est pas authentifié
    this.router.navigate(['/error']); // Note: Idéalement, rediriger vers '/login'
    return false;
  }
}