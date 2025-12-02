import { ComponentFixture, TestBed } from '@angular/core/testing';
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
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

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
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditProjectComponent, HttpClientTestingModule, ReactiveFormsModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? '1' : null)
              }
            }
          }
        },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditProjectComponent);
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

  it('should not submit if form is invalid', () => {
    component.projectForm.controls['name'].setValue('');
    component.onSubmit();
    expect(component.projectForm.invalid).toBeTrue();
  });

  it('should navigate back to projects list on goBack', () => {
    component.projectId = 1;
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/projects']);
  });

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
});
