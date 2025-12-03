<<<<<<< HEAD
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
=======
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
import { VersionManagementComponent } from './version-management';
import { VersionService } from '../../services/version.service';
import { UserStoryService } from '../../services/user-story.service';
import { ProjectService } from '../../services/project.service';
import { Version, UserStory, Project } from '../../models/kanban.models';
<<<<<<< HEAD
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

/**
 * Tests unitaires pour le composant VersionManagementComponent.
 * Vérifie la gestion des versions, l'assignation des user stories et les opérations CRUD.
 */
describe('VersionManagement', () => {
  describe('VersionManagementComponent', () => {
    let component: VersionManagementComponent;
    let fixture: ComponentFixture<VersionManagementComponent>;
    let httpMock: HttpTestingController;
    let router: Router;
    let versionService: VersionService;
    let userStoryService: UserStoryService;
    let projectService: ProjectService;

    const mockProject: Project = {
      id: 1,
      name: 'Test Project',
      description: 'Test Description',
      owner: { username: 'testuser' },
      members: []
    };

    const mockVersions: Version[] = [
      {
        id: 1,
        title: 'Version 1.0',
        versionNumber: '1.0.0',
        description: 'First release',
        status: 'PLANNED',
        releaseDate: '2024-12-31',
        userStories: []
      },
      {
        id: 2,
        title: 'Version 2.0',
        versionNumber: '2.0.0',
        description: 'Second release',
        status: 'IN_PROGRESS',
        releaseDate: undefined,
        userStories: []
      }
    ] as Version[];

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
        imports: [VersionManagementComponent, HttpClientTestingModule, FormsModule, CommonModule],
        providers: [
          provideRouter([]),
          VersionService,
          UserStoryService,
          ProjectService,
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { paramMap: { get: () => '1' } }
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(VersionManagementComponent);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      router = TestBed.inject(Router);
      versionService = TestBed.inject(VersionService);
      userStoryService = TestBed.inject(UserStoryService);
      projectService = TestBed.inject(ProjectService);

      spyOn(router, 'navigate').and.stub();
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('ngOnInit should load project, versions and available user stories', fakeAsync(() => {
      fixture.detectChanges();

      // Flush HTTP requests
      const projectReq = httpMock.expectOne('/api/projects/1');
      projectReq.flush(mockProject);

      const versionsReq = httpMock.expectOne('/api/versions/project/1');
      versionsReq.flush(mockVersions);

      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush(mockUserStories);

      tick();

      expect(component.project).toEqual(mockProject);
      expect(component.versions).toEqual(mockVersions);
      expect(component.availableUserStories.length).toBe(2);
      expect(component.loading).toBeFalse();
    }));

    it('should open and close create version modal', () => {
      component.project = mockProject;
      component.openCreateVersionModal();

      expect(component.showVersionModal).toBeTrue();
      expect(component.isEditMode).toBeFalse();
      expect(component.versionForm.title).toBe('');
      expect(component.versionForm.projectId).toBe(1);

      component.closeVersionModal();
      expect(component.showVersionModal).toBeFalse();
    });

    it('should open edit modal with version data', () => {
      component.project = mockProject;
      const version = mockVersions[0];
      component.openEditVersionModal(version);

      expect(component.showVersionModal).toBeTrue();
      expect(component.isEditMode).toBeTrue();
      expect(component.versionForm.title).toBe(version.title);
      expect(component.versionForm.versionNumber).toBe(version.versionNumber);
      expect(component.currentVersion).toEqual(version);
    });

    it('should create version successfully', fakeAsync(() => {
      component.project = mockProject;
      component.versionForm = {
        title: 'New Version',
        versionNumber: '3.0.0',
        description: 'New release',
        projectId: 1
      };
      component.isEditMode = false;

      component.saveVersion();

      const req = httpMock.expectOne('/api/versions');
      expect(req.request.method).toBe('POST');
      expect(req.request.body.title).toBe('New Version');
      req.flush({ id: 3, ...component.versionForm });

      tick();

      expect(component.versions.length).toBe(1);
      expect(component.showVersionModal).toBeFalse();
    }));

    it('should validate form before creating version', () => {
      component.versionForm = {
        title: '',
        versionNumber: '1.0.0',
        description: '',
        projectId: 1
      };

      component.saveVersion();

      expect(component.versionError).toBe('Veuillez remplir le titre et le numéro de version.');
    });

    it('should update version successfully', fakeAsync(() => {
      component.project = mockProject;
      component.versions = [...mockVersions];
      component.currentVersion = mockVersions[0];
      component.isEditMode = true;
      component.versionForm = {
        title: 'Updated Version',
        versionNumber: '1.0.1',
        description: 'Updated',
        projectId: 1
      };

      component.saveVersion();

      const req = httpMock.expectOne(`/api/versions/${mockVersions[0].id}`);
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockVersions[0], title: 'Updated Version' });

      tick();

      expect(component.versions[0].title).toBe('Updated Version');
      expect(component.showVersionModal).toBeFalse();
    }));

    it('should delete version successfully', fakeAsync(() => {
      component.project = mockProject;
      component.versions = [...mockVersions];
      component.versionToDelete = mockVersions[0];

      component.confirmDelete();

      const req = httpMock.expectOne(`/api/versions/${mockVersions[0].id}`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      // Reload available user stories
      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush(mockUserStories);

      tick();

      expect(component.versions.length).toBe(1);
      expect(component.showDeleteModal).toBeFalse();
    }));

    it('should open and close delete modal', () => {
      const version = mockVersions[0];
      component.openDeleteModal(version);

      expect(component.showDeleteModal).toBeTrue();
      expect(component.versionToDelete).toEqual(version);

      component.closeDeleteModal();
      expect(component.showDeleteModal).toBeFalse();
      expect(component.versionToDelete).toBeNull();
    });

    it('should assign user story to version', fakeAsync(() => {
      component.project = mockProject;
      component.versionToAssign = mockVersions[0];
      component.selectedUserStoryId = mockUserStories[0].id;

      component.assignUserStory();

      const req = httpMock.expectOne(`/api/versions/${mockVersions[0].id}/user-stories/${mockUserStories[0].id}`);
      expect(req.request.method).toBe('POST');
      req.flush({});

      // Reload versions
      const versionsReq = httpMock.expectOne('/api/versions/project/1');
      versionsReq.flush(mockVersions);

      // Reload available user stories
      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush([mockUserStories[1]]);

      tick();

      expect(component.showAssignModal).toBeFalse();
    }));

    it('should remove user story from version', fakeAsync(() => {
      component.project = mockProject;
      const version = mockVersions[0];
      const userStoryId = mockUserStories[0].id;

      component.removeUserStoryFromVersion(version, userStoryId);

      const req = httpMock.expectOne(`/api/versions/${version.id}/user-stories/${userStoryId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      // Reload versions
      const versionsReq = httpMock.expectOne('/api/versions/project/1');
      versionsReq.flush(mockVersions);

      // Reload available user stories
      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush(mockUserStories);

      tick();
    }));

    it('should open and close assign modal', () => {
      const version = mockVersions[0];
      component.openAssignModal(version);

      expect(component.showAssignModal).toBeTrue();
      expect(component.versionToAssign).toEqual(version);

      component.closeAssignModal();
      expect(component.showAssignModal).toBeFalse();
      expect(component.versionToAssign).toBeNull();
    });

    it('should toggle version details', fakeAsync(() => {
      component.versions = [...mockVersions];
      const versionId = mockVersions[0].id;

      component.toggleVersionDetails(versionId);

      const req = httpMock.expectOne(`/api/versions/${versionId}/user-stories`);
      req.flush([mockUserStories[0]]);

      tick();

      expect(component.expandedVersionId).toBe(versionId);
      expect(component.versions[0].userStories?.length).toBe(1);

      // Toggle again to collapse
      component.toggleVersionDetails(versionId);
      expect(component.expandedVersionId).toBeNull();
    }));

    it('should get selected version', () => {
      component.versions = [...mockVersions];
      component.expandedVersionId = mockVersions[0].id;

      const selected = component.getSelectedVersion();

      expect(selected).toEqual(mockVersions[0]);
    });

    it('should return null when no version is selected', () => {
      component.expandedVersionId = null;

      const selected = component.getSelectedVersion();

      expect(selected).toBeNull();
    });

    it('should quick assign user story', fakeAsync(() => {
      component.project = mockProject;
      component.versions = [...mockVersions];
      component.expandedVersionId = mockVersions[0].id;
      const userStoryId = mockUserStories[0].id;

      component.quickAssignUserStory(userStoryId);

      const req = httpMock.expectOne(`/api/versions/${mockVersions[0].id}/user-stories/${userStoryId}`);
      req.flush({});

      // Reload versions
      const versionsReq = httpMock.expectOne('/api/versions/project/1');
      versionsReq.flush(mockVersions);

      // Reload available user stories
      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush([mockUserStories[1]]);

      // Reload version user stories
      const versionUSReq = httpMock.expectOne(`/api/versions/${mockVersions[0].id}/user-stories`);
      versionUSReq.flush([mockUserStories[0]]);

      tick();
    }));

    it('should change version status', fakeAsync(() => {
      component.versions = [...mockVersions];
      const version = mockVersions[0];
      const newStatus = 'RELEASED';

      component.changeStatus(version, newStatus);

      const req = httpMock.expectOne(`/api/versions/${version.id}/status?status=${newStatus}`);
      expect(req.request.method).toBe('PUT');
      req.flush({ ...version, status: newStatus });

      tick();

      expect(component.versions[0].status).toBe(newStatus);
    }));

    it('should navigate back to project', () => {
      component.project = mockProject;
      component.goBack();

      expect(router.navigate).toHaveBeenCalledWith(['/projects', mockProject.id]);
    });

    it('should navigate to projects list when no project', () => {
      component.project = null;
      component.goBack();

      expect(router.navigate).toHaveBeenCalledWith(['/projects']);
    });

    it('should return correct status label', () => {
      expect(component.getStatusLabel('PLANNED')).toBe('Planifiée');
      expect(component.getStatusLabel('IN_PROGRESS')).toBe('En cours');
      expect(component.getStatusLabel('RELEASED')).toBe('Publiée');
      expect(component.getStatusLabel('ARCHIVED')).toBe('Archivée');
    });

    it('should return correct status class', () => {
      expect(component.getStatusClass('PLANNED')).toBe('status-planned');
      expect(component.getStatusClass('IN_PROGRESS')).toBe('status-in-progress');
      expect(component.getStatusClass('RELEASED')).toBe('status-released');
      expect(component.getStatusClass('ARCHIVED')).toBe('status-archived');
    });

    it('should format date correctly', () => {
      const date = '2024-12-31';
      const formatted = component.formatDate(date);

      expect(formatted).toContain('31');
      expect(formatted).toContain('12');
      expect(formatted).toContain('2024');
    });

    it('should return empty string for empty date', () => {
      expect(component.formatDate('')).toBe('');
    });

    it('should handle error when loading versions', fakeAsync(() => {
      fixture.detectChanges();

      const projectReq = httpMock.expectOne('/api/projects/1');
      projectReq.flush(mockProject);

      const versionsReq = httpMock.expectOne('/api/versions/project/1');
      versionsReq.error(new ProgressEvent('error'), { status: 500 });

      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush([]);

      tick();

      expect(component.error).toBeTruthy();
      expect(component.loading).toBeFalse();
    }));

    it('should handle error when creating version', fakeAsync(() => {
      component.versionForm = {
        title: 'New Version',
        versionNumber: '3.0.0',
        description: 'New',
        projectId: 1
      };
      component.isEditMode = false;

      component.saveVersion();

      const req = httpMock.expectOne('/api/versions');
      req.error(new ProgressEvent('error'), { status: 400 });

      tick();

      expect(component.versionError).toBeTruthy();
    }));

    it('should handle error when deleting version', fakeAsync(() => {
      component.versionToDelete = mockVersions[0];

      component.confirmDelete();

      const req = httpMock.expectOne(`/api/versions/${mockVersions[0].id}`);
      req.error(new ProgressEvent('error'), { status: 500 });

      tick();

      expect(component.deleteError).toBeTruthy();
    }));

    it('should load available user stories filtering out those with versions', fakeAsync(() => {
      const userStoriesWithVersion = [
        { ...mockUserStories[0], version: mockVersions[0] },
        mockUserStories[1]
      ];

      fixture.detectChanges();

      const projectReq = httpMock.expectOne('/api/projects/1');
      projectReq.flush(mockProject);

      const versionsReq = httpMock.expectOne('/api/versions/project/1');
      versionsReq.flush(mockVersions);

      const userStoriesReq = httpMock.expectOne('/api/user-stories/project/1');
      userStoriesReq.flush(userStoriesWithVersion);

      tick();

      expect(component.availableUserStories.length).toBe(1);
      expect(component.availableUserStories[0].id).toBe(mockUserStories[1].id);
    }));
=======

/**
 * Tests unitaires pour le composant VersionManagementComponent.
 * Couvre la gestion des versions : création, édition, suppression, assignation de user stories.
 */
describe('VersionManagementComponent', () => {
  let component: VersionManagementComponent;
  let fixture: ComponentFixture<VersionManagementComponent>;
  let versionService: jasmine.SpyObj<VersionService>;
  let userStoryService: jasmine.SpyObj<UserStoryService>;
  let projectService: jasmine.SpyObj<ProjectService>;
  let router: Router;

  const mockProject: Project = {
    id: 1,
    name: 'Test Project',
    description: 'Test Description',
    owner: { username: 'testuser' },
    members: []
  };

  const mockVersions: Version[] = [
    {
      id: 1,
      title: 'Version 1.0',
      description: 'First version',
      versionNumber: '1.0.0',
      status: 'PLANNED',
      userStories: []
    },
    {
      id: 2,
      title: 'Version 2.0',
      description: 'Second version',
      versionNumber: '2.0.0',
      status: 'IN_PROGRESS',
      userStories: []
    }
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
    const versionServiceSpy = jasmine.createSpyObj('VersionService', [
      'getByProject',
      'create',
      'update',
      'delete',
      'assignUserStory',
      'removeUserStory'
    ]);
    const userStoryServiceSpy = jasmine.createSpyObj('UserStoryService', ['getByProject', 'update']);
    const projectServiceSpy = jasmine.createSpyObj('ProjectService', ['getById']);

    await TestBed.configureTestingModule({
      imports: [VersionManagementComponent, HttpClientTestingModule, FormsModule],
      providers: [
        { provide: VersionService, useValue: versionServiceSpy },
        { provide: UserStoryService, useValue: userStoryServiceSpy },
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

    versionService = TestBed.inject(VersionService) as jasmine.SpyObj<VersionService>;
    userStoryService = TestBed.inject(UserStoryService) as jasmine.SpyObj<UserStoryService>;
    projectService = TestBed.inject(ProjectService) as jasmine.SpyObj<ProjectService>;
    router = TestBed.inject(Router);

    versionService.getByProject.and.returnValue(of(mockVersions));
    userStoryService.getByProject.and.returnValue(of(mockUserStories));
    projectService.getById.and.returnValue(of(mockProject));

    fixture = TestBed.createComponent(VersionManagementComponent);
    component = fixture.componentInstance;
    versionService.getUserStories = jasmine.createSpy('getUserStories').and.returnValue(of(mockUserStories));
    localStorage.setItem('token', 'test-token');
    spyOn(router, 'navigate').and.stub();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open create version modal', () => {
    component.openCreateVersionModal();
    expect(component.showVersionModal).toBeTrue();
    expect(component.isEditMode).toBeFalse();
    expect(component.versionForm.title).toBe('');
  });

  it('should close version modal', () => {
    component.showVersionModal = true;
    component.closeVersionModal();
    expect(component.showVersionModal).toBeFalse();
  });

  it('should create version successfully', () => {
    component.versionForm = {
      title: 'Version 1.0',
      description: 'First version',
      versionNumber: '1.0.0',
      projectId: 1
    };

    const newVersion: Version = {
      id: 3,
      title: 'Version 1.0',
      description: 'First version',
      versionNumber: '1.0.0',
      status: 'PLANNED',
      userStories: []
    };

    versionService.create.and.returnValue(of(newVersion));
    versionService.getByProject.and.returnValue(of([...mockVersions, newVersion]));

    component.saveVersion();

    expect(versionService.create).toHaveBeenCalledWith({
      title: 'Version 1.0',
      description: 'First version',
      versionNumber: '1.0.0',
      projectId: 1
    });
    expect(component.showVersionModal).toBeFalse();
  });

  it('should open edit version modal', () => {
    component.openEditVersionModal(mockVersions[0]);
    expect(component.showVersionModal).toBeTrue();
    expect(component.isEditMode).toBeTrue();
    expect(component.currentVersion).toEqual(mockVersions[0]);
    expect(component.versionForm.title).toBe('Version 1.0');
  });

  it('should update version successfully', () => {
    component.isEditMode = true;
    component.currentVersion = mockVersions[0];
    component.versionForm = {
      title: 'Updated Version',
      description: 'Updated description',
      versionNumber: '1.0.1',
      projectId: 1
    };

    const updatedVersion = { ...mockVersions[0], title: 'Updated Version' };
    versionService.update.and.returnValue(of(updatedVersion));
    versionService.getByProject.and.returnValue(of([updatedVersion, mockVersions[1]]));

    component.saveVersion();

    expect(versionService.update).toHaveBeenCalledWith(1, {
      title: 'Updated Version',
      description: 'Updated description',
      versionNumber: '1.0.1',
      projectId: 1
    });
    expect(component.showVersionModal).toBeFalse();
  });

  it('should open delete confirmation modal', () => {
    component.openDeleteModal(mockVersions[0]);
    expect(component.showDeleteModal).toBeTrue();
    expect(component.versionToDelete).toEqual(mockVersions[0]);
  });

  it('should delete version successfully', () => {
    component.versionToDelete = mockVersions[0];
    versionService.delete.and.returnValue(of(void 0));
    versionService.getByProject.and.returnValue(of([mockVersions[1]]));

    component.confirmDelete();

    expect(versionService.delete).toHaveBeenCalledWith(1);
    expect(component.showDeleteModal).toBeFalse();
  });

  it('should open assign modal', () => {
    component.openAssignModal(mockVersions[0]);
    expect(component.showAssignModal).toBeTrue();
    expect(component.versionToAssign).toEqual(mockVersions[0]);
  });

  it('should assign user story to version', () => {
    component.versionToAssign = mockVersions[0];
    component.selectedUserStoryId = 1;
    const userStory = mockUserStories[0];
    
    versionService.assignUserStory.and.returnValue(of(userStory as any));
    versionService.getByProject.and.returnValue(of(mockVersions));
    userStoryService.getByProject.and.returnValue(of([]));

    component.assignUserStory();

    expect(versionService.assignUserStory).toHaveBeenCalledWith(1, 1);
  });

  it('should remove user story from version', () => {
    const version = mockVersions[0];
    const userStory = { ...mockUserStories[0], version: { id: 1 } } as any;
    
    versionService.removeUserStory.and.returnValue(of(userStory));
    versionService.getByProject.and.returnValue(of(mockVersions));
    userStoryService.getByProject.and.returnValue(of(mockUserStories));

    component.removeUserStoryFromVersion(version, userStory.id);

    expect(versionService.removeUserStory).toHaveBeenCalledWith(version.id, userStory.id);
  });

  it('should toggle version expansion', () => {
    if (versionService.getUserStories) {
      (versionService.getUserStories as jasmine.Spy).and.returnValue(of(mockUserStories));
    }
    
    component.expandedVersionId = null;
    component.toggleVersionDetails(1);
    expect(component.expandedVersionId).not.toBeNull();

    component.toggleVersionDetails(1);
    expect(component.expandedVersionId).toBeNull();
  });

  it('should navigate back to kanban', () => {
    component.project = mockProject;
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/projects', 1]);
>>>>>>> 96d52c342137394fae52ee0c36810e37ab529d5c
  });
});
