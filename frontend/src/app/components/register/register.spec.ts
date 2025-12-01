import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { RegisterComponent } from './register';
import { provideRouter } from '@angular/router';

/**
 * Tests unitaires pour le composant RegisterComponent.
 * Vérifie la création du composant.
 */
describe('Register', () => {
  describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;
    let httpMock: HttpTestingController;
    let router: Router;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [RegisterComponent, HttpClientTestingModule],
        providers: [provideRouter([])]
      })
        .compileComponents();

      fixture = TestBed.createComponent(RegisterComponent);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      router = TestBed.inject(Router);
      spyOn(router, 'navigate').and.stub();
      fixture.detectChanges();
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize register form with empty values', () => {
      expect(component.registerForm.value).toEqual({
        username: '',
        email: '',
        password: ''
      });
    });

    it('should make the form invalid when fields are empty', () => {
      expect(component.registerForm.valid).toBeFalsy();
    });

    it('should validate email format', () => {
      const emailControl = component.registerForm.controls['email'];
      emailControl.setValue('invalid-email');
      expect(emailControl.hasError('email')).toBeTruthy();

      emailControl.setValue('valid@email.com');
      expect(emailControl.hasError('email')).toBeFalsy();
    });

    it('should validate password minimum length', () => {
      const passwordControl = component.registerForm.controls['password'];
      passwordControl.setValue('short');
      expect(passwordControl.hasError('minlength')).toBeTruthy();

      passwordControl.setValue('longenough');
      expect(passwordControl.hasError('minlength')).toBeFalsy();
    });

    it('should make the form valid when all fields are filled correctly', () => {
      component.registerForm.controls['username'].setValue('testuser');
      component.registerForm.controls['email'].setValue('test@email.com');
      component.registerForm.controls['password'].setValue('password123');
      expect(component.registerForm.valid).toBeTruthy();
    });

    it('should not call http post if form is invalid', () => {
      component.onSubmit();
      fixture.detectChanges();
      expect(component.registerForm.valid).toBeFalse();
    });
  });

});