import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PlayerStatsService } from '../../services/player-stats.service';
import { PlayerStats } from '../../models/player-stats.model';

@Component({
  selector: 'app-clan-perfil',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './clan-perfil.html',
  styleUrl: './clan-perfil.css'
})
export class ClanPerfil implements OnInit {

  stats: PlayerStats | null = null;
  jugadores: PlayerStats[] = [];
  clanName = '';
  error = '';

  constructor(
    private playerStatsService: PlayerStatsService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  
  ngOnInit(): void {

    this.clanName = this.route.snapshot.paramMap.get('clanName') ?? '';

    if (!this.clanName) {
      this.error = 'Clan no especificado';
      return;
    }

    // Llama al metodo del servicio que le pasa las stats del clan
    this.playerStatsService.getClanStats(this.clanName).subscribe({
      next: (data) => { this.stats = data; this.cdr.detectChanges(); },
      error: () => { this.error = 'No se pudieron cargar las estadísticas del clan'; this.cdr.detectChanges(); }
    });

    // Llama al metodo del servicio que le pasa los jugadores del clan
    this.playerStatsService.getJugadoresDeClan(this.clanName).subscribe({
      next: (data) => { this.jugadores = data; this.cdr.detectChanges(); },
      error: () => { this.jugadores = []; }
    });
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
}