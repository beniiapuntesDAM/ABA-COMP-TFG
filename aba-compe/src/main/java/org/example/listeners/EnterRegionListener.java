package org.example.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.example.Main;
import org.example.entity.mapa.EnterRuleEntity;
import org.example.mapa.GameMap;
import org.example.partida.Match;
import org.example.partida.MatchManager;

public class EnterRegionListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player p = e.getPlayer();
        GameMap map = Main.get().getCurrentMap();
        if (map == null) return;

        Match match = MatchManager.get().getPartida();
        if (match == null) return;

        String teamId = match.getTeamOf(p);

        double x = e.getTo().getX();
        double y = e.getTo().getY();
        double z = e.getTo().getZ();

        for (EnterRuleEntity rule : map.getEnterRules()) {
            if (rule.getRegion() == null) continue;
            if (!rule.getRegion().contains(x, y, z)) continue;



            if (teamId == null || !teamId.equalsIgnoreCase(rule.getTeamId())) {
                e.setCancelled(true);
                p.sendMessage("§c" + rule.getMessage());
                return;
            }
        }
    }
}