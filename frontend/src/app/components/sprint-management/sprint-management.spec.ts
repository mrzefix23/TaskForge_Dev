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
      'getUserStoriesBySprint',
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
            params: of({ id: '1' }),
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
    sprintService.getUserStoriesBySprint.and.returnValue(of([]));
    projectService.getById.and.returnValue(of(mockProject));

    fixture = TestBed.createComponent(SprintManagementComponent);
    component = fixture.componentInstance;
    localStorage.setItem('token', 'test-token');
    localStorage.setItem('username', 'testuser');
    spyOn(router, 'navigate').and.stub();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open create sprint modal', () => {
    component.openCreateModal();
    expect(component.showCreateModal).toBeTrue();
    expect(component.sprintForm.name).toBe('');
    expect(component.error).toBeNull();
  });

  it('should close create modal', () => {
    component.showCreateModal = true;
    component.closeCreateModal();
    expect(component.showCreateModal).toBeFalse();
  });

  it('should create sprint successfully', () => {
    component.projectId = 1; // Ensure projectId is set
    const newSprint = {
      id: 3,
      name: 'Sprint 3',
      startDate: '2025-02-01',
      endDate: '2025-02-15',
      status: 'PLANNED' as 'PLANNED',
      projectId: 1
    };

    component.sprintForm = {
      name: 'Sprint 3',
      startDate: '2025-02-01',
      endDate: '2025-02-15'
    };

    sprintService.createSprint.and.returnValue(of(newSprint));

    component.createSprint();

    expect(sprintService.createSprint).toHaveBeenCalledWith({
      name: 'Sprint 3',
      startDate: '2025-02-01',
      endDate: '2025-02-15',
      status: 'PLANNED',
      projectId: 1
    });
    expect(component.showCreateModal).toBeFalse();
    expect(component.success).toBeTruthy();
  });

  it('should open edit sprint modal', () => {
    component.openEditModal(mockSprints[0]);
    expect(component.showEditModal).toBeTrue();
    expect(component.selectedSprint).toEqual(mockSprints[0]);
    expect(component.sprintForm.name).toBe('Sprint 1');
  });

  it('should update sprint successfully', () => {
    component.projectId = 1; // Ensure projectId is set
    component.selectedSprint = mockSprints[0];
    component.sprintForm = {
      name: 'Updated Sprint',
      startDate: '2025-01-01',
      endDate: '2025-01-15'
    };

    const updatedSprint = { ...mockSprints[0], name: 'Updated Sprint' };
    sprintService.updateSprint.and.returnValue(of(updatedSprint));

    component.updateSprint();

    expect(sprintService.updateSprint).toHaveBeenCalledWith(1, {
      name: 'Updated Sprint',
      startDate: '2025-01-01',
      endDate: '2025-01-15',
      status: 'ACTIVE',
      projectId: 1
    });
    expect(component.showEditModal).toBeFalse();
  });

  it('should open delete confirmation modal', () => {
    component.openDeleteModal(mockSprints[0]);
    expect(component.showDeleteModal).toBeTrue();
    expect(component.sprintToDelete).toEqual(mockSprints[0]);
  });

  it('should delete sprint successfully', () => {
    component.sprintToDelete = mockSprints[0];
    sprintService.deleteSprint.and.returnValue(of(void 0));
    sprintService.getSprintsByProject.and.returnValue(of([mockSprints[1]]));

    component.deleteSprint();

    expect(sprintService.deleteSprint).toHaveBeenCalledWith(1);
    expect(component.showDeleteModal).toBeFalse();
  });

  it('should assign user story to sprint', () => {
    const mockUserStory = { ...mockBacklogStories[0] } as any;
    
    sprintService.assignUserStoryToSprint.and.returnValue(of(mockUserStory));
    sprintService.getBacklogUserStories.and.returnValue(of([]));

    component.assignStoryToSprint(1, 1);

    expect(sprintService.assignUserStoryToSprint).toHaveBeenCalledWith(1, 1);
  });

  it('should remove user story from sprint', () => {
    component.selectedSprint = mockSprints[0];
    const mockUserStory = { ...mockBacklogStories[0] } as any;
    
    sprintService.removeUserStoryFromSprint.and.returnValue(of(mockUserStory));
    sprintService.getBacklogUserStories.and.returnValue(of(mockBacklogStories));

    component.removeStoryFromSprint(1);

    expect(sprintService.removeUserStoryFromSprint).toHaveBeenCalledWith(1);
  });

  it('should validate form correctly', () => {
    component.sprintForm = {
      name: '',
      startDate: '',
      endDate: ''
    };
    expect(component.validateForm()).toBeFalse();

    component.sprintForm = {
      name: 'Test Sprint',
      startDate: '2025-01-01',
      endDate: '2024-12-01' // End before start
    };
    expect(component.validateForm()).toBeFalse();

    component.sprintForm = {
      name: 'Test Sprint',
      startDate: '2025-01-01',
      endDate: '2025-01-15'
    };
    expect(component.validateForm()).toBeTrue();
  });

  it('should check if user is owner', () => {
    component.project = mockProject;
    component.currentUsername = 'testuser';
    expect(component.isOwner()).toBeTrue();

    component.currentUsername = 'otheruser';
    expect(component.isOwner()).toBeFalse();
  });

  it('should navigate to kanban', () => {
    component.projectId = 1;
    component.goToKanban();
    expect(router.navigate).toHaveBeenCalledWith(['/projects', 1]);
  });
});
