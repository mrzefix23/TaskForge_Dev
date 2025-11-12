import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { Kanban } from './components/kanban/kanban';

export const routes: Routes = [
  { path: '', component: AuthComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'kanban', component: Kanban }
];