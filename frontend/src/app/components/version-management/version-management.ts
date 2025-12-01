import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../header/header';
import { VersionService, CreateVersionRequest } from '../../services/version.service';
import { UserStoryService } from '../../services/user-story.service';
import { ProjectService } from '../../services/project.service';
import { Version, UserStory, Project } from '../../models/kanban.models';

/**
 * Composant de gestion des versions (Version Management).
 * Permet de créer, modifier, supprimer des versions de livraison
 * et d'assigner des User Stories à ces versions.
 */
@Component({
  selector: 'app-version-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent],
  templateUrl: './version-management.html',
  styleUrls: ['./version-management.css']
})
export class VersionManagementComponent implements OnInit {

  /** Projet actuellement sélectionné. */
  project: Project | null = null;

  /** Liste des versions du projet. */
  versions: Version[] = [];

  /** Liste des User Stories disponibles (non assignées à une version). */
  availableUserStories: UserStory[] = [];

  /** Indicateur de chargement des données. */
  loading = true;

  /** Message d'erreur général affiché à l'utilisateur. */
  error: string | null = null;

  /** Bascule pour afficher/masquer la modal de création/édition. */
  showVersionModal = false;

  /** Indique si on est en mode édition (true) ou création (false). */
  isEditMode = false;

  /** Version actuellement en cours d'édition. */
  currentVersion: Version | null = null;

  /** Données du formulaire de création/édition de version. */
  versionForm: CreateVersionRequest = {
    title: '',
    description: '',
    versionNumber: '',
    projectId: 0
  };

  /** Message d'erreur spécifique au formulaire de version. */
  versionError: string | null = null;

  /** Bascule pour afficher/masquer la modal de confirmation de suppression. */
  showDeleteModal = false;

  /** Version sélectionnée pour suppression. */
  versionToDelete: Version | null = null;

  /** Message d'erreur lors de la suppression. */
  deleteError: string | null = null;

  /** Bascule pour afficher/masquer la modal d'assignation. */
  showAssignModal = false;

  /** Version cible pour l'assignation d'une User Story. */
  versionToAssign: Version | null = null;

  /** ID de la User Story sélectionnée pour assignation. */
  selectedUserStoryId: number | null = null;

  /** ID de la version actuellement expandée pour afficher ses détails. */
  expandedVersionId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private versionService: VersionService,
    private userStoryService: UserStoryService,
    private projectService: ProjectService
  ) {}

  /**
   * Initialisation du composant.
   * Récupère l'ID du projet depuis l'URL et charge les données associées.
   */
  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.versionForm.projectId = +projectId;
      this.loadProject(+projectId);
      this.loadVersions(+projectId);
      this.loadAvailableUserStories(+projectId);
    } else {
      this.error = "ID de projet non trouvé.";
      this.loading = false;
    }
  }

  /**
   * Charge les informations du projet depuis l'API.
   * @param projectId - ID du projet à charger
   */
  loadProject(projectId: number): void {
    this.projectService.getById(projectId).subscribe({
      next: (data) => {
        this.project = data;
      },
      error: (err) => {
        console.error('Erreur chargement projet:', err);
      }
    });
  }

  /**
   * Charge la liste des versions du projet depuis l'API.
   * @param projectId - ID du projet
   */
  loadVersions(projectId: number): void {
    this.versionService.getByProject(projectId).subscribe({
      next: (data) => {
        this.versions = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des versions.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  /**
   * Charge les User Stories disponibles (non assignées à une version).
   * @param projectId - ID du projet
   */
  loadAvailableUserStories(projectId: number): void {
    this.userStoryService.getByProject(projectId).subscribe({
      next: (data) => {
        this.availableUserStories = data.filter(us => !us.version);
      },
      error: (err) => {
        console.error('Erreur chargement user stories:', err);
      }
    });
  }

  /**
   * Retourne à la page de détail du projet.
   */
  goBack(): void {
    if (this.project) {
      this.router.navigate(['/projects', this.project.id]);
    } else {
      this.router.navigate(['/projects']);
    }
  }

  /**
   * Ouvre la modal en mode création d'une nouvelle version.
   * Réinitialise le formulaire avec des valeurs vides.
   */
  openCreateVersionModal(): void {
    this.isEditMode = false;
    this.currentVersion = null;
    this.versionForm = {
      title: '',
      description: '',
      versionNumber: '',
      projectId: this.project?.id || 0
    };
    this.versionError = null;
    this.showVersionModal = true;
  }

  /**
   * Ouvre la modal en mode édition d'une version existante.
   * Pré-remplit le formulaire avec les données de la version.
   * @param version - Version à modifier
   */
  openEditVersionModal(version: Version): void {
    this.isEditMode = true;
    this.currentVersion = version;
    this.versionForm = {
      title: version.title,
      description: version.description,
      versionNumber: version.versionNumber,
      projectId: this.project?.id || 0
    };
    this.versionError = null;
    this.showVersionModal = true;
  }

  /**
   * Ferme la modal de création/édition et réinitialise les états.
   */
  closeVersionModal(): void {
    this.showVersionModal = false;
    this.currentVersion = null;
    this.versionError = null;
  }

  /**
   * Soumet le formulaire de création ou modification de version.
   * Valide les champs requis avant l'envoi à l'API.
   */
  saveVersion(): void {
    if (!this.versionForm.title || !this.versionForm.versionNumber) {
      this.versionError = 'Veuillez remplir le titre et le numéro de version.';
      return;
    }

    this.versionError = null;

    if (this.isEditMode && this.currentVersion) {
      this.versionService.update(this.currentVersion.id, this.versionForm).subscribe({
        next: (updated) => {
          const index = this.versions.findIndex(v => v.id === updated.id);
          if (index !== -1) {
            this.versions[index] = updated;
          }
          this.closeVersionModal();
        },
        error: (err) => {
          this.versionError = err.error?.message || 'Erreur lors de la mise à jour.';
          console.error(err);
        }
      });
    } else {
      this.versionService.create(this.versionForm).subscribe({
        next: (created) => {
          this.versions.unshift(created);
          this.closeVersionModal();
        },
        error: (err) => {
          this.versionError = err.error?.message || 'Erreur lors de la création.';
          console.error(err);
        }
      });
    }
  }

  /**
   * Ouvre la modal de confirmation de suppression.
   * @param version - Version à supprimer
   */
  openDeleteModal(version: Version): void {
    this.versionToDelete = version;
    this.deleteError = null;
    this.showDeleteModal = true;
  }

  /**
   * Ferme la modal de suppression et réinitialise les états.
   */
  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.versionToDelete = null;
    this.deleteError = null;
  }

  /**
   * Confirme et exécute la suppression de la version.
   * Les User Stories associées sont dissociées mais pas supprimées.
   */
  confirmDelete(): void {
    if (!this.versionToDelete) return;

    this.versionService.delete(this.versionToDelete.id).subscribe({
      next: () => {
        this.versions = this.versions.filter(v => v.id !== this.versionToDelete!.id);
        this.closeDeleteModal();
        
        if (this.project) {
          this.loadAvailableUserStories(this.project.id);
        }
      },
      error: (err) => {
        this.deleteError = 'Erreur lors de la suppression.';
        console.error(err);
      }
    });
  }


  /**
   * Change le statut d'une version.
   * Si le statut passe à RELEASED, la date de publication est automatiquement définie.
   * @param version - Version concernée
   * @param newStatus - Nouveau statut à appliquer
   */
  changeStatus(version: Version, newStatus: string): void {
    this.versionService.updateStatus(version.id, newStatus).subscribe({
      next: (updated) => {
        const index = this.versions.findIndex(v => v.id === updated.id);
        if (index !== -1) {
          this.versions[index] = updated;
        }
      },
      error: (err) => {
        console.error('Erreur changement statut:', err);
      }
    });
  }

  /**
   * Ouvre la modal d'assignation d'une User Story à une version.
   * @param version - Version cible pour l'assignation
   */
  openAssignModal(version: Version): void {
    this.versionToAssign = version;
    this.selectedUserStoryId = null;
    this.showAssignModal = true;
  }

  /**
   * Ferme la modal d'assignation et réinitialise les états.
   */
  closeAssignModal(): void {
    this.showAssignModal = false;
    this.versionToAssign = null;
    this.selectedUserStoryId = null;
  }

  /**
   * Assigne la User Story sélectionnée à la version cible.
   * Met à jour les listes après l'assignation.
   */
  assignUserStory(): void {
    if (!this.versionToAssign || !this.selectedUserStoryId) return;

    this.versionService.assignUserStory(this.versionToAssign.id, this.selectedUserStoryId).subscribe({
      next: () => {
        if (this.project) {
          this.loadVersions(this.project.id);
          this.loadAvailableUserStories(this.project.id);
        }
        this.closeAssignModal();
      },
      error: (err) => {
        console.error('Erreur assignation:', err);
      }
    });
  }

  /**
   * Retire une User Story d'une version (la dissocie).
   * La User Story redevient disponible pour d'autres versions.
   * @param version - Version source
   * @param userStoryId - ID de la User Story à retirer
   */
  removeUserStoryFromVersion(version: Version, userStoryId: number): void {
    this.versionService.removeUserStory(version.id, userStoryId).subscribe({
      next: () => {
        if (this.project) {
          this.loadVersions(this.project.id);
          this.loadAvailableUserStories(this.project.id);
        }
      },
      error: (err) => {
        console.error('Erreur suppression US:', err);
      }
    });
  }

  /**
   * Bascule l'affichage des détails d'une version.
   * Charge les User Stories associées lors de l'expansion.
   * @param versionId - ID de la version à afficher/masquer
   */
  toggleVersionDetails(versionId: number): void {
    if (this.expandedVersionId === versionId) {
      this.expandedVersionId = null;
    } else {
      this.expandedVersionId = versionId;
      this.versionService.getUserStories(versionId).subscribe({
        next: (userStories) => {
          const version = this.versions.find(v => v.id === versionId);
          if (version) {
            version.userStories = userStories;
          }
        },
        error: (err) => {
          console.error('Erreur chargement US version:', err);
        }
      });
    }
  }

  /**
   * Retourne la version actuellement sélectionnée/expandée.
   * @returns La version sélectionnée ou null
   */
  getSelectedVersion(): Version | null {
    if (!this.expandedVersionId) return null;
    return this.versions.find(v => v.id === this.expandedVersionId) || null;
  }

  /**
   * Assigne rapidement une User Story à la version actuellement sélectionnée.
   * Utilisé depuis le panneau des US disponibles.
   * @param userStoryId - ID de la User Story à assigner
   */
  quickAssignUserStory(userStoryId: number): void {
    if (!this.expandedVersionId) return;

    this.versionService.assignUserStory(this.expandedVersionId, userStoryId).subscribe({
      next: () => {
        if (this.project) {
          this.loadVersions(this.project.id);
          this.loadAvailableUserStories(this.project.id);
        }

        // Recharger les US de la version sélectionnée
        if (this.expandedVersionId) {
          this.versionService.getUserStories(this.expandedVersionId).subscribe({
            next: (userStories) => {
              const version = this.versions.find(v => v.id === this.expandedVersionId);
              if (version) {
                version.userStories = userStories;
              }
            }
          });
        }
      },
      error: (err) => {
        console.error('Erreur assignation:', err);
      }
    });
  }

  /**
   * Retourne le libellé français d'un statut de version.
   * @param status - Statut en anglais (PLANNED, IN_PROGRESS, etc.)
   * @returns Libellé traduit en français
   */
  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'PLANNED': 'Planifiée',
      'IN_PROGRESS': 'En cours',
      'RELEASED': 'Publiée',
      'ARCHIVED': 'Archivée'
    };
    return labels[status] || status;
  }

  /**
   * Retourne la classe CSS correspondant à un statut.
   * @param status - Statut de la version
   * @returns Nom de la classe CSS
   */
  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'PLANNED': 'status-planned',
      'IN_PROGRESS': 'status-in-progress',
      'RELEASED': 'status-released',
      'ARCHIVED': 'status-archived'
    };
    return classes[status] || '';
  }

  /**
   * Formate une date ISO en format français (JJ/MM/AAAA).
   * @param date - Date au format ISO string
   * @returns Date formatée ou chaîne vide
   */
  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR');
  }
}