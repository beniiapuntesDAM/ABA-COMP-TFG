package org.example.gamemode.koth;


import org.example.entity.objetivo.ControlPointEntity;

import java.util.List;

/**
 * Almacena la configuración global del modo KOTH cargada desde el map.yml.
 * Controla los puntos para ganar y el comportamiento de captura de los puntos de control.
 */
public class KOTHConfig {

    /** Puntos necesarios para ganar la partida. */
    private final int pointsToWin;

    /**
     * Si es {@code true}, el progreso de captura se acumula entre intentos
     * en lugar de reiniciarse al salir del punto.
     */
    private final boolean incremental;

    /**
     * Si es {@code true}, los puntos empiezan sin dueño y deben capturarse
     * desde el estado neutral.
     */
    private final boolean neutralState;

    /**
     * Si es {@code true}, más jugadores en el punto aceleran la captura
     * proporcionalmente al número de jugadores.
     */
    private final boolean scaledTime;

    /** Factor de escala para el tiempo de captura con scaledTime activo. */
    private final double timeMultiplier;

    /** Lista de puntos de control del mapa. */
    private final List<ControlPointEntity> controlPoints;

    /**
     * Crea una nueva configuración de KOTH.
     *
     * @param pointsToWin    puntos necesarios para ganar
     * @param incremental    si el progreso de captura es acumulativo
     * @param neutralState   si los puntos empiezan en estado neutral
     * @param scaledTime     si más jugadores aceleran la captura
     * @param timeMultiplier factor de escala para el tiempo de captura
     * @param controlPoints  lista de puntos de control del mapa
     */
    public KOTHConfig(int pointsToWin, boolean incremental, boolean neutralState,
                      boolean scaledTime, double timeMultiplier, List<ControlPointEntity> controlPoints) {
        this.pointsToWin = pointsToWin;
        this.incremental = incremental;
        this.neutralState = neutralState;
        this.scaledTime = scaledTime;
        this.timeMultiplier = timeMultiplier;
        this.controlPoints = controlPoints;
    }

    /** @return puntos necesarios para ganar la partida */
    public int getPointsToWin() { return pointsToWin; }

    /** @return {@code true} si el progreso de captura es acumulativo */
    public boolean isIncremental() { return incremental; }

    /** @return {@code true} si los puntos empiezan en estado neutral */
    public boolean isNeutralState() { return neutralState; }

    /** @return {@code true} si más jugadores aceleran la captura */
    public boolean isScaledTime() { return scaledTime; }

    /** @return factor de escala para el tiempo de captura */
    public double getTimeMultiplier() { return timeMultiplier; }

    /** @return lista de puntos de control del mapa */
    public List<ControlPointEntity> getControlPoints() { return controlPoints; }
}
