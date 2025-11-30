import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

/**
 * Point d'entrée principal de l'application.
 *
 * Cette fonction démarre l'application Angular en utilisant le composant racine `App`
 * et la configuration définie dans `appConfig`.
 * C'est ici que l'arbre des composants est initialisé et rendu dans le DOM.
 *
 * @see App Le composant racine.
 * @see appConfig La configuration globale (routes, providers, etc.).
 */
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));