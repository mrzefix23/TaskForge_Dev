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
    fixture.detectChanges();
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
        title: 'Valid Task Title',
        description: 'Valid Description',
        priority: 'MEDIUM',
        status: 'TODO'
      });
      expect(component.form.valid).toBeTrue();
    });
  });
});
