package org.example.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.example.Main;
import org.example.entity.objetivo.JumpPadEntity;
import org.example.mapa.GameMap;

public class JumpPadListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player p = e.getPlayer();
        GameMap map = Main.get().getCurrentMap();
        if (map == null) return;

        double x = e.getTo().getX();
        double y = e.getTo().getY();
        double z = e.getTo().getZ();

        for (JumpPadEntity pad : map.getJumpPads()) {

            if (pad.contains(p.getLocation())) {

                Vector vel = pad.getDirection().clone()
                        .normalize()
                        .multiply(pad.getForce());

                p.setVelocity(vel);
                return;
            }
        }
    }
}