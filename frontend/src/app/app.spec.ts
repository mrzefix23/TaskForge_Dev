import { TestBed } from '@angular/core/testing';
import { App } from './app';

/**
 * Suite de tests unitaires pour le composant racine App.
 */
describe('App', () => {
  
  /**
   * Configuration de l'environnement de test avant chaque test.
   * Importe le composant App (standalone) pour le rendre disponible au testeur.
   */
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  /**
   * Test de base ("Sanity Check").
   * Vérifie simplement que le composant peut être instancié sans erreur.
   */
  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});