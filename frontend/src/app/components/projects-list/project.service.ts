import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project } from './project.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = '/api/projects'; 

  constructor(private http: HttpClient) {}

  getMyProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }
}