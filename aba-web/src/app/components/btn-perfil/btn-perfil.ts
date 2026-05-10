import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-btn-perfil',
  standalone: true,
  imports: [],
  templateUrl: './btn-perfil.html',
  styleUrl: './btn-perfil.css',
})
export class BtnPerfilComponent {

  constructor(private router: Router) {}

  navegar(): void {
    const username = this.getCookie('username');
    if (username) {
      this.router.navigate(['/perfil']);
    } else {
      this.router.navigate(['/login']);
    }
  }

  private getCookie(name: string): string | null {
    const found = document.cookie
      .split('; ')
      .find(row => row.startsWith(`${name}=`));
    return found ? found.split('=')[1] : null;
  }
}