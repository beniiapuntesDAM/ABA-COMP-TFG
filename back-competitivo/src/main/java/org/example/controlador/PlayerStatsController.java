package org.example.controlador;

import org.example.entity.PlayerStats;
import org.example.service.PlayerStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints de estadísticas de jugadores bajo {@code /api/stats}.
 * Permite peticiones desde cualquier origen y desde {@code localhost:4200} en desarrollo.
 */
@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class PlayerStatsController {

    /** Servicio que gestiona las consultas de estadísticas. */
    private PlayerStatsService playerStatsService;

    /**
     * Crea una nueva instancia del controlador con el servicio inyectado.
     *
     * @param playerStatsService servicio de estadísticas de jugadores
     */
    public PlayerStatsController(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    /**
     * Devuelve la lista completa de estadísticas de todos los jugadores.
     *
     * @return lista de {@link PlayerStats}
     */
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping
    public List<PlayerStats> getPlayerStats() {
        return playerStatsService.getAll();
    }

    /**
     * Devuelve las estadísticas del jugador con el nombre de usuario indicado.
     *
     * @param username nombre de usuario del jugador
     * @return {@link PlayerStats} del jugador, o {@code null} si no existe
     */
    @GetMapping("/username/{username}")
    public PlayerStats getByUsername(@PathVariable String username) {
        return playerStatsService.getByUsername(username);
    }

    /**
     * Devuelve true o false dependiendo si la contraseña coincide para ese username pasado tambien
     * 
     * @param jugador jugador con el nombre de usuario y contraseña a comprobar
     * @return {@code true} si la contraseña es correcta, {@code false} si no lo es o el usuario no existe
     */
    @PostMapping("/check")
    public ResponseEntity<Boolean> comprobarContra(@RequestBody PlayerStats jugador) {
        Boolean resultado = playerStatsService.loginComprobarContrasenia(
                jugador.getUsername(),
                jugador.getPassword()
        );
        if (resultado) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }

    /**
     * Actualiza la contraseña del jugador con el nombre de usuario indicado.
     *
     * @param jugador jugador con el nombre de usuario y nueva contraseña
     * @return {@code true} si la contraseña se actualizó correctamente, {@code false} si hubo un error
     */
    @PatchMapping("/updateContra")
    public ResponseEntity<Boolean> updateContra(@RequestBody PlayerStats jugador) {
        System.out.println("Username recibido: " + jugador.getUsername());  // ← log
        System.out.println("Password recibida: " + jugador.getPassword());  // ← log
        boolean resultado = playerStatsService.updateContra(
                jugador.getUsername(),
                jugador.getPassword()
        );
        System.out.println("Resultado update: " + resultado);  // ← log
        if (resultado) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
    }

    /**
     * Devuelve las estadísticas agregadas de todos los jugadores de un clan,
     * sumadas como si fueran las de un único jugador.
     *
     * @param nombreClan nombre del clan
     * @return {@link PlayerStats} con las estadísticas totales del clan
     */
    @GetMapping("/clan/{nombreClan}")
    public ResponseEntity<PlayerStats> getClanStats(@PathVariable String nombreClan) {
        PlayerStats resultado = playerStatsService.buscarStatsClan(nombreClan);
        if (resultado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resultado);
    }

    /**
     * Devuelve la lista de jugadores del clan
     *
     * @param nombreClan
     * @return
     */
    @GetMapping("/clan/{nombreClan}/jugadores")
    public ResponseEntity<List<PlayerStats>> getJugadoresDeClan(@PathVariable String nombreClan) {
        List<PlayerStats> jugadores = playerStatsService.getJugadoresDeClan(nombreClan);
        if (jugadores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(jugadores);
    }

    /**
     * Actualiza el clan de un jugador, asignandole el id del clan sacado con {@link #getIdClanByNombre(String)}, a partir del nombre del clan pasado por parametro
     * 
     * @param nombreJugador nombre del jugador al que se le va a actualizar el clan 
     * @param nombreClan    nombre del clan al que se le va a asignar el jugador
     * @return {@code true} si se ha actualizado correctamente, {@code false} si
     */
    @PatchMapping("/updateClan")
    public ResponseEntity<Boolean> updateClan(
            @RequestParam String nombreJugador,
            @RequestParam String nombreClan) {

        System.out.println("Jugador recibido: " + nombreJugador);
        System.out.println("Clan recibido: " + nombreClan);

        boolean resultado = playerStatsService.actualizarClanJugador(nombreJugador, nombreClan);

        System.out.println("Resultado updateClan: " + resultado);

        if (resultado) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
    }

    /**
     * Devuelve una lista con el nombre de todos los clanes
     * 
     * @return lista de nombres de clanes
     */
    @GetMapping("/clanes")
    public ResponseEntity<List<String>> getAllClanes() {
        List<String> clanes = playerStatsService.getAllClanes();
        if (clanes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clanes);
    }

    /**
     * Crea un nuevo clan con el nombre indicado, eliminando tambien espacios, mayusculas y comprobando el nombre si existiera
     * 
     * @param nombreClan nombre del clan a crear
     * @return {@code true} si el clan se creó correctamente, {@code false} si hubo un error al crearlo
     */
    @PostMapping("/crearClan")
    public ResponseEntity<Boolean> crearClan(@RequestParam String nombreClan) {
        System.out.println("Clan a crear: " + nombreClan);

        boolean resultado = playerStatsService.crearClan(nombreClan);

        System.out.println("Resultado crearClan: " + resultado);

        if (resultado) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
    }

    /**
     * Devuelve el nombre del clan al que pertenece un jugador
     * 
     * @param nombreJugador nombre del jugador
     * @return nombre del clan, o {@code null} si el jugador no pertenece a ningún clan
     */
    @GetMapping("/clan-jugador/{nombreJugador}")
    public ResponseEntity<String> getClanByJugador(@PathVariable String nombreJugador) {
        String nombreClan = playerStatsService.getNombreClanByJugador(nombreJugador);
        if (nombreClan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nombreClan);
    }
}