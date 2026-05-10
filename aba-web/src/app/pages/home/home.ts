import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchBar } from '../../components/search-bar/search-bar';
import { TopCard } from '../../components/top-card/top-card';
import { PlayerStatsService } from '../../services/player-stats.service';
import { PlayerStats } from '../../models/player-stats.model';
import { BtnPerfilComponent } from '../../components/btn-perfil/btn-perfil';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, SearchBar, TopCard],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  topKills: PlayerStats[] = [];
  topWins: PlayerStats[] = [];
  topPlayTime: PlayerStats[] = [];

  constructor(
    private playerStatsService: PlayerStatsService,
    private cdr: ChangeDetectorRef  
  ) {}

  ngOnInit(): void {
    this.playerStatsService.getAllStats().subscribe({
      next: (data: PlayerStats[]) => {
        this.topKills    = [...data].sort((a, b) => b.kills - a.kills).slice(0, 5);
        this.topWins     = [...data].sort((a, b) => b.wins - a.wins).slice(0, 5);
        this.topPlayTime = [...data].sort((a, b) => b.time_played - a.time_played).slice(0, 5);
        this.cdr.detectChanges();  
      }
    });
  }
}