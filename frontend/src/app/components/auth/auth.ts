import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common'; // Import nécessaire pour le @if

/**
 * Composant racine pour le module d'authentification.
 */
@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent implements OnInit {
  
  currentYear = new Date().getFullYear();
  
  // Variable pour contrôler l'affichage du message
  showAlert = true;

  ngOnInit(): void {
    setTimeout(() => {
      this.showAlert = false;
    }, 10000);
  }
}