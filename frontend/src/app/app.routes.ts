import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { AccueilComponent } from './components/accueil/accueil';
import { AuthGuard } from './auth.guard';
import { NotFoundComponent } from './components/not-found/not-found';
import { CreateProjectComponent } from './components/projects/create-project/create-project';
import { ProjectsListComponent } from './components/projects/projects-list/projects-list';
import { EditProjectComponent } from './components/projects/edit-project/edit-project';

export const routes: Routes = [
  { path: '', component: AuthComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'accueil', component: AccueilComponent, canActivate: [AuthGuard] },
  { path: 'projects/create', component: CreateProjectComponent, canActivate: [AuthGuard] },
  { path: 'projects/edit/:id', component: EditProjectComponent, canActivate: [AuthGuard] },
  { path: 'myprojects', component: ProjectsListComponent, canActivate: [AuthGuard] },
  { path: 'error', component: NotFoundComponent }
];