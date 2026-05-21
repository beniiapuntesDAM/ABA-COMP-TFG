import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerStats } from '../models/player-stats.model';

/*
  SERVICIO QUE CONECTA CON EL BACKEND PARA OBTENER A TRAVES DE PETICIONES
  HTTP PARA EXRTAER DATOS DE LA BBDD
*/
@Injectable({
  providedIn: 'root'
})
export class PlayerStatsService {

  // URL donde se aloja el backend
  private apiUrl = 'https://aba-comp-tfg.onrender.com/api/stats';

  constructor(private http: HttpClient) {}

  // Devuelve todos los jugadores
  getAllStats(): Observable<PlayerStats[]> {
    return this.http.get<PlayerStats[]>(this.apiUrl);
  }

  /* Devuelve un PlayerStats con las estadisticas del 
     jugador cuyo username se le pasa por parametro */
  getStatsByUsername(username: string): Observable<PlayerStats> {
    return this.http.get<PlayerStats>(`${this.apiUrl}/username/${username}`);
  }

  /* Comprueba si la contraseña pasada por parametro coincide con
     la del jugador con el username pasado por parametro,
     devolviendo un booleano si coincide */
  comprobarContra(username: string, password: string): Observable<boolean> {
    return this.http.post<boolean>(`${this.apiUrl}/check`, { username, password });
  }

  /* Una vez ya logeado, cambia el atributo contraseña en la bbdd 
     del jugador cuyo nombre hemos pasado por parametro,
     devolviendo un booleano para comprobar si se hizo correctamente */
  updateContra(username: string, password: string): Observable<boolean> {
    return this.http.patch<boolean>(`${this.apiUrl}/updateContra`, { username, password });
  }

  /* Busca un clan por nombre, y comprueba en la bbdd todos
     los jugadores que pertenecen a ese clan
     y devuelve un objeto PlayerStats que contiene las estadistica de
     todos los jugadores de ese clan sumadas para semejar las del clan*/
  getClanStats(clanName: string): Observable<PlayerStats> {
    return this.http.get<PlayerStats>(`${this.apiUrl}/clan/${clanName}`);
  }

  /* Cambia el clan de un jugador(especificando username)
     por el clan nuevo(especificando el nombre)
     y devuelve un booleano para comprobar si se hizo correctamente */
  updateClan(nombreJugador: string, nombreClan: string): Observable<boolean> {
    return this.http.patch<boolean>(
      `${this.apiUrl}/updateClan`,
      null,
      { params: { nombreJugador, nombreClan } }
    );
  }

  // Devuelve todos los clanes de la bbdd(solo los nombres)
  getAllClanes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/clanes`);
  }

  // Crea un nuevo clan con el nombre especificado por parametro
  crearClan(nombreClan: string): Observable<boolean> {
    return this.http.post<boolean>(
      `${this.apiUrl}/crearClan`,
      null,
      { params: { nombreClan } }
    );
  }

  // Obtiene el nombre del clan de un jugador por su username
  getClanByJugador(nombreJugador: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/clan-jugador/${nombreJugador}`, { responseType: 'text' });
  }

  // Obtiene todos los jugadores de un clan por su nombre
  getJugadoresDeClan(nombreClan: string): Observable<PlayerStats[]> {
    return this.http.get<PlayerStats[]>(`${this.apiUrl}/clan/${nombreClan}/jugadores`);
  }
}