import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthGuard } from './auth.guard';

/**
 * Tests unitaires pour le guard AuthGuard.
 * Vérifie le contrôle d'accès aux routes protégées.
 */
describe('AuthGuard', () => {
  let guard: AuthGuard;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: Router, useValue: routerSpy }
      ]
    });

    guard = TestBed.inject(AuthGuard);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  describe('canActivate', () => {
    it('should allow access when token is present', () => {
      localStorage.setItem('token', 'valid-token');

      const result = guard.canActivate();

      expect(result).toBeTrue();
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });
});
