import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
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
  esPropietario = false;
  nombreClan: string | null = null;

  nuevaContra = '';
  confirmarContra = '';
  mensajeContra = '';
  errorContra = '';

  nuevoClan = '';
  mensajeClan = '';
  errorClan = '';

  clanAbierto = false;
  contraAbierto = false;

  constructor(
    private playerStatsService: PlayerStatsService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const usernameParam = this.route.snapshot.paramMap.get('username');

    if (usernameParam) {
      this.playerStatsService.getStatsByUsername(usernameParam).subscribe({
        next: (data) => {
          this.stats = data;
          this.cargarClan(data.username);
          this.cdr.detectChanges();
        },
        error: () => { this.error = 'Jugador no encontrado'; this.cdr.detectChanges(); }
      });
      return;
    }

    const username = this.getCookie('username');
    if (!username) {
      this.router.navigate(['/login']);
      return;
    }

    this.esPropietario = true;

    this.playerStatsService.getStatsByUsername(username).subscribe({
      next: (data) => {
        this.stats = data;
        this.cargarClan(data.username);
        this.cdr.detectChanges();
      },
      error: () => { this.error = 'No se pudieron cargar las estadísticas'; this.cdr.detectChanges(); }
    });
  }

  cargarClan(username: string): void {
    this.playerStatsService.getClanByJugador(username).subscribe({
      next: (clan) => { this.nombreClan = clan; this.cdr.detectChanges(); },
      error: () => { this.nombreClan = null; }
    });
  }

  toggleClan(): void { this.clanAbierto = !this.clanAbierto; }
  toggleContra(): void { this.contraAbierto = !this.contraAbierto; }

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
        this.cdr.detectChanges();
      },
      error: () => { this.errorContra = 'Error al actualizar la contraseña.'; this.cdr.detectChanges(); }
    });
  }

  cambiarClan(): void {
    this.mensajeClan = '';
    this.errorClan = '';

    if (!this.nuevoClan.trim()) {
      this.errorClan = 'Introduce el nombre del clan.';
      return;
    }

    const username = this.getCookie('username')!;
    this.playerStatsService.updateClan(username, this.nuevoClan.trim()).subscribe({
      next: (resultado) => {
        if (resultado) {
          this.mensajeClan = `¡Te has unido al clan ${this.nuevoClan} correctamente!`;
          this.nombreClan = this.nuevoClan.trim();
          this.nuevoClan = '';
        } else {
          this.errorClan = '❌ Clan no encontrado. Comprueba el nombre.';
        }
        this.cdr.detectChanges();
      },
      error: () => { this.errorClan = '❌ Clan no encontrado. Comprueba el nombre.'; this.cdr.detectChanges(); }
    });
  }

  private getCookie(name: string): string | null {
    const found = document.cookie.split('; ').find(row => row.startsWith(`${name}=`));
    return found ? found.split('=')[1] : null;
  }

  private deleteCookie(name: string): void {
    document.cookie = `${name}=; path=/; max-age=0`;
  }
}