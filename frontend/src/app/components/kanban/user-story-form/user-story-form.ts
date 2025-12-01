import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface UserStory {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string }[];
}

interface Member {
  username: string;
}

/**
 * Composant de formulaire (Modal) pour la Création et l'Édition de User Stories.
 * Ce composant est "dumb" (présentationnel) : il reçoit des données et émet des événements,
 * sans appeler directement les services API.
 */
@Component({
  selector: 'app-user-story-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-story-form.html',
  styleUrls: ['./user-story-form.css']
})
export class UserStoryFormComponent implements OnChanges {
  
  /** Contrôle la visibilité de la modale. */
  @Input() show = false;

  /** Détermine si le formulaire est en mode création ou modification. */
  @Input() mode: 'create' | 'edit' = 'create';

  /** * La User Story à éditer (si mode 'edit'). 
   * Null en mode création.
   */
  @Input() story: UserStory | null = null;

  /** Liste des membres disponibles pour l'assignation. */
  @Input() members: Member[] = [];

  /** Message d'erreur éventuel à afficher (venant du parent). */
  @Input() error: string | null = null;
  
  /** Événement émis lorsque l'utilisateur ferme la modale (croix ou bouton annuler). */
  @Output() closeModal = new EventEmitter<void>();

  /** * Événement émis lors de la soumission valide du formulaire.
   * Contient l'objet JSON des valeurs du formulaire.
   */
  @Output() submitForm = new EventEmitter<any>();

  form: FormGroup;

  constructor(private fb: FormBuilder) {
    // Initialisation de la structure du formulaire avec validateurs
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM', Validators.required],
      status: ['TODO', Validators.required],
      assignedToUsernames: [[]] // Tableau de strings pour les Usernames
    });
  }

  /**
   * Méthode du cycle de vie Angular.
   * Détecte les changements des @Input pour mettre à jour le formulaire.
   * * @param changes Objet contenant les valeurs changées (currentValue, previousValue).
   */
  ngOnChanges(changes: SimpleChanges): void {
    // Cas 1 : Mode Édition - On remplit le formulaire avec les données de la story
    if (changes['story'] && this.story && this.mode === 'edit') {
      this.form.patchValue({
        title: this.story.title,
        description: this.story.description || '',
        priority: this.story.priority,
        status: this.story.status,
        // Mapping des objets utilisateurs vers un tableau de noms simples
        assignedToUsernames: this.story.assignedTo?.map(u => u.username) || []
      });
    }

    // Cas 2 : Mode Création - On réinitialise le formulaire à l'ouverture
    if (changes['show'] && this.show && this.mode === 'create') {
      this.form.reset({ priority: 'MEDIUM', status: 'TODO', assignedToUsernames: [] });
    }
  }

  /**
   * Ferme la modale sans sauvegarder.
   */
  onClose(): void {
    this.closeModal.emit();
  }

  /**
   * Vérifie la validité du formulaire et émet les données vers le composant parent.
   * Ne fait rien si le formulaire est invalide.
   */
  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitForm.emit(this.form.value);
  }

  /**
   * Getter utilitaire pour vérifier le mode actuel.
   * @returns true si mode édition, false sinon.
   */
  get isEditMode(): boolean {
    return this.mode === 'edit';
  }

  /**
   * Titre dynamique de la modale selon le contexte.
   */
  get modalTitle(): string {
    return this.isEditMode ? ' Modifier la User Story' : ' Créer une User Story';
  }

  /**
   * Texte dynamique du bouton de validation.
   */
  get submitButtonText(): string {
    return this.isEditMode ? '💾 Enregistrer' : 'Créer';
  }
}