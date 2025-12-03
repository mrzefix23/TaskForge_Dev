import { ComponentFixture, TestBed } from '@angular/core/testing';
<<<<<<< HEAD
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { EditProjectComponent } from './edit-project';

describe('EditProject EditProjectComponent', () => {
=======
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { EditProjectComponent } from './edit-project';

interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
  members: { username: string }[];
}

interface User {
  username: string;
}

/**
 * Tests unitaires pour le composant EditProjectComponent.
 * Couvre la modification de projets existants.
 */
describe('EditProjectComponent', () => {
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

<<<<<<< HEAD
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
=======
  const mockProject: Project = {
    id: 1,
    name: 'Test Project',
    description: 'Test Description',
    owner: { username: 'testowner' },
    members: [{ username: 'member1' }, { username: 'member2' }]
  };

  const mockUsers: User[] = [
    { username: 'user1' },
    { username: 'user2' },
    { username: 'member1' },
    { username: 'member2' }
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
<<<<<<< HEAD
      imports: [
        EditProjectComponent,
        ReactiveFormsModule,
        HttpClientTestingModule
      ],
=======
      imports: [EditProjectComponent, HttpClientTestingModule, ReactiveFormsModule],
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
<<<<<<< HEAD
                get: (key: string) => key === 'id' ? '1' : null
              }
            }
          }
        }
=======
                get: (key: string) => (key === 'id' ? '1' : null)
              }
            }
          }
        },
        provideRouter([])
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
<<<<<<< HEAD

    localStorage.setItem('token', 'test-token');
    localStorage.setItem('username', 'testuser');
=======
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('username', 'testuser');
    spyOn(router, 'navigate').and.stub();
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

<<<<<<< HEAD
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

  it('should navigate back to projects list', () => {
    spyOn(router, 'navigate');
=======
  it('should not submit if form is invalid', () => {
    component.projectForm.controls['name'].setValue('');
    component.onSubmit();
    expect(component.projectForm.invalid).toBeTrue();
  });

  it('should navigate back to projects list on goBack', () => {
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
    component.projectId = 1;
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/projects']);
  });
<<<<<<< HEAD
=======

  it('should filter owner from available members', () => {
    component.ownerUsername = 'testowner';
    component.allUsers = [
      { username: 'testowner' },
      { username: 'user1' },
      { username: 'user2' }
    ];

    const availableMembers = component.allUsers.filter(
      u => u.username !== component.ownerUsername
    );

    expect(availableMembers.length).toBe(2);
    expect(availableMembers).toEqual([
      { username: 'user1' },
      { username: 'user2' }
    ]);
  });
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
});
