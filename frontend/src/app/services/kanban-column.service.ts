import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KanbanColumn } from '../models/kanban.models';

@Injectable({
  providedIn: 'root'
})
export class KanbanColumnService {
  private readonly API_URL = '/api/kanban-columns';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getByProject(projectId: number): Observable<KanbanColumn[]> {
    return this.http.get<KanbanColumn[]>(`${this.API_URL}/project/${projectId}`, {
      headers: this.getHeaders()
    });
  }

  create(column: Partial<KanbanColumn>): Observable<KanbanColumn> {
    return this.http.post<KanbanColumn>(this.API_URL, column, {
      headers: this.getHeaders()
    });
  }

  update(id: number, column: Partial<KanbanColumn>): Observable<KanbanColumn> {
    return this.http.put<KanbanColumn>(`${this.API_URL}/${id}`, column, {
      headers: this.getHeaders()
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`, {
      headers: this.getHeaders()
    });
  }
}
