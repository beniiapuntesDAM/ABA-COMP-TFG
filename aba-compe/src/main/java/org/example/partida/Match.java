package org.example.partida;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.example.Main;
import org.example.entity.mapa.EquipoEntity;
import org.example.entity.mapa.MapaEntity;
import org.example.mapa.GameMap;

import java.util.*;

/**
 * Representa una partida en curso y gestiona la asignación de jugadores a equipos,
 * el scoreboard compartido y la visibilidad entre jugadores y espectadores.
 */
public class Match {

    /** Mapa de equipos con sus jugadores, indexado por ID de equipo. */
    private final Map<String, Set<Player>> equipos = new HashMap<>();

    /** Información del mapa asociado a esta partida. */
    private final MapaEntity mapa;

    /** Gestor del TAB de jugadores. */
    private final TabManager tabManager;

    /** Scoreboard compartido entre todos los jugadores y espectadores. */
    private final Scoreboard scoreboard;

    /**
     * Crea una nueva partida para el mapa indicado.
     * Registra en el scoreboard un equipo por cada equipo del mapa
     * y un equipo de espectadores.
     *
     * @param mapa información del mapa con los equipos definidos
     */
    public Match(MapaEntity mapa) {
        this.mapa = mapa;
        this.tabManager = new TabManager();

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team spectators = scoreboard.registerNewTeam("spectators");
        spectators.setColor(ChatColor.AQUA);
        spectators.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        spectators.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        for (EquipoEntity eq : mapa.getEquipos()) {
            String id = eq.getId().toLowerCase();
            Team team = scoreboard.registerNewTeam(id);
            team.setColor(ChatColor.valueOf(eq.getColor().toUpperCase()));
            team.setPrefix("");
            team.setSuffix("");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            equipos.put(eq.getId(), new HashSet<>());
        }
    }

    /**
     * Asigna un jugador al equipo indicado, actualiza su scoreboard y nombre en el TAB.
     * Si la partida está en curso, lo teleporta a su spawn, le entrega el kit
     * y gestiona su visibilidad respecto al resto de jugadores.
     *
     * @param p        jugador a añadir
     * @param equipoId ID del equipo al que se une
     */
    public void meterEnEquipo(Player p, String equipoId) {
        Team spectators = scoreboard.getTeam("spectators");
        if (spectators != null) spectators.removeEntry(p.getName());

        equipos.get(equipoId).add(p);
        p.setScoreboard(scoreboard);

        Team team = scoreboard.getTeam(equipoId.toLowerCase());
        if (team != null) team.addEntry(p.getName());

        ChatColor color = team.getColor();
        p.setDisplayName(color + p.getName());
        p.setPlayerListName(color + p.getName());
        p.sendMessage("§aTe has unido al equipo §e" + equipoId);

        if (MatchManager.get().isInGame()) {
            GameMap map = Main.get().getCurrentMap();
            if (map != null) {
                List<Location> spawns = map.getTeamSpawns(equipoId);
                if (!spawns.isEmpty()) p.teleport(spawns.get(0));
            }

            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();

            List<ItemStack> kit = map.getSpawnKit();
            for (int i = 0; i < kit.size(); i++) {
                if (kit.get(i) != null) p.getInventory().setItem(i, kit.get(i).clone());
            }

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other == p) continue;
                String otherTeam = getTeamOf(other);
                if (otherTeam != null) {
                    other.showPlayer(Main.get(), p);
                    p.showPlayer(Main.get(), other);
                } else {
                    other.showPlayer(Main.get(), p);
                    p.hidePlayer(Main.get(), other);
                }
            }
        }
    }

    /**
     * Elimina al jugador de su equipo, lo quita de todos los teams del scoreboard
     * y le asigna un scoreboard limpio.
     *
     * @param p jugador a eliminar
     */
    public void quitarDeEquipo(Player p) {
        for (Set<Player> set : equipos.values()) {
            set.remove(p);
        }
        for (Team t : scoreboard.getTeams()) {
            t.removeEntry(p.getName());
        }
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /**
     * Aplica el scoreboard de espectador al jugador, añadiéndolo al team
     * de espectadores y coloreando su nombre en azul claro.
     *
     * @param p jugador al que se aplica el scoreboard de espectador
     */
    public void aplicarScoreboardEspectador(Player p) {
        p.setScoreboard(scoreboard);
        Team spectators = scoreboard.getTeam("spectators");
        if (spectators != null) {
            spectators.addEntry(p.getName());
        }
        p.setDisplayName(ChatColor.AQUA + p.getName());
        p.setPlayerListName(ChatColor.AQUA + p.getName());
    }

    /**
     * Comprueba si el jugador pertenece a algún equipo.
     *
     * @param p jugador a comprobar
     * @return {@code true} si el jugador está en un equipo
     */
    public boolean estaEnEquipo(Player p) {
        return equipos.values().stream().anyMatch(set -> set.contains(p));
    }

    /**
     * Devuelve el ID del equipo al que pertenece el jugador.
     *
     * @param p jugador a consultar
     * @return ID del equipo, o {@code null} si es espectador
     */
    public String getTeamOf(Player p) {
        for (String teamId : equipos.keySet()) {
            if (equipos.get(teamId).contains(p)) return teamId;
        }
        return null;
    }

    /**
     * Indica si el jugador está asignado a algún equipo.
     *
     * @param p jugador a comprobar
     * @return {@code true} si el jugador tiene equipo asignado
     */
    public boolean isInTeam(Player p) {
        return getTeamOf(p) != null;
    }

    /** @return información del mapa asociado a esta partida */
    public MapaEntity getMapa() { return mapa; }

    /** @return mapa de equipos con sus jugadores */
    public Map<String, Set<Player>> getEquipos() { return equipos; }

    /** @return scoreboard compartido de la partida */
    public Scoreboard getScoreboard() { return scoreboard; }
    /**
     * Quita a un jugador del equipo sin quitarle de el scoreboard
     *
     * @param p jugador a comprobar
     */
    public void quitarDeEquipoSinScoreboard(Player p) {
        for (Set<Player> set : equipos.values()) {
            set.remove(p);
        }
        // NO tocamos el scoreboard team, se mantiene el color en el TAB
    }
}