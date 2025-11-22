import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderComponent } from '../header/header';

@Component({
  selector: 'app-accueil',
  standalone: true,
  templateUrl: './accueil.html',
  styleUrls: ['./accueil.css'],
  imports: [CommonModule, HeaderComponent]
})
export class AccueilComponent {
  username = localStorage.getItem('username');
  menuOpen = false;

  constructor(private router: Router) {}

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu() {
    setTimeout(() => this.menuOpen = false, 100);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.router.navigate(['/']);
  }

  createProject() {
    this.router.navigate(['/projects/create']);
  }
}
