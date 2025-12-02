import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { VersionManagementComponent } from './version-management';
import { VersionService } from '../../services/version.service';
import { UserStoryService } from '../../services/user-story.service';
import { ProjectService } from '../../services/project.service';
import { Version, UserStory, Project } from '../../models/kanban.models';

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
  });
});
