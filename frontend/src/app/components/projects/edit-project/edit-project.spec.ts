import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { EditProjectComponent } from './edit-project';

describe('EditProject EditProjectComponent', () => {
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockProject = {
    id: 1,
    name: 'Test Project',
    description: 'Test Description',
    owner: { username: 'owner' },
    members: [
      { username: 'owner' },
      { username: 'member1' },
      { username: 'member2' }
    ]
  };

  const mockUsers = [
    { username: 'owner' },
    { username: 'member1' },
    { username: 'member2' },
    { username: 'user3' },
    { username: 'user4' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EditProjectComponent,
        ReactiveFormsModule,
        HttpClientTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => key === 'id' ? '1' : null
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    localStorage.setItem('token', 'test-token');
    localStorage.setItem('username', 'testuser');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.projectForm.get('name')?.value).toBe('');
    expect(component.projectForm.get('description')?.value).toBe('');
    expect(component.projectForm.get('members')?.value).toEqual([]);
  });

  it('should not load project when id is not provided', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [EditProjectComponent, ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => null } } }
        }
      ]
    });

    const noIdFixture = TestBed.createComponent(EditProjectComponent);
    const noIdComponent = noIdFixture.componentInstance;
    noIdFixture.detectChanges();

    expect(noIdComponent.projectId).toBe(0);
    expect(noIdComponent.initialLoading).toBe(false);
  });

  it('should validate required fields', () => {
    const nameControl = component.projectForm.get('name');
    
    nameControl?.setValue('');
    expect(nameControl?.invalid).toBe(true);
    expect(nameControl?.hasError('required')).toBe(true);

    nameControl?.setValue('Valid Name');
    expect(nameControl?.valid).toBe(true);
  });

  it('should not submit invalid form', () => {
    component.projectForm.patchValue({ name: '', description: 'Test' });
    const initialLoading = component.loading;
    component.onSubmit();
    httpMock.expectNone('/api/projects/1');
    expect(component.loading).toBe(initialLoading);
  });

  it('should handle submit error with string message', () => {
    component.projectId = 1;
    component.projectForm.patchValue({ name: 'Test', description: 'Test', members: [] });

    component.onSubmit();

    const req = httpMock.expectOne('/api/projects/1');
    req.flush('Custom error message', { status: 400, statusText: 'Bad Request' });

    expect(component.loading).toBe(false);
    expect(component.success).toBe(false);
    expect(component.error).toBe('Custom error message');
  });

  it('should handle submit error with generic message', () => {
    component.projectId = 1;
    component.projectForm.patchValue({ name: 'Test', description: 'Test', members: [] });

    component.onSubmit();

    const req = httpMock.expectOne('/api/projects/1');
    req.flush({ error: 'Not a string' }, { status: 500, statusText: 'Server Error' });

    expect(component.loading).toBe(false);
    expect(component.success).toBe(false);
    expect(component.error).toBe('Erreur lors de la mise à jour du projet.');
  });

  it('should navigate back to projects list', () => {
    spyOn(router, 'navigate');
    component.projectId = 1;
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/projects']);
  });
});
