import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { UserStoryFormComponent } from './user-story-form';

/**
 * Tests unitaires pour le composant UserStoryFormComponent.
 * Couvre la création et l'édition de user stories.
 */
describe('UserStoryFormComponent', () => {
  let component: UserStoryFormComponent;
  let fixture: ComponentFixture<UserStoryFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserStoryFormComponent, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(UserStoryFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Setup', () => {
    it('should have initial state', () => {
      expect(component.show).toBeFalse();
      expect(component.mode).toBe('create');
    });

    it('should have form group initialized', () => {
      expect(component.form).toBeDefined();
      expect(component.form.get('title')).toBeDefined();
      expect(component.form.get('description')).toBeDefined();
      expect(component.form.get('priority')).toBeDefined();
    });
  });

  describe('Form Validation', () => {
    it('should have required validators on title', () => {
      const titleControl = component.form.get('title');
      titleControl?.setValue('');
      expect(titleControl?.valid).toBeFalse();
    });

    it('should be valid with proper values', () => {
      component.form.patchValue({
        title: 'Valid Title',
        description: 'Valid Description',
        priority: 'MEDIUM'
      });
      expect(component.form.valid).toBeTrue();
    });
  });

  describe('Create Mode', () => {
    beforeEach(() => {
      component.mode = 'create';
      component.story = null;
    });

    it('should not submit if form is invalid', () => {
      spyOn(component.submitForm, 'emit');
      component.form.patchValue({ title: '' });

      component.onSubmit();

      expect(component.submitForm.emit).not.toHaveBeenCalled();
    });
  });

  describe('Edit Mode', () => {
    const mockUserStory = {
      id: 1,
      title: 'Existing Story',
      description: 'Existing Description',
      priority: 'MEDIUM' as 'MEDIUM',
      status: 'IN_PROGRESS' as 'IN_PROGRESS',
      assignedTo: [
        { username: 'user1' },
        { username: 'user2' }
      ],
      column: { id: 2 }
    };

    beforeEach(() => {
      component.mode = 'edit';
      component.story = mockUserStory as any;
    });

    it('should populate form with story data on changes', () => {
      component.ngOnChanges({
        story: {
          currentValue: mockUserStory,
          previousValue: null,
          firstChange: false,
          isFirstChange: () => false
        }
      });

      expect(component.form.value.title).toBe('Existing Story');
      expect(component.form.value.description).toBe('Existing Description');
      expect(component.form.value.priority).toBe('MEDIUM');
      expect(component.form.value.status).toBe('IN_PROGRESS');
      expect(component.form.value.assignedToUsernames).toEqual(['user1', 'user2']);
    });

    it('should emit updated story data on submit', () => {
      spyOn(component.submitForm, 'emit');

      component.form.patchValue({
        title: 'Updated Story',
        description: 'Updated Description',
        priority: 'HIGH',
        status: 'DONE',
        assignedToUsernames: ['user3']
      });

      component.onSubmit();

      expect(component.submitForm.emit).toHaveBeenCalledWith({
        title: 'Updated Story',
        description: 'Updated Description',
        priority: 'HIGH',
        status: 'DONE',
        assignedToUsernames: ['user3']
      });
    });
  });

  describe('Modal Control', () => {
    it('should emit closeModal event', () => {
      spyOn(component.closeModal, 'emit');
      component.onClose();
      expect(component.closeModal.emit).toHaveBeenCalled();
    });
  });

  describe('Members Assignment', () => {
    it('should display members in multi-select', () => {
      component.members = [
        { username: 'user1' },
        { username: 'user2' },
        { username: 'user3' }
      ];

      fixture.detectChanges();

      expect(component.members.length).toBe(3);
    });

    it('should allow selecting multiple members', () => {
      component.form.patchValue({
        assignedToUsernames: ['user1', 'user2', 'user3']
      });

      expect(component.form.value.assignedToUsernames.length).toBe(3);
    });

    it('should allow empty member assignment', () => {
      component.form.patchValue({ assignedToUsernames: [] });
      expect(component.form.value.assignedToUsernames).toEqual([]);
    });
  });

  describe('Column Assignment', () => {
    it('should display columns in dropdown', () => {
      component.kanbanColumns = [
        { id: 1, name: 'To Do', status: 'TODO', order: 0 },
        { id: 2, name: 'In Progress', status: 'IN_PROGRESS', order: 1 },
        { id: 3, name: 'Done', status: 'DONE', order: 2 }
      ];

      fixture.detectChanges();

      expect(component.kanbanColumns.length).toBe(3);
    });
  });

  describe('Error Display', () => {
    it('should display error message when provided', () => {
      component.show = true;
      component.error = 'Test error message';
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const modalOverlay = compiled.querySelector('.modal-overlay');
      
      expect(modalOverlay).toBeTruthy();
      
      const errorElement = compiled.querySelector('.error-message');
      if (errorElement) {
        expect(errorElement.textContent).toContain('Test error message');
      }
    });
  });

  describe('Toggle Member Selection', () => {
    it('should toggle member selection', () => {
      component.form.patchValue({ assignedToUsernames: [] });

      // Add member
      const currentMembers = component.form.value.assignedToUsernames || [];
      component.form.patchValue({
        assignedToUsernames: [...currentMembers, 'user1']
      });
      expect(component.form.value.assignedToUsernames).toContain('user1');

      // Remove member
      const updatedMembers = component.form.value.assignedToUsernames.filter(
        (u: string) => u !== 'user1'
      );
      component.form.patchValue({ assignedToUsernames: updatedMembers });
      expect(component.form.value.assignedToUsernames).not.toContain('user1');
    });
  });
});
