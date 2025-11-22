import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { Kanban } from './components/kanban/kanban';
import { AccueilComponent } from './components/accueil/accueil';
import { AuthGuard } from './auth.guard';
import { NotFoundComponent } from './components/not-found/not-found';
import { CreateProjectComponent } from './components/projects/create-project';
import { ProjectsList } from './components/projects-list/projects-list';

export const routes: Routes = [
  { path: '', component: AuthComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'kanban', component: Kanban },
  { path: 'accueil', component: AccueilComponent, canActivate: [AuthGuard] },
  { path: 'projects/create', component: CreateProjectComponent, canActivate: [AuthGuard] },
  { path: 'projects', component: ProjectsList, canActivate: [AuthGuard] },
  { path: 'error', component: NotFoundComponent }
];