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
});
