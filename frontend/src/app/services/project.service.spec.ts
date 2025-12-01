import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProjectService } from './project.service';
import { Project, Sprint } from '../models/kanban.models';

describe('ProjectService', () => {
  let service: ProjectService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProjectService]
    });
    service = TestBed.inject(ProjectService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.setItem('token', 'test-token');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get project by id with authorization header', () => {
    const mockProject: Project = {
      id: 1,
      name: 'Test Project',
      description: 'Test Description',
      owner: { username: 'owner' },
      members: []
    };

    service.getById(1).subscribe(project => {
      expect(project).toEqual(mockProject);
    });

    const req = httpMock.expectOne('/api/projects/1');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockProject);
  });

  it('should get sprints by project with authorization header', () => {
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

    service.getSprintsByProject(1).subscribe(sprints => {
      expect(sprints).toEqual(mockSprints);
      expect(sprints.length).toBe(2);
    });

    const req = httpMock.expectOne('/api/sprints/project/1');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockSprints);
  });

  it('should handle error when getting project by id', () => {
    service.getById(999).subscribe({
      next: () => fail('should have failed with 404 error'),
      error: (error) => {
        expect(error.status).toBe(404);
      }
    });

    const req = httpMock.expectOne('/api/projects/999');
    req.flush('Project not found', { status: 404, statusText: 'Not Found' });
  });
});
