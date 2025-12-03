import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Sprint {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED';
  projectId: number;
}

export interface CreateSprintRequest {
  name: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED';
  projectId: number;
}

export interface UserStory {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string }[];
  sprint?: Sprint;
}

@Injectable({
  providedIn: 'root'
})
export class SprintService {
  private apiUrl = '/api/sprints';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getSprintsByProject(projectId: number): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(`${this.apiUrl}/project/${projectId}`, {
      headers: this.getHeaders()
    });
  }

  getSprintById(sprintId: number): Observable<Sprint> {
    return this.http.get<Sprint>(`${this.apiUrl}/${sprintId}`, {
      headers: this.getHeaders()
    });
  }

  createSprint(request: CreateSprintRequest): Observable<Sprint> {
    return this.http.post<Sprint>(this.apiUrl, request, {
      headers: this.getHeaders()
    });
  }

  updateSprint(sprintId: number, request: CreateSprintRequest): Observable<Sprint> {
    return this.http.put<Sprint>(`${this.apiUrl}/${sprintId}`, request, {
      headers: this.getHeaders()
    });
  }

  deleteSprint(sprintId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${sprintId}`, {
      headers: this.getHeaders()
    });
  }

  assignUserStoryToSprint(sprintId: number, userStoryId: number): Observable<UserStory> {
    return this.http.post<UserStory>(`${this.apiUrl}/${sprintId}/user-stories/${userStoryId}`, {}, {
      headers: this.getHeaders()
    });
  }

  removeUserStoryFromSprint(userStoryId: number): Observable<UserStory> {
    return this.http.delete<UserStory>(`${this.apiUrl}/user-stories/${userStoryId}/sprint`, {
      headers: this.getHeaders()
    });
  }

  getUserStoriesBySprint(sprintId: number): Observable<UserStory[]> {
    return this.http.get<UserStory[]>(`${this.apiUrl}/${sprintId}/user-stories`, {
      headers: this.getHeaders()
    });
  }

  getBacklogUserStories(projectId: number): Observable<UserStory[]> {
    return this.http.get<UserStory[]>(`${this.apiUrl}/project/${projectId}/backlog`, {
      headers: this.getHeaders()
    });
  }

  startSprint(sprintId: number): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.apiUrl}/${sprintId}/start`, {}, {
      headers: this.getHeaders()
    });
  }

  completeSprint(sprintId: number): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.apiUrl}/${sprintId}/complete`, {}, {
      headers: this.getHeaders()
    });
  }
}
