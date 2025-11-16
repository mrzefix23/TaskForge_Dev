import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AccueilComponent } from './accueil';
import { HeaderComponent } from '../header/header';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('AccueilComponent', () => {
  let component: AccueilComponent;
  let fixture: ComponentFixture<AccueilComponent>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'createUrlTree', 'serializeUrl']);
    routerSpy.serializeUrl.and.callFake((url: UrlTree) => '/'); // Mock serializeUrl to return root path
    routerSpy.createUrlTree.and.returnValue({} as UrlTree); // Mock UrlTree to an empty object for simplicity
    Object.defineProperty(routerSpy, 'events', { get: () => of() }); // Mock the events observable to emit nothing
    await TestBed.configureTestingModule({
      imports: [AccueilComponent, HeaderComponent],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: {} },
      ]
    }).compileComponents();

    localStorage.setItem('username', 'testuser');
    fixture = TestBed.createComponent(AccueilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the username from localStorage', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.accueil-accent')?.textContent).toContain('testuser');
  });

  it('should remove token and username from localStorage on logout', () => {
    localStorage.setItem('token', 'dummy');
    component.logout();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('username')).toBeNull();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should navigate to /projects/create on createProject()', () => {
    component.createProject();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/projects/create']);
  });

  it('should toggle menuOpen when toggleMenu is called', () => {
    expect(component.menuOpen).toBeFalse();
    component.toggleMenu();
    expect(component.menuOpen).toBeTrue();
    component.toggleMenu();
    expect(component.menuOpen).toBeFalse();
  });

  it('should close menu after calling closeMenu', (done) => {
    component.menuOpen = true;
    component.closeMenu();
    setTimeout(() => {
      expect(component.menuOpen).toBeFalse();
      done();
    }, 150);
  });
});