import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { AccueilComponent } from './components/accueil/accueil';
import { AuthGuard } from './auth.guard';
import { NotFoundComponent } from './components/not-found/not-found';

export const routes: Routes = [
  { path: '', component: AuthComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'accueil', component: AccueilComponent, canActivate: [AuthGuard] },
  { path: 'error', component: NotFoundComponent }
];