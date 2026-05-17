package org.example.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.example.Main;
import org.example.entity.mapa.EnterRuleEntity;
import org.example.entity.mapa.RegionEntity;
import org.example.entity.objetivo.WoolEntity;
import org.example.mapa.GameMap;
import org.example.partida.Match;
import org.example.partida.MatchManager;

import java.util.Map;

public class RegionProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        GameMap map = Main.get().getCurrentMap();
        if (map != null && !map.isBuildingAllowed()) {
            e.setCancelled(true);
            return;
        }
        // Proteger salas de lana
        if (map != null && MatchManager.get().isInGame()) {
            Match match = MatchManager.get().getPartida();
            if (match != null) {
                String teamId = match.getTeamOf(p);
                for (EnterRuleEntity rule : map.getEnterRules()) {
                    if (rule.getRegion() == null) continue;
                    if (!rule.getRegion().contains(
                            e.getBlock().getX(),
                            e.getBlock().getY(),
                            e.getBlock().getZ())) continue;

                    if (teamId == null || !teamId.equalsIgnoreCase(rule.getTeamId())) {
                        e.setCancelled(true);
                        p.sendMessage("§c" + rule.getMessage());
                        return;
                    }
                }
            }
        }

        if (isProtected(p, e.getBlock().getLocation())) {
            e.setCancelled(true);
            p.sendMessage("§cNo puedes modificar bloques aquí.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        GameMap map = Main.get().getCurrentMap();
        if (map != null && !map.isBuildingAllowed()) {
            e.setCancelled(true);
            return;
        }
        if (map == null) return;

        Block block = e.getBlock();

        // BYPASS MONUMENTO — tiene prioridad sobre todo
        if (isMonumentPlacement(block, map)) {
            e.setCancelled(false);
            return;
        }

        // Proteger salas de lana
        if (MatchManager.get().isInGame()) {
            Match match = MatchManager.get().getPartida();
            if (match != null) {
                String teamId = match.getTeamOf(p);
                for (EnterRuleEntity rule : map.getEnterRules()) {
                    if (rule.getRegion() == null) continue;
                    if (!rule.getRegion().contains(
                            block.getX(),
                            block.getY(),
                            block.getZ())) continue;

                    if (teamId == null || !teamId.equalsIgnoreCase(rule.getTeamId())) {
                        e.setCancelled(true);
                        p.sendMessage("§c" + rule.getMessage());
                        return;
                    }
                }
            }
        }

        // LÍMITE DE ALTURA MÁXIMA
        if (map.getMaxBuildHeight() > 0 && block.getY() > map.getMaxBuildHeight()) {
            e.setCancelled(true);
            p.sendMessage("§cNo puedes construir más alto de Y " + map.getMaxBuildHeight());
            return;
        }

        // LÍMITE DE ALTURA MÍNIMA
        if (block.getY() < map.getMinBuildHeight()) {
            e.setCancelled(true);
            p.sendMessage("§cNo puedes construir más abajo de Y " + map.getMinBuildHeight());
            return;
        }

        // BUILD REGIONS
        if (MatchManager.get().isInGame() && !map.getBuildRegions().isEmpty()) {
            boolean dentroDeAlguna = false;
            for (RegionEntity region : map.getBuildRegions()) {
                if (region.contains(block.getX(), block.getY(), block.getZ())) {
                    dentroDeAlguna = true;
                    break;
                }
            }
            if (!dentroDeAlguna) {
                e.setCancelled(true);
                p.sendMessage("§cNo puedes construir fuera de los límites del mapa.");
                return;
            }
        }

        // NEVER BUILD REGIONS
        if (isProtected(p, block.getLocation())) {
            e.setCancelled(true);
            p.sendMessage("§cNo puedes modificar bloques aquí.");
        }
    }

    private boolean isMonumentPlacement(Block block, GameMap map) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        for (WoolEntity wool : map.getWools()) {
            if (wool.getColor() != block.getType()) continue;

            int monX = (int) Math.floor(wool.getMonX());
            int monY = (int) Math.floor(wool.getMonY());
            int monZ = (int) Math.floor(wool.getMonZ());

            if (x == monX && y == monY && z == monZ) {
                return true;
            }
        }
        return false;
    }

    private boolean isProtected(Player p, Location loc) {
        if (!MatchManager.get().isInGame()) return false;

        GameMap map = Main.get().getCurrentMap();
        if (map == null) return false;

        if (isMonumentPlacement(loc.getBlock(), map)) return false;

        Map<String, RegionEntity> regions = map.getRegions();
        for (String regionId : map.getNeverBuildRegions()) {
            RegionEntity region = regions.get(regionId);
            if (region == null) continue;
            if (region.contains(loc.getX(), loc.getY(), loc.getZ())) return true;
        }

        return false;
    }
}