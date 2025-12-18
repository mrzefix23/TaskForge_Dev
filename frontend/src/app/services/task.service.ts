import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task } from '../models/kanban.models';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private readonly API_URL = 'https://taskforge-dev.onrender.com/api/tasks';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getByUserStory(userStoryId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.API_URL}/user-story/${userStoryId}`, {
      headers: this.getHeaders()
    });
  }

  create(task: Partial<Task> & { userStoryId: number }): Observable<Task> {
    return this.http.post<Task>(this.API_URL, task, {
      headers: this.getHeaders()
    });
  }

  update(id: number, task: Partial<Task> & { userStoryId: number }): Observable<Task> {
    return this.http.put<Task>(`${this.API_URL}/${id}`, task, {
      headers: this.getHeaders()
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`, {
      headers: this.getHeaders()
    });
  }
}
