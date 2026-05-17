package org.example.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.example.entity.mapa.EquipoEntity;
import org.example.entity.objetivo.WoolEntity;
import org.example.mapa.GameMap;
import org.example.partida.Match;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WoolScoreboard {

    /** Un scoreboard por equipo. */
    private final Map<String, Scoreboard> scoreboardPorEquipo = new HashMap<>();
    private final Map<String, Objective> objectivePorEquipo = new HashMap<>();

    private final Set<String> woolesColocadas = new HashSet<>();

    private final Map<String, Set<String>> tocadasPorEquipo = new HashMap<>();

    public WoolScoreboard(GameMap map, Match match) {
        // Crear un scoreboard por equipo
        for (EquipoEntity eq : map.getMapaEntity().getEquipos()) {
            String teamId = eq.getId().toLowerCase();

            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();

            // Copiar los teams del scoreboard principal para mantener colores en TAB
            for (Team t : match.getScoreboard().getTeams()) {
                Team newTeam = sb.registerNewTeam(t.getName());
                newTeam.setColor(t.getColor());
                newTeam.setPrefix(t.getPrefix());
                newTeam.setSuffix(t.getSuffix());
                newTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, t.getOption(Team.Option.NAME_TAG_VISIBILITY));
                newTeam.setOption(Team.Option.COLLISION_RULE, t.getOption(Team.Option.COLLISION_RULE));
                for (String entry : t.getEntries()) newTeam.addEntry(entry);
            }

            Objective old = sb.getObjective("wools");
            if (old != null) old.unregister();

            Objective obj = sb.registerNewObjective("wools", Criteria.DUMMY, "§6Capture the Wool");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            scoreboardPorEquipo.put(teamId, sb);
            objectivePorEquipo.put(teamId, obj);
            tocadasPorEquipo.put(teamId, new HashSet<>());
        }

        actualizar(map, match);

        // Asignar scoreboard a cada jugador según su equipo
        for (Player p : Bukkit.getOnlinePlayers()) {
            String teamId = match.getTeamOf(p);
            if (teamId != null && scoreboardPorEquipo.containsKey(teamId.toLowerCase())) {
                p.setScoreboard(scoreboardPorEquipo.get(teamId.toLowerCase()));
            }
        }
    }

    /**
     * Marca una lana como tocada solo para el equipo que la tocó.
     */
    public void marcarTocada(WoolEntity wool, String equipoQueToco) {
        String woolKey = wool.getTeam() + "_" + wool.getColor().name();
        Set<String> set = tocadasPorEquipo.get(equipoQueToco.toLowerCase());
        if (set != null) set.add(woolKey);
    }

    public void marcarColocada(WoolEntity wool) {
        woolesColocadas.add(wool.getTeam() + "_" + wool.getColor().name());
    }

    public void actualizar(GameMap map, Match match) {
        for (Map.Entry<String, Scoreboard> entry : scoreboardPorEquipo.entrySet()) {
            String equipoId = entry.getKey();
            Scoreboard sb = entry.getValue();
            Objective obj = objectivePorEquipo.get(equipoId);

            for (String e : sb.getEntries()) sb.resetScores(e);

            int score = map.getWools().size() * 2 + 2;

            for (WoolEntity wool : map.getWools()) {
                String teamColor = map.getMapaEntity().getEquipos().stream()
                        .filter(eq -> eq.getId().equalsIgnoreCase(wool.getTeam()))
                        .findFirst()
                        .map(eq -> eq.getColor().toUpperCase())
                        .orElse("WHITE");

                ChatColor color = ChatColor.valueOf(teamColor);
                String teamName = wool.getTeam().substring(0, 1).toUpperCase() + wool.getTeam().substring(1);
                obj.getScore(color + teamName).setScore(score--);

                String woolKey = wool.getTeam() + "_" + wool.getColor().name();
                String woolColorName = wool.getColor().name().replace("_WOOL", "").toLowerCase();
                ChatColor woolChatColor = getWoolChatColor(woolColorName);
                String woolName = wool.getColor().name().replace("_WOOL", "") + " Wool";

                String woolLine;
                if (woolesColocadas.contains(woolKey)) {
                    woolLine = "  " + woolChatColor + "■ " + woolName;
                } else if (tocadasPorEquipo.get(equipoId) != null && tocadasPorEquipo.get(equipoId).contains(woolKey)) {
                    woolLine = "  " + color + "■ " + woolName;
                } else {
                    woolLine = "  §7■ " + woolName;
                }

                obj.getScore(woolLine).setScore(score--);
            }
        }
    }

    /**
     * Asigna el scoreboard correcto a un jugador cuando entra o cambia de equipo.
     */
    public void asignarAJugador(Player p, String teamId) {
        Scoreboard sb = scoreboardPorEquipo.get(teamId.toLowerCase());
        if (sb != null) p.setScoreboard(sb);
    }

    private ChatColor getWoolChatColor(String woolName) {
        switch (woolName.toLowerCase()) {
            case "cyan":        return ChatColor.DARK_AQUA;
            case "orange":      return ChatColor.GOLD;
            case "red":         return ChatColor.RED;
            case "blue":        return ChatColor.BLUE;
            case "green":       return ChatColor.DARK_GREEN;
            case "yellow":      return ChatColor.YELLOW;
            case "purple":      return ChatColor.DARK_PURPLE;
            case "white":       return ChatColor.WHITE;
            case "black":       return ChatColor.BLACK;
            case "pink":        return ChatColor.LIGHT_PURPLE;
            case "lime":        return ChatColor.GREEN;
            case "magenta":     return ChatColor.LIGHT_PURPLE;
            case "gray":        return ChatColor.DARK_GRAY;
            case "light_gray":  return ChatColor.GRAY;
            default:            return ChatColor.WHITE;
        }
    }
}