import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router, RouterLinkWithHref } from '@angular/router';
import { HeaderComponent } from './header';
import { By } from '@angular/platform-browser';

describe('HeaderComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display the username from localStorage', () => {
    localStorage.setItem('username', 'testuser');
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.user-menu-username')?.textContent).toContain('testuser');
  });

  it('should toggle the menu on click', () => {
    localStorage.setItem('username', 'testuser');
    const fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const menu = compiled.querySelector('.user-menu') as HTMLElement;

    expect(compiled.querySelector('.user-menu-dropdown')).toBeNull();

    menu.click();
    fixture.detectChanges();
    expect(compiled.querySelector('.user-menu-dropdown')).toBeTruthy();

    menu.click();
    fixture.detectChanges();
    expect(compiled.querySelector('.user-menu-dropdown')).toBeNull();
  });

  it('should close the menu after closeMenu()', fakeAsync(() => {
    const fixture = TestBed.createComponent(HeaderComponent);
    const component = fixture.componentInstance;
    component.menuOpen = true;

    component.closeMenu();
    tick(120);
    expect(component.menuOpen).toBeFalse();
  }));

  it('should logout, clear storage and navigate to root', () => {
    localStorage.setItem('token', 't');
    localStorage.setItem('username', 'u');
    const fixture = TestBed.createComponent(HeaderComponent);
    const component = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navSpy = spyOn(router, 'navigate');

    component.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('username')).toBeNull();
    expect(navSpy).toHaveBeenCalledWith(['/']);
  });
});
