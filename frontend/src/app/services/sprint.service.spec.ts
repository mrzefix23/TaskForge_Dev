import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SprintService, Sprint, CreateSprintRequest, UserStory } from './sprint.service';

describe('SprintService', () => {
  let service: SprintService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SprintService]
    });
    service = TestBed.inject(SprintService);
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

  it('should get sprints by project', () => {
    const mockSprints: Sprint[] = [
      {
        id: 1,
        name: 'Sprint 1',
        startDate: '2024-01-01',
        endDate: '2024-01-14',
        status: 'ACTIVE',
        projectId: 1
      }
    ];

    service.getSprintsByProject(1).subscribe(sprints => {
      expect(sprints).toEqual(mockSprints);
    });

    const req = httpMock.expectOne('/api/sprints/project/1');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockSprints);
  });

  it('should get sprint by id', () => {
    const mockSprint: Sprint = {
      id: 1,
      name: 'Sprint 1',
      startDate: '2024-01-01',
      endDate: '2024-01-14',
      status: 'ACTIVE',
      projectId: 1
    };

    service.getSprintById(1).subscribe(sprint => {
      expect(sprint).toEqual(mockSprint);
    });

    const req = httpMock.expectOne('/api/sprints/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockSprint);
  });

  it('should create a sprint', () => {
    const createRequest: CreateSprintRequest = {
      name: 'New Sprint',
      startDate: '2024-02-01',
      endDate: '2024-02-14',
      status: 'PLANNED',
      projectId: 1
    };

    const mockSprint: Sprint = { id: 2, ...createRequest };

    service.createSprint(createRequest).subscribe(sprint => {
      expect(sprint).toEqual(mockSprint);
    });

    const req = httpMock.expectOne('/api/sprints');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(createRequest);
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    req.flush(mockSprint);
  });

  it('should update a sprint', () => {
    const updateRequest: CreateSprintRequest = {
      name: 'Updated Sprint',
      startDate: '2024-02-01',
      endDate: '2024-02-14',
      status: 'ACTIVE',
      projectId: 1
    };

    const mockSprint: Sprint = { id: 1, ...updateRequest };

    service.updateSprint(1, updateRequest).subscribe(sprint => {
      expect(sprint).toEqual(mockSprint);
    });

    const req = httpMock.expectOne('/api/sprints/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateRequest);
    req.flush(mockSprint);
  });

  it('should delete a sprint', () => {
    service.deleteSprint(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne('/api/sprints/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should assign user story to sprint', () => {
    const mockUserStory: UserStory = {
      id: 1,
      title: 'Test Story',
      description: 'Test Description',
      priority: 'HIGH',
      status: 'TODO',
      sprint: {
        id: 1,
        name: 'Sprint 1',
        startDate: '2024-01-01',
        endDate: '2024-01-14',
        status: 'ACTIVE',
        projectId: 1
      }
    };

    service.assignUserStoryToSprint(1, 1).subscribe(story => {
      expect(story).toEqual(mockUserStory);
    });

    const req = httpMock.expectOne('/api/sprints/1/user-stories/1');
    expect(req.request.method).toBe('POST');
    req.flush(mockUserStory);
  });

  it('should remove user story from sprint', () => {
    const mockUserStory: UserStory = {
      id: 1,
      title: 'Test Story',
      description: 'Test Description',
      priority: 'HIGH',
      status: 'TODO'
    };

    service.removeUserStoryFromSprint(1).subscribe(story => {
      expect(story).toEqual(mockUserStory);
    });

    const req = httpMock.expectOne('/api/sprints/user-stories/1/sprint');
    expect(req.request.method).toBe('DELETE');
    req.flush(mockUserStory);
  });

  it('should get user stories by sprint', () => {
    const mockUserStories: UserStory[] = [
      {
        id: 1,
        title: 'Story 1',
        description: 'Description 1',
        priority: 'HIGH',
        status: 'TODO'
      }
    ];

    service.getUserStoriesBySprint(1).subscribe(stories => {
      expect(stories).toEqual(mockUserStories);
    });

    const req = httpMock.expectOne('/api/sprints/1/user-stories');
    expect(req.request.method).toBe('GET');
    req.flush(mockUserStories);
  });

  it('should get backlog user stories', () => {
    const mockUserStories: UserStory[] = [
      {
        id: 2,
        title: 'Backlog Story',
        description: 'Description',
        priority: 'MEDIUM',
        status: 'TODO'
      }
    ];

    service.getBacklogUserStories(1).subscribe(stories => {
      expect(stories).toEqual(mockUserStories);
    });

    const req = httpMock.expectOne('/api/sprints/project/1/backlog');
    expect(req.request.method).toBe('GET');
    req.flush(mockUserStories);
  });
});
