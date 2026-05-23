package org.example.service;

import org.example.entity.PlayerStats;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Cliente HTTP para acceder a la tabla {@code player_stats} de Supabase.
 * Proporciona métodos para consultar jugadores por UUID o nombre de usuario.
 */
@Component
public class SupabaseClient {

    /** URL de la tabla jugadores en Supabase. */
    private final String url = "https://fiyinrjnpkwmllzkpnsj.supabase.co/rest/v1/jugadores";

    /** URL de la tabla clanes en Supabase. */
    private final String urlClanes = "https://fiyinrjnpkwmllzkpnsj.supabase.co/rest/v1/clan";

    /** Clave de API pública de Supabase. */
    private final String key = "sb_publishable_3hsCpcNVmlgYWXINwHpYkQ_Ti-ZpanW";

    /** Cliente HTTP de Spring para realizar las peticiones REST. */
    private final RestTemplate restTemplate = new RestTemplate(
            new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault())
    );

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
     * @return lista de PlayerStats de todos los jugadores
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
     * @param uuid UUID del jugador a buscar
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
     * @return {@code true} si el username y contraseña pasados coinciden con los de la bbdd, {@code false} en caso contrario
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

    /**
     * Actualiza la contraseña de un jugador identificado por su nombre de usuario.
     *
     * @param username nombre de usuario del jugador
     * @param newPassword nueva contraseña a establecer
     * @return {@code true} si la actualización fue exitosa, {@code false} en caso contrario
     */
    public boolean updateContra(String username, String newPassword) {
        HttpEntity<String> entity = new HttpEntity<>(
                "{\"password\": \"" + newPassword + "\"}",
                headers()
        );
        ResponseEntity<String> response = restTemplate.exchange(
                url + "?username=eq." + username,
                HttpMethod.PATCH,
                entity,
                String.class
        );
        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Devuelve el id del clan, a partir de su nombre pasado por parametro
     *
     * @param nombreClan nombre del clan
     * @return ID del clan, o {@code null} si no existe
     */
    public Integer getIdClanByNombre(String nombreClan) {
        HttpEntity<String> entity = new HttpEntity<>(headers());

        String nombreNormalizado = nombreClan.trim().toLowerCase();
        String queryUrl = urlClanes + "?nombre_clan=ilike.*" + nombreNormalizado + "*&select=id";

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                queryUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> result = response.getBody();

        return (result != null && !result.isEmpty()) ? ((Number) result.get(0).get("id")).intValue() : null;
    }

    /**
     * Devuelve el nombre del clan a partir del id_clan de un jugador.
     *
     * @param idClan ID del clan (foreign key en la tabla jugadores)
     * @return nombre del clan, o {@code null} si no existe
     */
    public String getNombreClanById(Integer idClan) {
        if (idClan == null) return null;

        HttpEntity<String> entity = new HttpEntity<>(headers());
        String queryUrl = urlClanes + "?id=eq." + idClan + "&select=nombre_clan";

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                queryUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> result = response.getBody();
        if (result == null || result.isEmpty()) return null;
        return (String) result.get(0).get("nombre_clan");
    }

    /**
     * Devuelve todos los jugadores cuyo id_clan coincida con el ID indicado(obtenido a partir del nombre del clan con el metodo {@link #getIdClanByNombre(String)})
     *
     * @param nombreClan nombre del clan
     * @return lista de {@link PlayerStats} miembros del clan
     */
    public List<PlayerStats> getJugadoresByNombreClan(String nombreClan) {
        HttpEntity<String> entity = new HttpEntity<>(headers());
        Integer idClan = getIdClanByNombre(nombreClan);

        if (idClan == null) return List.of();

        ResponseEntity<PlayerStats[]> response = restTemplate.exchange(
                url + "?id_clan=eq." + idClan,
                HttpMethod.GET,
                entity,
                PlayerStats[].class
        );
        PlayerStats[] result = response.getBody(); 

        return result != null ? Arrays.asList(result) : List.of();
    }

    /**
     * Cambia el id_clan del jugador por el id sacado con {@link #getIdClanByNombre(String)}, a partir del nombre del clan pasado por parametro
     * 
     * @param nombreJugador
     * @param nombreClan
     */
    public boolean actualizarClanJugador(String nombreJugador, String nombreClan) {
        Integer idClan = getIdClanByNombre(nombreClan);

        if (idClan == null) {
            System.out.println(">>> No se encontró clan con nombre: " + nombreClan);
            return false;
        }

        HttpHeaders httpHeaders = headers();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("id_clan", idClan);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);

        String queryUrl = url + "?username=eq." + nombreJugador.trim();

        ResponseEntity<String> response = restTemplate.exchange(
                queryUrl,
                HttpMethod.PATCH,
                entity,
                String.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }

    /**
     * Devuelve una lista con el nombre de todos los clanes 
     * 
     * @return lista de nombres de clanes
     */
    public List<String> getAllClanes() {
        HttpEntity<String> entity = new HttpEntity<>(headers());

        String queryUrl = urlClanes + "?select=nombre_clan";

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                queryUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> result = response.getBody();

        if (result == null || result.isEmpty()) return List.of();

        return result.stream()
                .map(row -> (String) row.get("nombre_clan"))
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo clan con el nombre indicado, eliminando tambien espacios, mayusculas y comprobando el nombre si existiera
     * 
     * @param nombreClan nombre del clan a crear
     * @return {@code true} si el clan se creó correctamente, {@code false} si hubo un error al crearlo
     */
    public boolean crearClan(String nombreClan) {
        HttpHeaders httpHeaders = headers();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("nombre_clan", nombreClan.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                urlClanes,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }

    

    /**
     * Devuelve el nombre del clan al que pertenece un jugador.
     *
     * @param nombreJugador nombre de usuario del jugador
     * @return nombre del clan, o {@code null} si no tiene clan o no existe
     */
    public String getNombreClanByJugador(String nombreJugador) {
        PlayerStats jugador = getByUsername(nombreJugador);
        if (jugador == null) return null;
        return getNombreClanById(jugador.getIdClan());
    }

}