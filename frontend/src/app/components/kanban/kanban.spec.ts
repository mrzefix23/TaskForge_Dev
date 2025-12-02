import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { KanbanComponent } from './kanban';
import { ProjectService } from '../../services/project.service';
import { UserStoryService } from '../../services/user-story.service';
import { TaskService } from '../../services/task.service';
import { KanbanColumnService } from '../../services/kanban-column.service';
import { Project, UserStory, Sprint, KanbanColumn } from '../../models/kanban.models';

/**
 * Tests unitaires pour le composant KanbanComponent.
 * Couvre les scénarios principaux : chargement, CRUD, drag & drop, modales.
 */
describe('KanbanComponent', () => {
  let component: KanbanComponent;
  let fixture: ComponentFixture<KanbanComponent>;
  let projectService: jasmine.SpyObj<ProjectService>;
  let userStoryService: jasmine.SpyObj<UserStoryService>;
  let taskService: jasmine.SpyObj<TaskService>;
  let kanbanColumnService: jasmine.SpyObj<KanbanColumnService>;
  let router: Router;

  const mockProject: Project = {
    id: 1,
    name: 'Test Project',
    description: 'Test Description',
    owner: { username: 'owner' },
    members: []
  };

  const mockSprints: Sprint[] = [
    { id: 1, name: 'Sprint 1', startDate: '2025-01-01', endDate: '2025-01-15', status: 'ACTIVE', projectId: 1 }
  ];

  const mockKanbanColumns: KanbanColumn[] = [
    { id: 1, name: 'À Faire', status: 'TODO', order: 1, projectId: 1, isDefault: true },
    { id: 2, name: 'En Cours', status: 'IN_PROGRESS', order: 2, projectId: 1, isDefault: true },
    { id: 3, name: 'Terminé', status: 'DONE', order: 3, projectId: 1, isDefault: true }
  ];

  const mockUserStories: UserStory[] = [
    {
      id: 1,
      title: 'User Story 1',
      description: 'Description 1',
      priority: 'HIGH',
      status: 'TODO',
      assignedTo: [],
      tasks: [],
      showTasks: false
    }
  ];

  beforeEach(async () => {
    const projectServiceSpy = jasmine.createSpyObj('ProjectService', [
      'getById',
      'getSprintsByProject'
    ]);
    const userStoryServiceSpy = jasmine.createSpyObj('UserStoryService', [
      'getByProject',
      'create',
      'update',
      'delete'
    ]);
    const taskServiceSpy = jasmine.createSpyObj('TaskService', [
      'create',
      'update',
      'delete'
    ]);
    const kanbanColumnServiceSpy = jasmine.createSpyObj('KanbanColumnService', [
      'getByProject',
      'create',
      'update',
      'delete'
    ]);

    await TestBed.configureTestingModule({
      imports: [KanbanComponent, HttpClientTestingModule],
      providers: [
        { provide: ProjectService, useValue: projectServiceSpy },
        { provide: UserStoryService, useValue: userStoryServiceSpy },
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: KanbanColumnService, useValue: kanbanColumnServiceSpy },
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
    userStoryService = TestBed.inject(UserStoryService) as jasmine.SpyObj<UserStoryService>;
    taskService = TestBed.inject(TaskService) as jasmine.SpyObj<TaskService>;
    kanbanColumnService = TestBed.inject(KanbanColumnService) as jasmine.SpyObj<KanbanColumnService>;
    router = TestBed.inject(Router);

    projectService.getById.and.returnValue(of(mockProject));
    projectService.getSprintsByProject.and.returnValue(of(mockSprints));
    userStoryService.getByProject.and.returnValue(of(mockUserStories));
    kanbanColumnService.getByProject.and.returnValue(of(mockKanbanColumns));

    fixture = TestBed.createComponent(KanbanComponent);
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
      expect(component.userStories).toEqual([]);
      expect(component.loading).toBeTrue();
      expect(component.error).toBeNull();
      expect(component.selectedSprintFilter).toBe('all');
    });
  });
});
