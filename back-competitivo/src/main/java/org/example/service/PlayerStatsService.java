package org.example.service;

import org.example.entity.PlayerStats;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que expone las operaciones de consulta de estadísticas de jugadores.
 * Actúa como capa intermedia entre el controlador y el cliente de Supabase
 */
@Service
public class PlayerStatsService {

    /** Cliente de Supabase usado para acceder a los datos. */
    private final SupabaseClient client;

    /**
     * Crea una nueva instancia del servicio con el cliente inyectado.
     *
     * @param client cliente de Supabase
     */
    public PlayerStatsService(SupabaseClient client) {
        this.client = client;
    }

    /**
     * Devuelve las estadísticas del jugador con el UUID indicado.
     *
     * @param uuid UUID del jugador
     * @return estadísticas del jugador, o {@code null} si no existe
     */
    public PlayerStats getByUuid(String uuid) {
        return client.getByUuid(uuid);
    }

    /**
     * Devuelve la lista completa de estadísticas de todos los jugadores.
     *
     * @return lista de {@link PlayerStats}
     */
    public List<PlayerStats> getAll() {
        return client.getAllPlayers();
    }

    /**
     * Devuelve las estadísticas del jugador con el nombre de usuario indicado.
     *
     * @param username nombre de usuario del jugador
     * @return estadísticas del jugador, o {@code null} si no existe
     */
    public PlayerStats getByUsername(String username) {
        return client.getByUsername(username);
    }

    public boolean updateContra(String username, String password) { return client.updateContra(username, password); }

    public Boolean loginComprobarContrasenia(String username, String password){ return client.loginComprobarContrasenia(username, password); }

    /**
     * Devuelve las estadísticas agregadas de todos los jugadores de un clan
     *
     * @param nombreClan nombre del clan
     * @return {@link PlayerStats} con las estadísticas totales del clan
     */
    public PlayerStats buscarStatsClan(String nombreClan) {
        List<PlayerStats> members = client.getJugadoresByNombreClan(nombreClan);

        PlayerStats total = new PlayerStats();
        total.setUsername(nombreClan);

        for (PlayerStats p : members) {
            total.setKills(total.getKills() + p.getKills());
            total.setDeaths(total.getDeaths() + p.getDeaths());
            total.setWins(total.getWins() + p.getWins());
            total.setLosses(total.getLosses() + p.getLosses());
            total.setWoolsPlaced(total.getWoolsPlaced() + p.getWoolsPlaced());
            total.setPlayTime(total.getPlayTime() + p.getPlayTime());
            total.setDamageDone(total.getDamageDone() + p.getDamageDone());
            total.setDamageTaken(total.getDamageTaken() + p.getDamageTaken());
        }

        return total;
    }

    /**
     * Saca todos los jugadores de un clan
     * 
     * @param nombreClan
     * @return
     */
    public List<PlayerStats> getJugadoresDeClan(String nombreClan) {
        return client.getJugadoresByNombreClan(nombreClan);
    }


    /**
     * Actualiza el clan de un jugador, asignandole el id del clan sacado con {@link #getIdClanByNombre(String)}, a partir del nombre del clan pasado por parametro
     * 
     * @param nombreJugador
     * @param nombreClan
     * @return true si se ha actualizado correctamente, false si ha habido un error
     */
    public Boolean actualizarClanJugador(String nombreJugador, String nombreClan) {
        return client.actualizarClanJugador(nombreJugador, nombreClan);
    }

    /**
     * Devuelve una lista con el nombre de todos los clanes
     * 
     * @return lista de nombres de clanes
     */
    public List<String> getAllClanes() {
        return client.getAllClanes();
    }

    /**
     * Crea un nuevo clan con el nombre indicado, eliminando tambien espacios, mayusculas y comprobando el nombre si existiera
     * 
     * @param nombreJugador nombre del jugador que crea el clan, se le asignará el nuevo clan
      * @return {@code true} si el clan se creó correctamente, {@code false} si hubo un error al crearlo
     */
    public boolean crearClan(String nombreJugador) {
        return client.crearClan(nombreJugador);
    }

    /**
     * Devuelve el nombre del clan al que pertenece un jugador, a partir de su nombre de usuario
      * 
      * @param nombreJugador nombre del jugador
      * @return nombre del clan al que pertenece el jugador, o {@code null} si no pertenece a ningún clan
     */
    public String getNombreClanByJugador(String nombreJugador) {
        return client.getNombreClanByJugador(nombreJugador);
    }

}