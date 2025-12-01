import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Version, UserStory } from '../models/kanban.models';

export interface CreateVersionRequest {
    title: string;
    description: string;
    versionNumber: string;
    projectId: number;
}

@Injectable({ providedIn: 'root' })
export class VersionService {
    private apiUrl = '/api/versions';

    constructor(private http: HttpClient) { }

    private getAuthHeaders(): { headers: HttpHeaders } {
        const token = localStorage.getItem('token');
        return {
            headers: new HttpHeaders({
                'Authorization': `Bearer ${token}`
            })
        };
    }

    getByProject(projectId: number): Observable<Version[]> {
        return this.http.get<Version[]>(`${this.apiUrl}/project/${projectId}`, this.getAuthHeaders());
    }

    getById(id: number): Observable<Version> {
        return this.http.get<Version>(`${this.apiUrl}/${id}`, this.getAuthHeaders());
    }

    create(request: CreateVersionRequest): Observable<Version> {
        return this.http.post<Version>(this.apiUrl, request, this.getAuthHeaders());
    }

    update(id: number, request: CreateVersionRequest): Observable<Version> {
        return this.http.put<Version>(`${this.apiUrl}/${id}`, request, this.getAuthHeaders());
    }

    updateStatus(id: number, status: string): Observable<Version> {
        return this.http.put<Version>(`${this.apiUrl}/${id}/status?status=${status}`, {}, this.getAuthHeaders());
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`, this.getAuthHeaders());
    }

    assignUserStory(versionId: number, userStoryId: number): Observable<UserStory> {
        return this.http.post<UserStory>(`${this.apiUrl}/${versionId}/user-stories/${userStoryId}`, {}, this.getAuthHeaders());
    }

    removeUserStory(versionId: number, userStoryId: number): Observable<UserStory> {
        return this.http.delete<UserStory>(`${this.apiUrl}/${versionId}/user-stories/${userStoryId}`, this.getAuthHeaders());
    }

    getUserStories(versionId: number): Observable<UserStory[]> {
        return this.http.get<UserStory[]>(`${this.apiUrl}/${versionId}/user-stories`, this.getAuthHeaders());
    }

}