package org.example.service;

import org.example.entity.PlayerStats;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Arrays;
import java.util.List;

/**
 * Cliente HTTP para acceder a la tabla {@code player_stats} de Supabase.
 * Proporciona métodos para consultar jugadores por UUID o nombre de usuario.
 */
@Component
public class SupabaseClient {

    /** URL de la tabla player_stats en Supabase. */
    private final String url = "https://fiyinrjnpkwmllzkpnsj.supabase.co/rest/v1/jugadores";

    /** Clave de API pública de Supabase. */
    private final String key = "sb_publishable_3hsCpcNVmlgYWXINwHpYkQ_Ti-ZpanW";

    /** Cliente HTTP de Spring para realizar las peticiones REST. */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Construye las cabeceras HTTP necesarias para autenticarse en Supabase.
     *
     * @return cabeceras con apikey, Authorization y Content-Type
     */
    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", key);
        headers.set("Authorization", "Bearer " + key);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Devuelve la lista completa de jugadores almacenados en Supabase.
     *
     * @return lista de {@link PlayerStats} con todos los jugadores
     */
    public List<PlayerStats> getAllPlayers() {
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<PlayerStats[]> response = restTemplate.exchange(
                url + "?select=*",
                HttpMethod.GET,
                entity,
                PlayerStats[].class
        );
        return Arrays.asList(response.getBody());
    }

    /**
     * Busca un jugador por su UUID.
     *
     * @param uuid UUID del jugador
     * @return {@link PlayerStats} del jugador, o {@code null} si no existe
     */
    public PlayerStats getByUuid(String uuid) {
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<PlayerStats[]> response = restTemplate.exchange(
                url + "?uuid=eq." + uuid,
                HttpMethod.GET,
                entity,
                PlayerStats[].class
        );
        PlayerStats[] result = response.getBody();
        return result.length > 0 ? result[0] : null;
    }

    /**
     * Busca un jugador por su nombre de usuario.
     *
     * @param username nombre de usuario del jugador
     * @return {@link PlayerStats} del jugador, o {@code null} si no existe
     */
    public PlayerStats getByUsername(String username) {
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<PlayerStats[]> response = restTemplate.exchange(
                url + "?username=eq." + username,
                HttpMethod.GET,
                entity,
                PlayerStats[].class
        );
        PlayerStats[] result = response.getBody();
        return result.length > 0 ? result[0] : null;
    }

    /**
     * Metodo que busca al jugador, y compara el username y contraseña con los datos de la bbdd,
     * para la acreditacion del login
     *
     * @param username nombre de cuenta del usuario
     * @param password contraseña del usuario
     * @return
     */
    public boolean loginComprobarContrasenia(String username, String password) {
        HttpEntity<String> entity = new HttpEntity<>(headers());
        ResponseEntity<PlayerStats[]> response = restTemplate.exchange(
                url + "?username=eq." + username,
                HttpMethod.GET,
                entity,
                PlayerStats[].class
        );
        PlayerStats[] result = response.getBody();
        if (result == null || result.length == 0) return false;
        return result[0].getPassword().equals(password);
    }

}