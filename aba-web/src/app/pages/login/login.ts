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

  /*
  nombre de usuario
  contraseña
  error concreto(en caso de que ocurriera)
  */
  usernameInput = '';
  passwordInput = '';
  error = '';

  constructor(
    private playerStatsService: PlayerStatsService,
    private router: Router
  ) {}

  /*Al cargar la página, comprobamos si ya hay una cookie con el username,
    en caso de que haya redirige a perfil directamente*/
  ngOnInit(): void {
    const username = this.getCookie('username');
    if (username) {
      this.router.navigate(['/perfil']);
    }
  }

  // Comprueba la contraseña, con logs añadidos para comprobar que funciona
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
  // Guarda el username en una cookie para mantener la sesión iniciada
  private setCookie(name: string, value: string): void {
    document.cookie = `${name}=${value}; path=/`;
  }

  //Comprueba la cookie del username
  private getCookie(name: string): string | null {
    const found = document.cookie
      .split('; ')
      .find(row => row.startsWith(`${name}=`));
    return found ? found.split('=')[1] : null;
  }
}