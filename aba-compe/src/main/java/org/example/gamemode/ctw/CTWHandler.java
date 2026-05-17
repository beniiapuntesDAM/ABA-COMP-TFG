package org.example.gamemode.ctw;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.example.Main;
import org.example.entity.objetivo.WoolEntity;
import org.example.mapa.GameMap;
import org.example.partida.GameMatch;
import org.example.partida.Match;
import org.example.partida.MatchManager;
import org.example.stats.StatsManager;

public class CTWHandler {

    private final Main plugin;
    private final CTWConfig config;

    public CTWHandler(Main plugin, CTWConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!MatchManager.get().isInGame()) return;

        GameMap map = plugin.getCurrentMap();
        if (map == null) return;

        Match match = MatchManager.get().getPartida();
        if (match == null) return;

        String teamId = match.getTeamOf(p);
        if (teamId == null) return;

        ItemStack item = e.getItem().getItemStack();

        for (WoolEntity wool : config.getWools()) {
            if (wool.getColor() != item.getType()) continue;

            double ix = e.getItem().getLocation().getX();
            double iy = e.getItem().getLocation().getY();
            double iz = e.getItem().getLocation().getZ();

            if (Math.abs(ix - wool.getLocX()) > 3 ||
                    Math.abs(iy - wool.getLocY()) > 3 ||
                    Math.abs(iz - wool.getLocZ()) > 3) continue;

            // Solo si es la lana de su equipo
            if (!wool.getTeam().equalsIgnoreCase(teamId)) break;

            GameMatch gameMatch = plugin.getMatch();
            if (gameMatch != null && gameMatch.getWoolScoreboard() != null) {
                gameMatch.getWoolScoreboard().marcarTocada(wool, teamId);
                gameMatch.getWoolScoreboard().actualizar(map, match);
            }

            StatsManager.get().getJugador(p.getUniqueId(), p.getName()).addWoolTouched();

            if (gameMatch != null) {
                gameMatch.getEquiposTocaron().add(teamId);
            }

            // Solo el equipo que tocó la lana ve el mensaje
            for (Player online : Bukkit.getOnlinePlayers()) {
                String onlineTeam = match.getTeamOf(online);
                if (teamId.equalsIgnoreCase(onlineTeam)) {
                    online.sendMessage("§e" + p.getName() + " §fha tocado la lana §e" +
                            wool.getColor().name().replace("_WOOL", ""));
                }
            }

            break;
        }
    }

    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Material broken = e.getBlock().getType();

        for (WoolEntity wool : config.getWools()) {
            if (wool.getColor() != broken) continue;

            if (isSameTeam(p, wool.getTeam())) {
                p.sendMessage("§cNo puedes romper tu propia lana");
                e.setCancelled(true);
                return;
            }

            p.getInventory().addItem(new ItemStack(wool.getColor()));
            p.sendMessage("§aHas robado la lana " + wool.getColor().name());
            Bukkit.broadcastMessage("§e" + p.getName() + " ha robado la lana " + wool.getColor().name());
            return;
        }
    }

    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Material placed = e.getBlock().getType();

        GameMap map = plugin.getCurrentMap();
        Match match = MatchManager.get().getPartida();
        GameMatch gameMatch = plugin.getMatch();

        if (match == null) return;

        String equipoQueCaptura = match.getTeamOf(p);
        if (equipoQueCaptura == null) return;

        for (WoolEntity wool : config.getWools()) {
            if (wool.getColor() != placed) continue;

            Location blockLoc = e.getBlock().getLocation();

            boolean enMonumento = blockLoc.getBlockX() == (int) wool.getMonX()
                    && blockLoc.getBlockY() == (int) wool.getMonY()
                    && blockLoc.getBlockZ() == (int) wool.getMonZ()
                    && blockLoc.getWorld().getName().equals(e.getBlock().getWorld().getName());

            if (!enMonumento) continue;

            if (!wool.getTeam().equalsIgnoreCase(equipoQueCaptura)) {
                p.sendMessage("§cEsta no es tu lana para capturar.");
                continue;
            }

            e.setCancelled(false);
            wool.setCaptured(true);
            StatsManager.get().getJugador(p.getUniqueId(), p.getName()).addWoolPlaced();

            Bukkit.broadcastMessage("§6" + p.getName() + " ha capturado la lana " + wool.getColor().name().replace("_WOOL", ""));

            if (gameMatch != null && gameMatch.getWoolScoreboard() != null) {
                gameMatch.getWoolScoreboard().marcarColocada(wool);
                gameMatch.getWoolScoreboard().actualizar(map, match);
            }

            if (gameMatch != null) {
                gameMatch.getEquiposTocaron().add(equipoQueCaptura);
            }

            boolean todasCapturadas = config.getWools().stream()
                    .filter(w -> w.getTeam().equalsIgnoreCase(equipoQueCaptura))
                    .allMatch(WoolEntity::isCaptured);

            if (todasCapturadas && gameMatch != null) {
                gameMatch.end(equipoQueCaptura);
                return;
            }

            givePoints(p, config.getPointsPerWool());
            return;
        }
    }

    private boolean isSameTeam(Player p, String teamId) {
        Match match = MatchManager.get().getPartida();
        if (match == null) return false;
        String playerTeam = match.getTeamOf(p);
        return playerTeam != null && playerTeam.equalsIgnoreCase(teamId);
    }

    private void givePoints(Player p, int points) {
    }
}