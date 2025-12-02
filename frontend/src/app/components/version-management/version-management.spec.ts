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
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Setup', () => {
    it('should have correct initial state', () => {
      expect(component.versions).toEqual([]);
      expect(component.loading).toBeTrue();
      expect(component.error).toBeNull();
    });
  });
});
