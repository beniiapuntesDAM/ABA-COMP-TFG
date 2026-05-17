package org.example.scoreboard;


import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.example.entity.objetivo.ControlPointEntity;
import org.example.gamemode.koth.KOTHHandler;
import org.example.mapa.GameMap;
import org.example.partida.Match;

import java.util.Map;

/**
 * Scoreboard lateral que muestra el estado del modo KOTH durante la partida.
 * Presenta los puntos acumulados de cada equipo y el estado de cada punto de control
 * (neutral, capturando, capturado o en disputa).
 */
public class KOTHScoreboard {

    /** Scoreboard compartido con la partida. */
    private final Scoreboard scoreboard;

    /** Objetivo del scoreboard mostrado en la barra lateral. */
    private final Objective objective;

    /** Handler del KOTH del que se obtienen los datos actuales. */
    private final KOTHHandler kothHandler;

    /** Mapa actual con la información de equipos. */
    private final GameMap gameMap;

    /**
     * Crea el scoreboard de KOTH reutilizando el scoreboard de la partida.
     *
     * @param match      partida activa de la que se obtiene el scoreboard
     * @param kothHandler handler del KOTH con los puntos y estados actuales
     * @param gameMap    mapa actual con la información de equipos
     */
    public KOTHScoreboard(Match match, KOTHHandler kothHandler, GameMap gameMap) {
        this.scoreboard = match.getScoreboard();
        this.kothHandler = kothHandler;
        this.gameMap = gameMap;

        objective = scoreboard.registerNewObjective("koth", Criteria.DUMMY, "§6King of the Hill");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        actualizar();
    }

    /**
     * Recalcula y redibuja el scoreboard con el estado actual de los puntos
     * de control y la puntuación de cada equipo.
     */
    public void actualizar() {
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        int score = kothHandler.getControlPoints().size() * 2 + 4;

        for (Map.Entry<String, Integer> entry : kothHandler.getTeamPoints().entrySet()) {
            String teamId = entry.getKey();
            int pts = entry.getValue();
            ChatColor color = getTeamColor(teamId);

            String teamLine = color + teamId.substring(0, 1).toUpperCase()
                    + teamId.substring(1) + ": §f" + pts;
            objective.getScore(teamLine).setScore(score--);
        }

        objective.getScore("§7──────────").setScore(score--);

        for (ControlPointEntity cp : kothHandler.getControlPoints()) {
            String indicator = getStateIndicator(cp);
            String line = "  " + indicator + " §f" + cp.getName();
            objective.getScore(line).setScore(score--);

            if (cp.getState() == ControlPointEntity.State.CAPTURING) {
                int percent = (int) (cp.getCapturePercent() * 100);
                String progressLine = "  §7  " + buildProgressBar(percent);
                objective.getScore(progressLine).setScore(score--);
            }
        }
    }

    /**
     * Devuelve el indicador de color y símbolo según el estado del punto de control.
     *
     * @param cp punto de control a evaluar
     * @return cadena con el símbolo coloreado correspondiente al estado
     */
    private String getStateIndicator(ControlPointEntity cp) {
        switch (cp.getState()) {
            case CAPTURED:
                return getTeamColor(cp.getOwnerTeam()) + "■";
            case CAPTURING:
                return getTeamColor(cp.getOwnerTeam()) + "▶";
            case CONTESTED:
                return ChatColor.YELLOW + "✦";
            case NEUTRAL:
            default:
                return ChatColor.GRAY + "■";
        }
    }

    /**
     * Construye una barra de progreso textual para mostrar el porcentaje de captura.
     *
     * @param percent porcentaje de captura entre 0 y 100
     * @return cadena con la barra de progreso coloreada
     */
    private String buildProgressBar(int percent) {
        int filled = percent / 10;
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i == filled) bar.append("§7");
            bar.append("|");
        }
        bar.append(" §f").append(percent).append("%");
        return bar.toString();
    }

    /**
     * Devuelve el ChatColor asociado al equipo indicado según la configuración del mapa.
     *
     * @param teamId ID del equipo
     * @return ChatColor del equipo, o GRAY si no se encuentra
     */
    private ChatColor getTeamColor(String teamId) {
        if (teamId == null) return ChatColor.GRAY;
        return gameMap.getMapaEntity().getEquipos().stream()
                .filter(eq -> eq.getId().equalsIgnoreCase(teamId))
                .findFirst()
                .map(eq -> {
                    try {
                        return ChatColor.valueOf(eq.getColor().toUpperCase());
                    } catch (Exception e) {
                        return ChatColor.WHITE;
                    }
                })
                .orElse(ChatColor.WHITE);
    }
}