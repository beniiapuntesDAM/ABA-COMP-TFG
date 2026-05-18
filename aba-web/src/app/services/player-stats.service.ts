import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerStats } from '../models/player-stats.model';

@Injectable({
  providedIn: 'root'
})
export class PlayerStatsService {

  private apiUrl = 'https://aba-comp-tfg.onrender.com/api/stats';

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

  // Cambiar contraseña una vez logeado
  updateContra(username: string, password: string): Observable<boolean> {
    return this.http.patch<boolean>(`${this.apiUrl}/updateContra`, { username, password });
  }

  // Obtener estadísticas agregadas de un clan
  getClanStats(clanName: string): Observable<PlayerStats> {
    return this.http.get<PlayerStats>(`${this.apiUrl}/clan/${clanName}`);
  }

  // Actualizar clan de un jugador
  updateClan(nombreJugador: string, nombreClan: string): Observable<boolean> {
    return this.http.patch<boolean>(
      `${this.apiUrl}/updateClan`,
      null,
      { params: { nombreJugador, nombreClan } }
    );
  }

  // Obtener todos los clanes
  getAllClanes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/clanes`);
  }

  // Crear un nuevo clan
  crearClan(nombreClan: string): Observable<boolean> {
    return this.http.post<boolean>(
      `${this.apiUrl}/crearClan`,
      null,
      { params: { nombreClan } }
    );
  }

  // Obtener nombre del clan de un jugador
  getClanByJugador(nombreJugador: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/clan-jugador/${nombreJugador}`, { responseType: 'text' });
  }
}