import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface UserStory {
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

@Component({
  selector: 'app-user-story-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-story-form.html',
  styleUrls: ['./user-story-form.css']
})
export class UserStoryFormComponent implements OnChanges {
  @Input() show = false;
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() story: UserStory | null = null;
  @Input() members: Member[] = [];
  @Input() error: string | null = null;
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<any>();

  form: FormGroup;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM', Validators.required],
      status: ['TODO', Validators.required],
      assignedToUsername: ['']
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['story'] && this.story && this.mode === 'edit') {
      this.form.patchValue({
        title: this.story.title,
        description: this.story.description || '',
        priority: this.story.priority,
        status: this.story.status,
        assignedToUsername: this.story.assignedTo?.username || ''
      });
    }

    if (changes['show'] && this.show && this.mode === 'create') {
      this.form.reset({ priority: 'MEDIUM', status: 'TODO', assignedToUsername: '' });
    }
  }

  onClose(): void {
    this.closeModal.emit();
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitForm.emit(this.form.value);
  }

  get isEditMode(): boolean {
    return this.mode === 'edit';
  }

  get modalTitle(): string {
    return this.isEditMode ? ' Modifier la User Story' : ' Créer une User Story';
  }

  get submitButtonText(): string {
    return this.isEditMode ? '💾 Enregistrer' : 'Créer';
  }
}
