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
     * @param jugador
     * @return
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
     *
     * @return
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
     *
     * @param nombreClan
     * @return
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

    @GetMapping("/clan-jugador/{nombreJugador}")
    public ResponseEntity<String> getClanByJugador(@PathVariable String nombreJugador) {
        String nombreClan = playerStatsService.getNombreClanByJugador(nombreJugador);
        if (nombreClan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nombreClan);
    }
}