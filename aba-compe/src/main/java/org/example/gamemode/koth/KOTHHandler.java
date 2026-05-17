package org.example.gamemode.koth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.example.Main;
import org.example.entity.mapa.RegionEntity;
import org.example.entity.objetivo.ControlPointEntity;
import org.example.mapa.GameMap;
import org.example.partida.Match;
import org.example.scoreboard.KOTHScoreboard;

import java.util.HashMap;
import java.util.Map;

public class KOTHHandler {

    private final Main plugin;
    private final KOTHConfig config;
    private GameMap gameMap;
    private Match match;
    private KOTHScoreboard scoreboard;
    private BukkitTask task;
    private Runnable onWin;
    private final Map<String, Integer> teamPoints = new HashMap<>();

    public KOTHHandler(Main plugin, KOTHConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
        teamPoints.clear();
        gameMap.getMapaEntity().getEquipos().forEach(eq ->
                teamPoints.put(eq.getId().toLowerCase(), 0)
        );
    }

    public void setMatch(Match match) { this.match = match; }
    public void setOnWin(Runnable onWin) { this.onWin = onWin; }
    public void setScoreboard(KOTHScoreboard scoreboard) { this.scoreboard = scoreboard; }

    public void start() {
        if (task != null) task.cancel();
        for (ControlPointEntity cp : config.getControlPoints()) {
            saveOriginalBlocks(cp);
            saveOriginalFloor(cp);
            restoreOriginalBlocks(cp);
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) task.cancel();

        // Restaurar bloques originales al parar
        for (ControlPointEntity cp : config.getControlPoints()) {
            restoreOriginalBlocks(cp);
        }
    }

    private void tick() {
        for (ControlPointEntity cp : config.getControlPoints()) {

            Map<String, Integer> players = countPlayers(cp);
            int teamsPresent = (int) players.values().stream().filter(c -> c > 0).count();

            if (teamsPresent == 0) {
                handleEmpty(cp);
            } else if (teamsPresent >= 2) {
                handleContested(cp);
            } else {
                String team = players.entrySet().stream()
                        .filter(e -> e.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);
                if (team != null) handleCapturing(cp, team, players.get(team));
            }

            // Dar puntos si está capturado
            if (cp.getState() == ControlPointEntity.State.CAPTURED) {
                String owner = cp.getOwnerTeam();
                teamPoints.merge(owner, cp.getPointsPerTick(), Integer::sum);

                if (teamPoints.get(owner) >= config.getPointsToWin()) {
                    announceWinner(owner);
                    stop();
                    if (onWin != null) onWin.run();
                    return;
                }
            }

            updateFill(cp);
            updateFloor(cp);
        }

        if (scoreboard != null) scoreboard.actualizar();
    }

    private void handleEmpty(ControlPointEntity cp) {
        // Si nadie está, el progreso se mantiene (incremental) o baja
        if (config.isIncremental()) return;

        if (cp.getCaptureProgress() > 0) {
            cp.setCaptureProgress(cp.getCaptureProgress() - 1);
            if (cp.getCaptureProgress() <= 0) {
                cp.setState(ControlPointEntity.State.NEUTRAL);
                cp.setOwnerTeam(null);
            }
        }
    }

    private void handleContested(ControlPointEntity cp) {
        // En disputa: no avanza ni retrocede
        if (cp.getState() != ControlPointEntity.State.CONTESTED) {
            cp.setState(ControlPointEntity.State.CONTESTED);
            Bukkit.broadcastMessage("§e" + cp.getName() + " §7está en disputa.");
        }
    }

    private void handleCapturing(ControlPointEntity cp, String team, int players) {
        // Si el mismo equipo lo tiene capturado, no hacer nada
        if (cp.getState() == ControlPointEntity.State.CAPTURED
                && team.equalsIgnoreCase(cp.getOwnerTeam())) {
            return;
        }

        // Si el rival está pisando un punto capturado o siendo capturado por otro equipo
        // → descapear (bajar progreso)
        if (cp.getOwnerTeam() != null && !team.equalsIgnoreCase(cp.getOwnerTeam())) {
            cp.setCaptureProgress(cp.getCaptureProgress() - 1);

            if (cp.getCaptureProgress() <= 0) {
                // Neutralizado — ahora el rival empieza a capturar
                cp.setState(ControlPointEntity.State.NEUTRAL);
                cp.setOwnerTeam(null);
                Bukkit.broadcastMessage("§7" + cp.getName() + " §7ha sido neutralizado.");
            }
            return;
        }

        // Capturando desde neutral
        cp.setState(ControlPointEntity.State.CAPTURING);
        cp.setOwnerTeam(team);

        double inc = 1.0;
        if (config.isScaledTime() && players > 1) {
            inc = 1.0 + (players - 1) * config.getTimeMultiplier();
        }

        cp.setCaptureProgress(cp.getCaptureProgress() + inc);

        if (cp.getCaptureProgress() >= cp.getCaptureTime()) {
            cp.setState(ControlPointEntity.State.CAPTURED);
            cp.setCaptureProgress(cp.getCaptureTime());

            ChatColor color = getTeamColor(team);
            Bukkit.broadcastMessage(color + team + " §aha capturado §e" + cp.getName() + "§a.");
        }
    }

    private void updateFill(ControlPointEntity cp) {
        RegionEntity fill = cp.getFillRegion();
        if (fill == null) return;

        World world = Bukkit.getWorld(gameMap.getMapName());
        if (world == null) return;

        int minY = (int) fill.getMinY();
        int maxY = (int) fill.getMaxY();
        int totalY = maxY - minY + 1;
        int filledY = (int) Math.round(cp.getCapturePercent() * totalY);

        Material teamMat = getMaterialForTeam(cp.getOwnerTeam());

        for (Map.Entry<String, Material> entry : cp.getOriginalBlocks().entrySet()) {
            // Solo modificar bloques que originalmente NO eran aire
            if (entry.getValue() == Material.AIR) continue;

            String[] parts = entry.getKey().split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            Block block = world.getBlockAt(x, y, z);
            int relY = y - minY;
            Material original = entry.getValue();

            if (cp.getOwnerTeam() == null) {
                // Neutral: restaurar original
                if (block.getType() != original) block.setType(original);
            } else if (relY < filledY) {
                // Relleno del equipo
                if (block.getType() != teamMat) block.setType(teamMat);
            } else {
                // Sin rellenar: original
                if (block.getType() != original) block.setType(original);
            }
        }
    }

    private void saveOriginalBlocks(ControlPointEntity cp) {
        RegionEntity fill = cp.getFillRegion();
        if (fill == null) return;

        World world = Bukkit.getWorld(gameMap.getMapName());
        if (world == null) return;

        int minX = (int) fill.getMinX();
        int maxX = (int) fill.getMaxX();
        int minY = (int) fill.getMinY();
        int maxY = (int) fill.getMaxY();
        int minZ = (int) fill.getMinZ();
        int maxZ = (int) fill.getMaxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    String key = x + "," + y + "," + z;
                    cp.getOriginalBlocks().put(key, world.getBlockAt(x, y, z).getType());
                }
            }
        }
    }

    private void restoreOriginalBlocks(ControlPointEntity cp) {
        RegionEntity fill = cp.getFillRegion();
        if (fill == null) return;

        World world = Bukkit.getWorld(gameMap.getMapName());
        if (world == null) return;

        for (Map.Entry<String, Material> entry : cp.getOriginalBlocks().entrySet()) {
            String[] parts = entry.getKey().split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            world.getBlockAt(x, y, z).setType(entry.getValue());
        }
    }

    private Map<String, Integer> countPlayers(ControlPointEntity cp) {
        Map<String, Integer> counts = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String team = match.getTeamOf(p);
            if (team == null) continue;
            if (cp.getCaptureRegion().contains(
                    p.getLocation().getX(),
                    p.getLocation().getY(),
                    p.getLocation().getZ())) {
                counts.merge(team.toLowerCase(), 1, Integer::sum);
            }
        }
        return counts;
    }

    private void announceWinner(String team) {
        ChatColor color = getTeamColor(team);
        Bukkit.broadcastMessage("§a¡El equipo " + color + team + " §aha ganado la partida!");
    }

    private ChatColor getTeamColor(String teamId) {
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

    private Material getMaterialForTeam(String teamId) {
        if (teamId == null) return Material.AIR;
        return switch (teamId.toLowerCase()) {
            case "red" -> Material.RED_TERRACOTTA;
            case "blue" -> Material.BLUE_TERRACOTTA;
            case "orange" -> Material.ORANGE_TERRACOTTA;
            case "cyan" -> Material.CYAN_TERRACOTTA;
            case "green" -> Material.GREEN_TERRACOTTA;
            case "yellow" -> Material.YELLOW_TERRACOTTA;
            case "purple" -> Material.PURPLE_TERRACOTTA;
            case "white" -> Material.WHITE_TERRACOTTA;
            default -> Material.WHITE_TERRACOTTA;
        };
    }
    private void saveOriginalFloor(ControlPointEntity cp) {
        RegionEntity floor = cp.getFloorRegion();
        if (floor == null) return;

        World world = Bukkit.getWorld(gameMap.getMapName());
        if (world == null) return;

        int minX = (int) floor.getMinX();
        int maxX = (int) floor.getMaxX();
        int minY = (int) floor.getMinY();
        int maxY = (int) floor.getMaxY();
        int minZ = (int) floor.getMinZ();
        int maxZ = (int) floor.getMaxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    String key = x + "," + y + "," + z;
                    cp.getOriginalFloorBlocks().put(key, world.getBlockAt(x, y, z).getType());
                }
            }
        }
    }

    private void updateFloor(ControlPointEntity cp) {
        RegionEntity floor = cp.getFloorRegion();
        if (floor == null) return;

        World world = Bukkit.getWorld(gameMap.getMapName());
        if (world == null) return;

        Material teamMat = getMaterialForTeam(cp.getOwnerTeam());
        Material baseMat = cp.getFloorMaterial() != null ? cp.getFloorMaterial() : Material.WHITE_TERRACOTTA;

        int totalBlocks = cp.getOriginalFloorBlocks().values().stream()
                .filter(m -> m == baseMat)
                .mapToInt(m -> 1).sum();
        int filledBlocks = (int) Math.round(cp.getCapturePercent() * totalBlocks);

        int count = 0;
        for (Map.Entry<String, Material> entry : cp.getOriginalFloorBlocks().entrySet()) {
            if (entry.getValue() != baseMat) continue;

            String[] parts = entry.getKey().split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            Block block = world.getBlockAt(x, y, z);

            if (cp.getOwnerTeam() == null) {
                if (block.getType() != baseMat) block.setType(baseMat);
            } else if (count < filledBlocks) {
                if (block.getType() != teamMat) block.setType(teamMat);
            } else {
                if (block.getType() != baseMat) block.setType(baseMat);
            }
            count++;
        }
    }

    public Map<String, Integer> getTeamPoints() { return teamPoints; }
    public java.util.List<ControlPointEntity> getControlPoints() { return config.getControlPoints(); }
}