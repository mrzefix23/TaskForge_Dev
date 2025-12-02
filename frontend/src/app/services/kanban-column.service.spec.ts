import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { KanbanColumnService } from './kanban-column.service';
import { KanbanColumn } from '../models/kanban.models';

/**
 * Tests unitaires pour KanbanColumnService.
 * Couvre les opérations CRUD sur les colonnes Kanban.
 */
describe('KanbanColumnService', () => {
  let service: KanbanColumnService;
  let httpMock: HttpTestingController;
  const apiUrl = '/api/kanban-columns';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [KanbanColumnService]
    });

    service = TestBed.inject(KanbanColumnService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getByProject', () => {
    it('should fetch columns for a project', () => {
      const mockColumns: KanbanColumn[] = [
        { id: 1, name: 'To Do', status: 'TODO', order: 1, projectId: 1, isDefault: true },
        { id: 2, name: 'In Progress', status: 'IN_PROGRESS', order: 2, projectId: 1, isDefault: true }
      ];

      service.getByProject(1).subscribe(columns => {
        expect(columns.length).toBe(2);
        expect(columns).toEqual(mockColumns);
      });

      const req = httpMock.expectOne(`${apiUrl}/project/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockColumns);
    });
  });

  describe('create', () => {
    it('should create a new column', () => {
      const newColumn = { name: 'Review', status: 'REVIEW', order: 3, projectId: 1 };
      const createdColumn: KanbanColumn = { id: 3, ...newColumn, isDefault: false };

      service.create(newColumn).subscribe(column => {
        expect(column).toEqual(createdColumn);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newColumn);
      req.flush(createdColumn);
    });
  });

  describe('update', () => {
    it('should update a column', () => {
      const updatedColumn: KanbanColumn = {
        id: 1,
        name: 'Updated Name',
        status: 'TODO',
        order: 1,
        projectId: 1,
        isDefault: false
      };

      service.update(1, updatedColumn).subscribe(column => {
        expect(column).toEqual(updatedColumn);
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updatedColumn);
      req.flush(updatedColumn);
    });
  });
});
