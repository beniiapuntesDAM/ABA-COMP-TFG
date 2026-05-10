import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerStats } from '../models/player-stats.model';

@Injectable({
  providedIn: 'root'
})
export class PlayerStatsService {

  private apiUrl = 'http://localhost:8080/api/stats';

  constructor(private http: HttpClient) {}

  // Obtener todos los jugadores
  getAllStats(): Observable<PlayerStats[]> {
    return this.http.get<PlayerStats[]>(this.apiUrl);
  }

  // Obtener jugador por username
  getStatsByUsername(username: string): Observable<PlayerStats> {
    return this.http.get<PlayerStats>(`${this.apiUrl}/username/${username}`);
  }

  // Verificar credenciales 
  comprobarContra(username: string, password: string): Observable<boolean> {
    return this.http.post<boolean>(`${this.apiUrl}/check`, { username, password });
  }
}
