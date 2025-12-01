import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Composant Racine (Root Component) de l'application.
 * Sert de conteneur principal et gère l'affichage des pages via RouterOutlet.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}