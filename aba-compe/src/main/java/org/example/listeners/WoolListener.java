package org.example.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.example.Main;
import org.example.entity.objetivo.WoolEntity;
import org.example.gamemode.ctw.CTWHandler;
import org.example.mapa.GameMap;
import org.example.partida.GameMatch;
import org.example.partida.Match;
import org.example.partida.MatchManager;
import org.example.stats.StatsManager;

/**
 * Listener que gestiona los objetivos de lana durante una partida.
 * Detecta cuando un jugador recoge una lana de su posición original
 * y cuando la coloca en el monumento rival para ganar la partida.
 */
public class WoolListener implements Listener {

    private final Main plugin;

    public WoolListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (Main.get().getHandler() instanceof CTWHandler ctw) {
            ctw.onPickup(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (Main.get().getHandler() instanceof CTWHandler ctw) {
            ctw.onBlockPlace(e);
        }
    }

}