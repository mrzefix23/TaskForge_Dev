import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface Task {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string };
}

interface Member {
  username: string;
}

/**
 * Composant de formulaire (Modal) pour la gestion des Tâches.
 * Permet de créer ou d'éditer une tâche au sein d'une User Story.
 * Ce composant est purement présentationnel (Smart/Dumb component pattern).
 */
@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './task-form.html',
  styleUrls: ['./task-form.css']
})
export class TaskFormComponent implements OnChanges {
  
  /** Contrôle l'affichage de la modale. */
  @Input() show = false;

  /** Mode d'ouverture : 'create' pour une nouvelle tâche, 'edit' pour modifier. */
  @Input() mode: 'create' | 'edit' = 'create';

  /** * La tâche à modifier (en mode 'edit').
   * Contient les données à pré-remplir dans le formulaire.
   */
  @Input() task: Task | null = null;

  /** Liste des membres du projet éligibles à l'assignation. */
  @Input() members: Member[] = [];

  /** Message d'erreur serveur à afficher en cas d'échec. */
  @Input() error: string | null = null;
  
  /** Événement émis lors de la fermeture de la modale (sans action). */
  @Output() closeModal = new EventEmitter<void>();

  /** * Événement émis lors de la soumission du formulaire valide.
   * Transmet les données de la tâche au composant parent.
   */
  @Output() submitForm = new EventEmitter<any>();

  form: FormGroup;

  constructor(private fb: FormBuilder) {
    // Initialisation du formulaire Reactif
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM', Validators.required],
      status: ['TODO', Validators.required],
      assignedToUsername: [''] // Une tâche est assignée à un seul utilisateur (string)
    });
  }

  /**
   * Gestion du cycle de vie : Réagit aux changements des Inputs.
   * Remplit le formulaire si une tâche est fournie (Edit) ou réinitialise (Create).
   * * @param changes Les changements détectés sur les propriétés d'entrée.
   */
  ngOnChanges(changes: SimpleChanges): void {
    // Cas : Chargement d'une tâche existante pour édition
    if (changes['task'] && this.task && this.mode === 'edit') {
      this.form.patchValue({
        title: this.task.title,
        description: this.task.description || '',
        priority: this.task.priority,
        status: this.task.status,
        // Extraction du username de l'objet assignedTo (s'il existe)
        assignedToUsername: this.task.assignedTo?.username || ''
      });
    }

    // Cas : Réinitialisation pour une nouvelle création
    if (changes['show'] && this.show && this.mode === 'create') {
      this.form.reset({ priority: 'MEDIUM', status: 'TODO', assignedToUsername: '' });
    }
  }

  /**
   * Ferme la modale en émettant l'événement vers le parent.
   */
  onClose(): void {
    this.closeModal.emit();
  }

  /**
   * Valide et soumet le formulaire.
   * Arrête l'exécution si le formulaire est invalide.
   */
  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitForm.emit(this.form.value);
  }

  /**
   * Vérifie si le composant est en mode édition.
   * @returns true si mode 'edit', false sinon.
   */
  get isEditMode(): boolean {
    return this.mode === 'edit';
  }

  /**
   * Définit le titre de la fenêtre modale selon le contexte.
   */
  get modalTitle(): string {
    return this.isEditMode ? 'Modifier la Tâche' : 'Créer une Tâche';
  }

  /**
   * Définit le texte du bouton d'action selon le contexte.
   */
  get submitButtonText(): string {
    return this.isEditMode ? '💾 Enregistrer' : 'Créer';
  }
}