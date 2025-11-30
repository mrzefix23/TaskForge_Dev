import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterComponent } from './register';
import { provideRouter } from '@angular/router';

/**
 * Tests unitaires pour le composant RegisterComponent.
 * Vérifie la création du composant.
 */
describe('Register', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
