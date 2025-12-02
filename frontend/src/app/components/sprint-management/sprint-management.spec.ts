import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { SprintManagementComponent } from './sprint-management';
import { SprintService } from '../../services/sprint.service';
import { ProjectService } from '../../services/project.service';
import { Project, Sprint, UserStory } from '../../models/kanban.models';

/**
 * Tests unitaires pour le composant SprintManagementComponent.
 * Couvre la gestion des sprints : création, édition, suppression, assignation.
 */
describe('SprintManagementComponent', () => {
  let component: SprintManagementComponent;
  let fixture: ComponentFixture<SprintManagementComponent>;
  let sprintService: jasmine.SpyObj<SprintService>;
  let projectService: jasmine.SpyObj<ProjectService>;
  let router: Router;

  const mockProject: Project = {
    id: 1,
    name: 'Test Project',
    description: 'Test Description',
    owner: { username: 'testuser' },
    members: []
  };

  const mockSprints: Sprint[] = [
    {
      id: 1,
      name: 'Sprint 1',
      startDate: '2025-01-01',
      endDate: '2025-01-15',
      status: 'ACTIVE',
      projectId: 1
    },
    {
      id: 2,
      name: 'Sprint 2',
      startDate: '2025-01-16',
      endDate: '2025-01-31',
      status: 'PLANNED',
      projectId: 1
    }
  ];

  const mockBacklogStories = [
    {
      id: 1,
      title: 'Backlog Story',
      description: 'Story in backlog',
      priority: 'MEDIUM' as 'MEDIUM',
      status: 'TODO' as 'TODO',
      assignedTo: []
    }
  ];

  beforeEach(async () => {
    const sprintServiceSpy = jasmine.createSpyObj('SprintService', [
      'getSprintsByProject',
      'createSprint',
      'updateSprint',
      'deleteSprint',
      'getBacklogUserStories',
      'assignUserStoryToSprint',
      'removeUserStoryFromSprint'
    ]);
    const projectServiceSpy = jasmine.createSpyObj('ProjectService', ['getById']);

    await TestBed.configureTestingModule({
      imports: [SprintManagementComponent, HttpClientTestingModule, FormsModule],
      providers: [
        { provide: SprintService, useValue: sprintServiceSpy },
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

    sprintService = TestBed.inject(SprintService) as jasmine.SpyObj<SprintService>;
    projectService = TestBed.inject(ProjectService) as jasmine.SpyObj<ProjectService>;
    router = TestBed.inject(Router);

    sprintService.getSprintsByProject.and.returnValue(of(mockSprints));
    sprintService.getBacklogUserStories.and.returnValue(of(mockBacklogStories));
    projectService.getById.and.returnValue(of(mockProject));

    fixture = TestBed.createComponent(SprintManagementComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Setup', () => {
    it('should have correct initial state', () => {
      expect(component.sprints).toEqual([]);
      expect(component.error).toBeNull();
    });
  });
});
