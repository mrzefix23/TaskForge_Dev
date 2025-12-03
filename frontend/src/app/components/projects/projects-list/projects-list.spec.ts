import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { ProjectsListComponent } from './projects-list';
import { provideRouter } from '@angular/router';

describe('ProjectsListComponent', () => {
  let component: ProjectsListComponent;
  let fixture: ComponentFixture<ProjectsListComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockProjects = [
    {
      id: 1,
      name: 'Project 1',
      description: 'Description 1',
      owner: { username: 'owner1' },
      members: [{ username: 'member1' }]
    },
    {
      id: 2,
      name: 'Project 2',
      description: 'Description 2',
      owner: { username: 'owner2' },
      members: [{ username: 'member2' }, { username: 'member3' }]
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectsListComponent, HttpClientTestingModule],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectsListComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    localStorage.setItem('token', 'test-token');
    // Ne pas appeler detectChanges ici pour éviter ngOnInit
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to project details', () => {
    spyOn(router, 'navigate');
    component.openProject(1);
    expect(router.navigate).toHaveBeenCalledWith(['/projects', 1]);
  });

  it('should navigate to edit project', () => {
    spyOn(router, 'navigate');
    const event = new MouseEvent('click');
    spyOn(event, 'stopPropagation');
    component.editProject(1, event);
    expect(event.stopPropagation).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/projects/edit', 1]);
  });

  it('should navigate to create project', () => {
    spyOn(router, 'navigate');
    component.createProject();
    expect(router.navigate).toHaveBeenCalledWith(['/projects/create']);
  });

  it('should open delete modal when deleting project', () => {
    component.projects = mockProjects;
    const event = new MouseEvent('click');
    spyOn(event, 'stopPropagation');
    
    component.deleteProject(1, event);
    
    expect(event.stopPropagation).toHaveBeenCalled();
    expect(component.showDeleteModal).toBeTrue();
    expect(component.projectToDelete).toEqual(mockProjects[0]);
  });

  it('should handle error when loading projects', () => {
    component.showDeleteModal = true;
    component.projectToDelete = mockProjects[0];
    component.deleteError = 'Some error';

    component.closeDeleteModal();

    expect(component.showDeleteModal).toBeFalse();
    expect(component.projectToDelete).toBeNull();
    expect(component.deleteError).toBeNull();
  });
});

