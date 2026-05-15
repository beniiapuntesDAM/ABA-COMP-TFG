import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PlayerStatsService } from '../../services/player-stats.service';
import { PlayerStats } from '../../models/player-stats.model';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {

  stats: PlayerStats | null = null;
  error = '';

  // --- Cambio de contraseña ---
  mostrarModalContra = false;
  nuevaContra = '';
  confirmarContra = '';
  mensajeContra = '';
  errorContra = '';

  constructor(
    private playerStatsService: PlayerStatsService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const username = this.getCookie('username');
    console.log('Cookie username:', username);

    if (!username) {
      this.router.navigate(['/login']);
      return;
    }

    this.playerStatsService.getStatsByUsername(username).subscribe({
      next: (data) => {
        console.log('Stats recibidas:', data);
        this.stats = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.log('Error stats:', err);
        this.error = 'No se pudieron cargar las estadísticas';
        this.cdr.detectChanges();
      }
    });
  }

  logout(): void {
    this.deleteCookie('username');
    this.router.navigate(['/login']);
  }

  formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h}h ${m}m`;
  }

  get kd(): string {
    if (!this.stats) return '0';
    return this.stats.deaths === 0
      ? this.stats.kills.toString()
      : (this.stats.kills / this.stats.deaths).toFixed(2);
  }

  get winrate(): string {
    if (!this.stats) return '0%';
    const total = this.stats.wins + this.stats.losses;
    return total === 0 ? '0%' : ((this.stats.wins / total) * 100).toFixed(1) + '%';
  }

  abrirModalContra(): void {
    this.mostrarModalContra = true;
    this.nuevaContra = '';
    this.confirmarContra = '';
    this.mensajeContra = '';
    this.errorContra = '';
  }

  cerrarModalContra(): void {
    this.mostrarModalContra = false;
  }

  cambiarContra(): void {
    this.mensajeContra = '';
    this.errorContra = '';

    if (!this.nuevaContra || !this.confirmarContra) {
      this.errorContra = 'Rellena ambos campos.';
      return;
    }
    if (this.nuevaContra !== this.confirmarContra) {
      this.errorContra = 'Las contraseñas no coinciden.';
      return;
    }

    const username = this.getCookie('username')!;
    this.playerStatsService.updateContra(username, this.nuevaContra).subscribe({
      next: () => {
        this.mensajeContra = '¡Contraseña actualizada correctamente!';
        this.nuevaContra = '';
        this.confirmarContra = '';
      },
      error: () => {
        this.errorContra = 'Error al actualizar la contraseña.';
      }
    });
  }

  private getCookie(name: string): string | null {
    const found = document.cookie
      .split('; ')
      .find(row => row.startsWith(`${name}=`));
    return found ? found.split('=')[1] : null;
  }

  private deleteCookie(name: string): void {
    document.cookie = `${name}=; path=/; max-age=0`;
  }
}