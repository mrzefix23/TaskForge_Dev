import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project, Sprint } from '../models/kanban.models';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly API_URL = '/api/projects';
  private readonly SPRINT_API_URL = '/api/sprints';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getById(projectId: number): Observable<Project> {
    return this.http.get<Project>(`${this.API_URL}/${projectId}`, {
      headers: this.getHeaders()
    });
  }

  getSprintsByProject(projectId: number): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(`${this.SPRINT_API_URL}/project/${projectId}`, {
      headers: this.getHeaders()
    });
  }
}
