import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserStoryService } from './user-story.service';
import { UserStory } from '../models/kanban.models';

describe('UserStoryService', () => {
  let service: UserStoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserStoryService]
    });
    service = TestBed.inject(UserStoryService);
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

  it('should get user stories by project', () => {
    const mockUserStories: UserStory[] = [
      {
        id: 1,
        title: 'User Story 1',
        description: 'Description 1',
        priority: 'HIGH',
        status: 'TODO'
      },
      {
        id: 2,
        title: 'User Story 2',
        description: 'Description 2',
        priority: 'MEDIUM',
        status: 'IN_PROGRESS'
      }
    ];

    service.getByProject(1).subscribe(stories => {
      expect(stories).toEqual(mockUserStories);
      expect(stories.length).toBe(2);
    });

    const req = httpMock.expectOne('/api/user-stories/project/1');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockUserStories);
  });

  it('should create a user story', () => {
    const newUserStory: Partial<UserStory> = {
      title: 'New Story',
      description: 'New Description',
      priority: 'HIGH',
      status: 'TODO'
    };

    const mockUserStory: UserStory = { id: 3, ...newUserStory } as UserStory;

    service.create(newUserStory).subscribe(story => {
      expect(story).toEqual(mockUserStory);
      expect(story.title).toBe('New Story');
    });

    const req = httpMock.expectOne('/api/user-stories');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newUserStory);
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockUserStory);
  });

  it('should update a user story', () => {
    const updateUserStory: Partial<UserStory> = {
      title: 'Updated Story',
      description: 'Updated Description',
      priority: 'LOW',
      status: 'DONE'
    };

    const mockUserStory: UserStory = { id: 1, ...updateUserStory } as UserStory;

    service.update(1, updateUserStory).subscribe(story => {
      expect(story).toEqual(mockUserStory);
      expect(story.status).toBe('DONE');
    });

    const req = httpMock.expectOne('/api/user-stories/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateUserStory);
    req.flush(mockUserStory);
  });

  it('should delete a user story', () => {
    service.delete(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne('/api/user-stories/1');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(null);
  });

  it('should handle error when creating user story', () => {
    const invalidUserStory: Partial<UserStory> = {
      title: '',
      description: '',
      priority: 'HIGH',
      status: 'TODO'
    };

    service.create(invalidUserStory).subscribe({
      next: () => fail('should have failed with 400 error'),
      error: (error) => {
        expect(error.status).toBe(400);
      }
    });

    const req = httpMock.expectOne('/api/user-stories');
    req.flush('Invalid user story', { status: 400, statusText: 'Bad Request' });
  });
});
