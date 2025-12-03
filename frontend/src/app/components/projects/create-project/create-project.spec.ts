import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CreateProjectComponent } from './create-project';
import { provideRouter } from '@angular/router';

describe('CreateProjectComponent', () => {
  let component: CreateProjectComponent;
  let fixture: ComponentFixture<CreateProjectComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockUsers = [
    { id: 1, username: 'user1', email: 'user1@test.com' },
    { id: 2, username: 'user2', email: 'user2@test.com' },
    { id: 3, username: 'user3', email: 'user3@test.com' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateProjectComponent, HttpClientTestingModule, ReactiveFormsModule],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateProjectComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('username', 'testuser');
    spyOn(router, 'navigate').and.stub();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.projectForm.value.name).toBe('');
    expect(component.projectForm.value.description).toBe('');
  });

  it('should make form invalid when name is empty', () => {
    component.projectForm.controls['name'].setValue('');
    expect(component.projectForm.valid).toBeFalsy();
  });

  it('should make form valid when name is provided', () => {
    component.projectForm.controls['name'].setValue('Test Project');
    expect(component.projectForm.valid).toBeTruthy();
  });

  it('should not submit if form is invalid', () => {
    component.projectForm.controls['name'].setValue('');
    component.onSubmit();
    expect(component.projectForm.invalid).toBeTrue();
  });

  it('should navigate back to projects list', () => {
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/projects']);
  });

  it('should not submit if username is missing', () => {
    localStorage.removeItem('username');
    component.projectForm.controls['name'].setValue('Test Project');

    component.onSubmit();

    expect(component.loading).toBeFalse();
    expect(component.error).toBe('Utilisateur non authentifié.');
  });

  it('should get current username from localStorage', () => {
    fixture.detectChanges();
    expect(component.currentUsername).toBe('testuser');
  });
});
