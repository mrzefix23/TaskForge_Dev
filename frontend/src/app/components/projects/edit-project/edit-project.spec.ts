import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { EditProjectComponent } from './edit-project';
import { ProjectService } from '../../../services/project.service';
import { Project } from '../../../models/kanban.models';

/**
 * Tests unitaires pour le composant EditProjectComponent.
 * Couvre la modification de projets existants.
 */
describe('EditProjectComponent', () => {
  let component: EditProjectComponent;
  let fixture: ComponentFixture<EditProjectComponent>;
  let projectService: jasmine.SpyObj<ProjectService>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockProject: Project = {
    id: 1,
    name: 'Test Project',
    description: 'Test Description',
    owner: { username: 'testowner' },
    members: [{ username: 'member1' }, { username: 'member2' }]
  };

  beforeEach(async () => {
    const projectServiceSpy = jasmine.createSpyObj('ProjectService', [
      'getById',
      'update'
    ]);

    await TestBed.configureTestingModule({
      imports: [EditProjectComponent, HttpClientTestingModule, ReactiveFormsModule],
      providers: [
        { provide: ProjectService, useValue: projectServiceSpy },
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

    projectService = TestBed.inject(ProjectService) as jasmine.SpyObj<ProjectService>;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    projectService.getById.and.returnValue(of(mockProject));

    fixture = TestBed.createComponent(EditProjectComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Setup', () => {
    it('should initialize with a form', () => {
      expect(component.projectForm).toBeDefined();
      expect(component.projectForm.get('name')).toBeDefined();
      expect(component.projectForm.get('description')).toBeDefined();
    });
  });
});
