//cLASE QUE ALMACENA LAS ESTADISTICAS DE LOS JUGADORES
export interface PlayerStats {
  // id dentro de minecraft
  uuid: string;

  // nombre de usuario dentro de minecraft
  username: string;

  // numero de kills registradas durante el tiempo jugado
  kills: number;

  // numero de muertes registradas durante el tiempo jugado
  deaths: number;

  // numero de victorias de un jugador
  wins: number;

  // numero de derrotas de un jugador
  losses: number;

  // numero de lanas colocadas en el modo Capture the Wool
  wools_placed: number;

  // tiempo de juego total del jugador
  time_played: number;

  // fecha del primer inicio de sesion en el server
  created_at: string;

  // total de daño hecho por el jugador durante el tiempo jugado
  damage_done: number;

  // total de daño recibido por el jugador durante el tiempo jugado
  damage_taken: number;

  // contraseña registrada en la bbdd del jugador
  password: string;
}
