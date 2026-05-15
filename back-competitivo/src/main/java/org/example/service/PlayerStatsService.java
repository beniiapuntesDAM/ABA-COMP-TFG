package org.example.service;

import org.example.entity.PlayerStats;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que expone las operaciones de consulta de estadísticas de jugadores.
 * Actúa como capa intermedia entre los controladores y el {@link SupabaseClient}.
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
}