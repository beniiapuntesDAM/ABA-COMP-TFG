import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { PlayerStatsService } from '../../services/player-stats.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {

  usernameInput = '';
  passwordInput = '';
  error = '';

  constructor(
    private playerStatsService: PlayerStatsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const username = this.getCookie('username');
    if (username) {
      this.router.navigate(['/perfil']);
    }
  }

  login(): void {
  this.error = '';
  this.playerStatsService.comprobarContra(this.usernameInput, this.passwordInput).subscribe({
    next: (valido) => {
      console.log('Login válido:', valido);
      if (valido) {
        this.setCookie('username', this.usernameInput);
        console.log('Cookie guardada:', this.getCookie('username'));
        this.router.navigate(['/perfil']);
      } else {
        this.error = 'Usuario o contraseña incorrectos';
      }
    },
    error: (err) => {
      console.log('Error login:', err);
      this.error = 'Usuario o contraseña incorrectos';
    }
  });
}

  private setCookie(name: string, value: string): void {
    document.cookie = `${name}=${value}; path=/`;
  }

  private getCookie(name: string): string | null {
    const found = document.cookie
      .split('; ')
      .find(row => row.startsWith(`${name}=`));
    return found ? found.split('=')[1] : null;
  }
}