import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

/**
 * Composant affichant la page d'erreur 404 (Not Found).
 * Ce composant est affiché lorsque l'utilisateur tente d'accéder à une route inexistante.
 * Il propose des options de navigation pour retourner vers l'accueil ou les projets.
 */
@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './not-found.html',
  styleUrl: './not-found.css'
})
export class NotFoundComponent {
  // Aucune logique métier nécessaire pour cette vue statique
}