import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthComponent } from './auth';

describe('Auth', () => {
  let component: AuthComponent;
  let fixture: ComponentFixture<AuthComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthComponent],
      providers: [provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the app name', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.landing-logo')?.textContent).toContain('TaskForge');
  });

  it('should have a link to login', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const loginLink = compiled.querySelector('a[routerLink="/login"]');
    expect(loginLink).toBeTruthy();
    expect(loginLink?.textContent).toContain('Connexion');
  });

  it('should have a link to register', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const registerLink = compiled.querySelector('a[routerLink="/register"]');
    expect(registerLink).toBeTruthy();
    expect(registerLink?.textContent).toContain('Créer un compte');
  });

  it('should have a CTA button to register', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const ctaBtn = compiled.querySelector('.landing-cta-btn');
    expect(ctaBtn).toBeTruthy();
    expect(ctaBtn?.getAttribute('routerLink')).toBe('/register');
    expect(ctaBtn?.textContent).toContain('Commencer');
  });

  it('should display the current year in the footer', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const year = new Date().getFullYear().toString();
    expect(compiled.querySelector('.landing-footer')?.textContent).toContain(year);
  });
});
