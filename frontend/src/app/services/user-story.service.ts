import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserStory } from '../models/kanban.models';

@Injectable({
  providedIn: 'root'
})
export class UserStoryService {
  private readonly API_URL = '/api/user-stories';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getByProject(projectId: number): Observable<UserStory[]> {
    return this.http.get<UserStory[]>(`${this.API_URL}/project/${projectId}`, {
      headers: this.getHeaders()
    });
  }

  create(userStory: Partial<UserStory>): Observable<UserStory> {
    return this.http.post<UserStory>(this.API_URL, userStory, {
      headers: this.getHeaders()
    });
  }

  update(id: number, userStory: Partial<UserStory>): Observable<UserStory> {
    return this.http.put<UserStory>(`${this.API_URL}/${id}`, userStory, {
      headers: this.getHeaders()
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`, {
      headers: this.getHeaders()
    });
  }
}
