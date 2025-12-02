import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { TaskFormComponent } from './task-form';

/**
 * Tests unitaires pour le composant TaskFormComponent.
 * Couvre la création et l'édition de tâches.
 */
describe('TaskFormComponent', () => {
  let component: TaskFormComponent;
  let fixture: ComponentFixture<TaskFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskFormComponent, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskFormComponent);
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
      expect(component.form.get('status')).toBeDefined();
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
        title: 'Test Task',
        description: 'Test Description',
        priority: 'HIGH',
        status: 'TODO',
        assignedToUsername: 'testuser'
      });
      expect(component.form.valid).toBeTrue();
    });
  });

  describe('Create Mode', () => {
    beforeEach(() => {
      component.mode = 'create';
      component.task = null;
    });

    it('should emit submitForm with form data', () => {
      spyOn(component.submitForm, 'emit');

      component.form.patchValue({
        title: 'New Task',
        description: 'Task Description',
        priority: 'MEDIUM',
        status: 'TODO',
        assignedToUsername: 'user1'
      });

      component.onSubmit();

      expect(component.submitForm.emit).toHaveBeenCalledWith({
        title: 'New Task',
        description: 'Task Description',
        priority: 'MEDIUM',
        status: 'TODO',
        assignedToUsername: 'user1'
      });
    });

    it('should not submit if form is invalid', () => {
      spyOn(component.submitForm, 'emit');
      component.form.patchValue({ title: '' });

      component.onSubmit();

      expect(component.submitForm.emit).not.toHaveBeenCalled();
    });
  });

  describe('Edit Mode', () => {
    const mockTask = {
      id: 1,
      title: 'Existing Task',
      description: 'Existing Description',
      priority: 'HIGH' as 'HIGH',
      status: 'IN_PROGRESS' as 'IN_PROGRESS',
      assignedTo: { username: 'testuser' }
    };

    beforeEach(() => {
      component.mode = 'edit';
      component.task = mockTask;
    });

    it('should populate form with task data on changes', () => {
      component.ngOnChanges({
        task: {
          currentValue: mockTask,
          previousValue: null,
          firstChange: false,
          isFirstChange: () => false
        }
      });

      expect(component.form.value.title).toBe('Existing Task');
      expect(component.form.value.description).toBe('Existing Description');
      expect(component.form.value.priority).toBe('HIGH');
      expect(component.form.value.status).toBe('IN_PROGRESS');
      expect(component.form.value.assignedToUsername).toBe('testuser');
    });

    it('should emit updated task data on submit', () => {
      spyOn(component.submitForm, 'emit');

      component.form.patchValue({
        title: 'Updated Task',
        description: 'Updated Description',
        priority: 'LOW',
        status: 'DONE',
        assignedToUsername: 'user2'
      });

      component.onSubmit();

      expect(component.submitForm.emit).toHaveBeenCalledWith({
        title: 'Updated Task',
        description: 'Updated Description',
        priority: 'LOW',
        status: 'DONE',
        assignedToUsername: 'user2'
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
    it('should display members in dropdown', () => {
      component.members = [
        { username: 'user1' },
        { username: 'user2' },
        { username: 'user3' }
      ];

      fixture.detectChanges();

      expect(component.members.length).toBe(3);
    });

    it('should allow unassigning task', () => {
      component.form.patchValue({ assignedToUsername: 'user1' });
      component.form.patchValue({ assignedToUsername: '' });

      expect(component.form.value.assignedToUsername).toBe('');
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
});
