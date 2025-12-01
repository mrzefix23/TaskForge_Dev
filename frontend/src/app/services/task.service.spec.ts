import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service';
import { Task } from '../models/kanban.models';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService]
    });
    service = TestBed.inject(TaskService);
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

  it('should get tasks by user story', () => {
    const mockTasks: Task[] = [
      {
        id: 1,
        title: 'Task 1',
        description: 'Description 1',
        priority: 'HIGH',
        status: 'TODO',
        userStory: { id: 1 }
      },
      {
        id: 2,
        title: 'Task 2',
        description: 'Description 2',
        priority: 'MEDIUM',
        status: 'IN_PROGRESS',
        userStory: { id: 1 }
      }
    ];

    service.getByUserStory(1).subscribe(tasks => {
      expect(tasks).toEqual(mockTasks);
      expect(tasks.length).toBe(2);
    });

    const req = httpMock.expectOne('/api/tasks/user-story/1');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockTasks);
  });

  it('should create a task', () => {
    const newTask: Partial<Task> & { userStoryId: number } = {
      title: 'New Task',
      description: 'New Description',
      status: 'TODO',
      userStoryId: 1
    };

    const mockTask: Task = { 
      id: 3, 
      title: 'New Task',
      description: 'New Description',
      priority: 'MEDIUM',
      status: 'TODO',
      userStory: { id: 1 }
    };

    service.create(newTask).subscribe(task => {
      expect(task).toEqual(mockTask);
      expect(task.title).toBe('New Task');
    });

    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newTask);
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(mockTask);
  });

  it('should update a task', () => {
    const updateTask: Partial<Task> & { userStoryId: number } = {
      title: 'Updated Task',
      description: 'Updated Description',
      status: 'DONE',
      userStoryId: 1
    };

    const mockTask: Task = { 
      id: 1, 
      title: 'Updated Task',
      description: 'Updated Description',
      priority: 'HIGH',
      status: 'DONE',
      userStory: { id: 1 }
    };

    service.update(1, updateTask).subscribe(task => {
      expect(task).toEqual(mockTask);
      expect(task.status).toBe('DONE');
    });

    const req = httpMock.expectOne('/api/tasks/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateTask);
    req.flush(mockTask);
  });

  it('should delete a task', () => {
    service.delete(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne('/api/tasks/1');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    req.flush(null);
  });

  it('should handle error when getting tasks', () => {
    service.getByUserStory(999).subscribe({
      next: () => fail('should have failed with 404 error'),
      error: (error) => {
        expect(error.status).toBe(404);
      }
    });

    const req = httpMock.expectOne('/api/tasks/user-story/999');
    req.flush('User story not found', { status: 404, statusText: 'Not Found' });
  });
});
