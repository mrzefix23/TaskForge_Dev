import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectsList } from './projects-list';

describe('ProjectsList', () => {
  let component: ProjectsList;
  let fixture: ComponentFixture<ProjectsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectsList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjectsList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
