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
import { KanbanComponent } from './components/kanban/kanban';
import { SprintManagementComponent } from './components/sprint-management/sprint-management';

/**
 * Définition des routes de l'application.
 * Associe les chemins URL (paths) aux composants correspondants.
 */
export const routes: Routes = [
  // --- Routes Publiques ---
  { path: '', component: AuthComponent }, // Page d'atterrissage (Landing)
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },

  // --- Routes Protégées (Nécessitent une authentification via AuthGuard) ---
  { path: 'accueil', component: AccueilComponent, canActivate: [AuthGuard] },
  
  // --- Gestion des Projets ---
  { path: 'projects', component: ProjectsListComponent, canActivate: [AuthGuard] },
  { path: 'projects/create', component: CreateProjectComponent, canActivate: [AuthGuard] },
  { path: 'projects/edit/:id', component: EditProjectComponent, canActivate: [AuthGuard] },
  
  // Vue détaillée d'un projet (Kanban)
  { path: 'projects/:id', component: KanbanComponent, canActivate: [AuthGuard] },

  // --- Gestion des erreurs ---
  { path: 'error', component: NotFoundComponent },
  
  // --- Gestion des sprints ---
  { path: 'projects/:id/sprints', component: SprintManagementComponent, canActivate: [AuthGuard] },
];