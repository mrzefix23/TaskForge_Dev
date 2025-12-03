<<<<<<< HEAD
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SprintManagementComponent } from './sprint-management';
import { SprintService, Sprint, UserStory } from '../../services/sprint.service';
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

/**
 * Tests unitaires pour le composant SprintManagementComponent.
 * Vérifie la création, la gestion des sprints, et l'assignation des user stories.
 */
describe('SprintManagement', () => {
  describe('SprintManagementComponent', () => {
    let component: SprintManagementComponent;
    let fixture: ComponentFixture<SprintManagementComponent>;
    let httpMock: HttpTestingController;
    let router: Router;
    let sprintService: SprintService;

    const mockSprints: Sprint[] = [
      {
        id: 1,
        name: 'Sprint 1',
        startDate: '2024-01-01',
        endDate: '2024-01-14',
        status: 'ACTIVE',
        projectId: 1
      },
      {
        id: 2,
        name: 'Sprint 2',
        startDate: '2024-01-15',
        endDate: '2024-01-28',
        status: 'PLANNED',
        projectId: 1
      }
    ];

    const mockUserStories: UserStory[] = [
      {
        id: 100,
        title: 'User Story 1',
        description: 'Description 1',
        priority: 'HIGH',
        status: 'TODO'
      },
      {
        id: 101,
        title: 'User Story 2',
        description: 'Description 2',
        priority: 'MEDIUM',
        status: 'IN_PROGRESS'
      }
    ] as UserStory[];

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [SprintManagementComponent, HttpClientTestingModule, FormsModule, CommonModule],
        providers: [
          provideRouter([]),
          SprintService,
          {
            provide: ActivatedRoute,
            useValue: {
              params: of({ id: '1' }),
              snapshot: { paramMap: { get: () => '1' } }
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(SprintManagementComponent);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      router = TestBed.inject(Router);
      sprintService = TestBed.inject(SprintService);
      
      spyOn(router, 'navigate').and.stub();
      
      // Mock window.fetch for loadProject
      spyOn(window, 'fetch').and.returnValue(
        Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              id: 1,
              name: 'Test Project',
              description: 'Test Description',
              owner: { username: 'testuser' }
            })
        } as any)
      );

      // Mock localStorage
      spyOn(localStorage, 'getItem').and.callFake((key: string) => {
        if (key === 'username') return 'testuser';
        if (key === 'token') return 'test-token';
        return null;
      });
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('ngOnInit should load project, sprints and backlog', fakeAsync(() => {
      fixture.detectChanges();

      // Flush HTTP requests
      const sprintsReq = httpMock.expectOne('/api/sprints/project/1');
      sprintsReq.flush(mockSprints);

      const backlogReq = httpMock.expectOne('/api/sprints/project/1/backlog');
      backlogReq.flush(mockUserStories);

      tick();

      expect(component.projectId).toBe(1);
      expect(component.sprints).toEqual(mockSprints);
      expect(component.backlogStories).toEqual(mockUserStories);
    }));

    it('should select sprint and load its user stories', fakeAsync(() => {
      component.sprints = mockSprints;
      const sprint = mockSprints[0];

      component.selectSprint(sprint);

      const req = httpMock.expectOne(`/api/sprints/${sprint.id}/user-stories`);
      req.flush([mockUserStories[0]]);

      tick();

      expect(component.selectedSprint).toEqual(sprint);
      expect(component.sprintStories.length).toBe(1);
    }));

    it('should open and close create modal', () => {
      component.openCreateModal();
      expect(component.showCreateModal).toBeTrue();
      expect(component.sprintForm.name).toBe('');

      component.closeCreateModal();
      expect(component.showCreateModal).toBeFalse();
    });

    it('should open edit modal with sprint data', () => {
      const sprint = mockSprints[0];
      component.openEditModal(sprint);

      expect(component.showEditModal).toBeTrue();
      expect(component.sprintForm.name).toBe(sprint.name);
      expect(component.sprintForm.startDate).toBe(sprint.startDate);
      expect(component.selectedSprint).toEqual(sprint);
    });

    it('should create sprint successfully', fakeAsync(() => {
      component.projectId = 1;
      component.sprintForm = {
        name: 'New Sprint',
        startDate: '2024-02-01',
        endDate: '2024-02-14',
        status: 'PLANNED'
      };

      component.createSprint();

      const req = httpMock.expectOne('/api/sprints');
      expect(req.request.method).toBe('POST');
      expect(req.request.body.name).toBe('New Sprint');
      req.flush({ id: 3, ...component.sprintForm, projectId: 1 });

      // Reload sprints
      const sprintsReq = httpMock.expectOne('/api/sprints/project/1');
      sprintsReq.flush([...mockSprints, { id: 3, name: 'New Sprint', startDate: '2024-02-01', endDate: '2024-02-14', status: 'PLANNED', projectId: 1 }]);

      tick(100);

      expect(component.success).toBeTruthy();
      expect(component.showCreateModal).toBeFalse();
    }));

    it('should validate form before creating sprint', () => {
      component.sprintForm = {
        name: '',
        startDate: '2024-02-01',
        endDate: '2024-02-14',
        status: 'PLANNED'
      };

      component.createSprint();

      expect(component.error).toBe('Tous les champs sont obligatoires');
    });

    it('should validate end date is after start date', () => {
      component.sprintForm = {
        name: 'Test',
        startDate: '2024-02-14',
        endDate: '2024-02-01',
        status: 'PLANNED'
      };

      const isValid = component.validateForm();

      expect(isValid).toBeFalse();
      expect(component.error).toBe('La date de fin doit être après la date de début');
    });

    it('should update sprint successfully', fakeAsync(() => {
      component.projectId = 1;
      component.selectedSprint = mockSprints[0];
      component.sprintForm = {
        name: 'Updated Sprint',
        startDate: '2024-01-01',
        endDate: '2024-01-14',
        status: 'ACTIVE'
      };

      component.updateSprint();

      const req = httpMock.expectOne(`/api/sprints/${mockSprints[0].id}`);
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockSprints[0], name: 'Updated Sprint' });

      // Reload sprints
      const sprintsReq = httpMock.expectOne('/api/sprints/project/1');
      sprintsReq.flush(mockSprints);

      // Reload sprint stories
      const storiesReq = httpMock.expectOne(`/api/sprints/${mockSprints[0].id}/user-stories`);
      storiesReq.flush([]);

      tick(100);

      expect(component.success).toBeTruthy();
      expect(component.showEditModal).toBeFalse();
    }));

    it('should delete sprint successfully', fakeAsync(() => {
      component.projectId = 1;
      component.sprintToDelete = mockSprints[0];

      component.deleteSprint();

      const req = httpMock.expectOne(`/api/sprints/${mockSprints[0].id}`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      // Reload sprints
      const sprintsReq = httpMock.expectOne('/api/sprints/project/1');
      sprintsReq.flush([mockSprints[1]]);

      // Reload backlog
      const backlogReq = httpMock.expectOne('/api/sprints/project/1/backlog');
      backlogReq.flush(mockUserStories);

      tick(100);

      expect(component.success).toBeTruthy();
      expect(component.showDeleteModal).toBeFalse();
    }));

    it('should assign user story to sprint', fakeAsync(() => {
      component.projectId = 1;
      component.selectedSprint = mockSprints[0];
      const userStoryId = mockUserStories[0].id;
      const sprintId = mockSprints[0].id;

      component.assignStoryToSprint(userStoryId, sprintId);

      const req = httpMock.expectOne(`/api/sprints/${sprintId}/user-stories/${userStoryId}`);
      expect(req.request.method).toBe('POST');
      req.flush({});

      // Reload backlog
      const backlogReq = httpMock.expectOne('/api/sprints/project/1/backlog');
      backlogReq.flush([mockUserStories[1]]);

      // Reload sprint stories
      const storiesReq = httpMock.expectOne(`/api/sprints/${sprintId}/user-stories`);
      storiesReq.flush([mockUserStories[0]]);

      tick(100);

      expect(component.success).toBeTruthy();
    }));

    it('should remove user story from sprint', fakeAsync(() => {
      component.projectId = 1;
      component.selectedSprint = mockSprints[0];
      const userStoryId = mockUserStories[0].id;

      component.removeStoryFromSprint(userStoryId);

      const req = httpMock.expectOne(`/api/sprints/user-stories/${userStoryId}/sprint`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      // Reload backlog
      const backlogReq = httpMock.expectOne('/api/sprints/project/1/backlog');
      backlogReq.flush(mockUserStories);

      // Reload sprint stories
      const storiesReq = httpMock.expectOne(`/api/sprints/${mockSprints[0].id}/user-stories`);
      storiesReq.flush([]);

      tick(100);

      expect(component.success).toBeTruthy();
    }));

    it('should check if user is owner', () => {
      component.project = {
        id: 1,
        name: 'Test',
        description: 'Test',
        owner: { username: 'testuser' }
      };
      component.currentUsername = 'testuser';

      expect(component.isOwner()).toBeTrue();

      component.currentUsername = 'otheruser';
      expect(component.isOwner()).toBeFalse();
    });

    it('should return correct status label', () => {
      expect(component.getStatusLabel('PLANNED')).toBe('Planifié');
      expect(component.getStatusLabel('ACTIVE')).toBe('Actif');
      expect(component.getStatusLabel('COMPLETED')).toBe('Terminé');
    });

    it('should return correct status badge class', () => {
      expect(component.getStatusBadgeClass('PLANNED')).toBe('badge-planned');
      expect(component.getStatusBadgeClass('ACTIVE')).toBe('badge-active');
      expect(component.getStatusBadgeClass('COMPLETED')).toBe('badge-completed');
    });

    it('should return correct priority badge class', () => {
      expect(component.getPriorityBadgeClass('HIGH')).toBe('badge-high');
      expect(component.getPriorityBadgeClass('MEDIUM')).toBe('badge-medium');
      expect(component.getPriorityBadgeClass('LOW')).toBe('badge-low');
    });

    it('should navigate to kanban', () => {
      component.projectId = 1;
      component.goToKanban();

      expect(router.navigate).toHaveBeenCalledWith(['/projects', 1]);
    });

    it('should handle error when loading sprints', fakeAsync(() => {
      fixture.detectChanges();

      const sprintsReq = httpMock.expectOne('/api/sprints/project/1');
      sprintsReq.error(new ProgressEvent('error'), { status: 500 });

      const backlogReq = httpMock.expectOne('/api/sprints/project/1/backlog');
      backlogReq.flush([]);

      tick();

      expect(component.error).toBeTruthy();
    }));

    it('should open and close delete modal', () => {
      const sprint = mockSprints[0];
      component.openDeleteModal(sprint);

      expect(component.showDeleteModal).toBeTrue();
      expect(component.sprintToDelete).toEqual(sprint);

      component.closeDeleteModal();
      expect(component.showDeleteModal).toBeFalse();
      expect(component.sprintToDelete).toBeNull();
    });

    it('should reset form', () => {
      component.sprintForm = {
        name: 'Test',
        startDate: '2024-01-01',
        endDate: '2024-01-14',
        status: 'ACTIVE'
      };

      component.resetForm();

      expect(component.sprintForm.name).toBe('');
      expect(component.sprintForm.startDate).toBe('');
      expect(component.sprintForm.endDate).toBe('');
      expect(component.sprintForm.status).toBe('PLANNED');
    });
=======
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
      endDate: '2025-02-15',
      status: 'PLANNED'
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
      endDate: '2025-01-15',
      status: 'PLANNED'
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
      endDate: '',
      status: 'PLANNED'
    };
    expect(component.validateForm()).toBeFalse();

    component.sprintForm = {
      name: 'Test Sprint',
      startDate: '2025-01-01',
      endDate: '2024-12-01', // End before start
      status: 'PLANNED'
    };
    expect(component.validateForm()).toBeFalse();

    component.sprintForm = {
      name: 'Test Sprint',
      startDate: '2025-01-01',
      endDate: '2025-01-15',
      status: 'PLANNED'
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
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
  });
});
