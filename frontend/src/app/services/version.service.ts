import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Version, UserStory } from '../models/kanban.models';

export interface CreateVersionRequest {
  title: string;
  description: string;
  versionNumber: string;
  projectId: number;
}

@Injectable({
  providedIn: 'root'
})
export class VersionService {
  private apiUrl = '/api/versions';

  constructor(private http: HttpClient) {}

  getByProject(projectId: number): Observable<Version[]> {
    return this.http.get<Version[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getById(id: number): Observable<Version> {
    return this.http.get<Version>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateVersionRequest): Observable<Version> {
    return this.http.post<Version>(this.apiUrl, request);
  }

  update(id: number, request: CreateVersionRequest): Observable<Version> {
    return this.http.put<Version>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(id: number, status: string): Observable<Version> {
    return this.http.put<Version>(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  assignUserStory(versionId: number, userStoryId: number): Observable<UserStory> {
    return this.http.post<UserStory>(`${this.apiUrl}/${versionId}/user-stories/${userStoryId}`, {});
  }

  removeUserStory(versionId: number, userStoryId: number): Observable<UserStory> {
    return this.http.delete<UserStory>(`${this.apiUrl}/${versionId}/user-stories/${userStoryId}`);
  }

  getUserStories(versionId: number): Observable<UserStory[]> {
    return this.http.get<UserStory[]>(`${this.apiUrl}/${versionId}/user-stories`);
  }
}