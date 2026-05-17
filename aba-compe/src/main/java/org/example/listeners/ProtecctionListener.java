package org.example.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.example.partida.Match;
import org.example.partida.MatchManager;

public class ProtecctionListener implements Listener {

    private boolean isSpectator(Player p) {
        Match partida = MatchManager.get().getPartida();
        if (partida == null) return true;
        return !partida.isInTeam(p);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player p)) return;

        Match match = MatchManager.get().getPartida();
        if (match == null) return;

        if (!match.isInTeam(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isSpectator(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Selecciona un equipo")) return;
        if (isSpectator((Player) event.getWhoClicked())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isSpectator((Player) event.getWhoClicked())) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!isSpectator(p)) return;
        if (event.getClickedBlock() == null) return;

        switch (event.getClickedBlock().getType()) {
            case BARREL:
            case ENDER_CHEST:
            case SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                event.setCancelled(true);
                break;
        }
    }
}
