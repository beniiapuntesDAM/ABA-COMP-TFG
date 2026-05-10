package org.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Entidad que representa las estadísticas de un jugador almacenadas en Supabase.
 * Los getters, setters y demás métodos estándar son generados por Lombok {@code @Data}.
 */
@Data
public class PlayerStats {

    /** UUID único del jugador. */
    private String uuid;

    /** Nombre de usuario del jugador. */
    private String username;

    /** Contraseña del jugador. */
    private String password;

    /** Número de kills realizados. */
    private int kills;

    /** Número de muertes. */
    private int deaths;

    /** Número de victorias. */
    private int wins;

    /** Número de derrotas. */
    private int losses;

    /** Número de lanas colocadas en el monumento. */
    @JsonProperty("wools_placed")
    private int woolsPlaced;

    /** Tiempo total jugado en segundos. */
    @JsonProperty("time_played")
    private long playTime;

    /** Fecha de creación del registro en formato ISO 8601. */
    @JsonProperty("created_at")
    private String createdAt;

    /** Daño infligido. */
    @JsonProperty("damage_done")
    private int damageDone;

    /** Daño recibido. */
    @JsonProperty("damage_taken")
    private int damageTaken;
}