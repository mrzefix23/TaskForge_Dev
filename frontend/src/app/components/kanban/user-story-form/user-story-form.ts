import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface User {
  id: number;
  username: string;
}

export interface UserStory {
  id?: number;
  title: string;
  description?: string;
  status: string;
  priority: string;
  assignedTo?: User[];
}

@Component({
  selector: 'app-user-story-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-story-form.html',
  styleUrls: ['./user-story-form.css']
})
export class UserStoryFormComponent implements OnInit, OnChanges {
  @Input() show = false;
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() story: UserStory | null = null;
  @Input() members: User[] = [];
  @Input() error = '';
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<any>();

  storyForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.storyForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM', Validators.required],
      assignedTo: [[]]
    });
  }

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['story'] && this.story) {
      this.storyForm.patchValue({
        title: this.story.title,
        description: this.story.description,
        priority: this.story.priority,
        assignedTo: this.story.assignedTo || []
      });
    } else if (changes['mode'] && this.mode === 'create') {
      this.storyForm.reset({
        title: '',
        description: '',
        priority: 'MEDIUM',
        assignedTo: []
      });
    }
  }

  onSubmit(): void {
    if (this.storyForm.valid) {
      this.submitForm.emit(this.storyForm.value);
    }
  }

  onClose(): void {
    this.closeModal.emit();
  }

  onMemberChange(event: any, member: User) {
    const assignedTo = this.storyForm.get('assignedTo')?.value as User[] || [];
    if (event.target.checked) {
      // Évite les doublons
      if (!assignedTo.some(m => m.id === member.id)) {
        this.storyForm.patchValue({ assignedTo: [...assignedTo, member] });
      }
    } else {
      this.storyForm.patchValue({ assignedTo: assignedTo.filter(m => m.id !== member.id) });
    }
  }

  isAssigned(member: User): boolean {
    const assignedTo = this.storyForm.get('assignedTo')?.value as User[];
    return assignedTo?.some(m => m.id === member.id) || false;
  }
}
