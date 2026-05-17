package org.example.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.example.Main;
import org.example.entity.mapa.EquipoEntity;
import org.example.entity.mapa.MapaEntity;
import org.example.partida.MatchManager;

/**
 * Menú de selección de equipo. Genera un inventario con un ítem de lana
 * por cada equipo disponible en el mapa y lo abre al jugador indicado.
 */
public class TeamSelectorMenu {

    /**
     * Abre el inventario de selección de equipo para el jugador.
     * Cada ítem representa un equipo, usa la lana del color correspondiente
     * y almacena el ID del equipo en su PersistentDataContainer.
     *
     * @param p jugador al que se abre el menú
     */
    public static void open(Player p) {
        MapaEntity mapa = MatchManager.get().getPartida().getMapa();
        Inventory inv = Bukkit.createInventory(null, 9, "Selecciona un equipo");

        for (EquipoEntity eq : mapa.getEquipos()) {
            String color = eq.getColor().toUpperCase();
            Material wool = getWoolForColor(color);

            ItemStack item = new ItemStack(wool);
            ItemMeta meta = item.getItemMeta();

            ChatColor chatColor = getChatColorSafe(color);
            meta.setDisplayName(chatColor + eq.getId().toUpperCase());
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(Main.get(), "team_id"),
                    PersistentDataType.STRING,
                    eq.getId()
            );
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        p.openInventory(inv);
    }

    /**
     * Devuelve el material de lana más representativo para un ChatColor dado.
     * Necesario porque los nombres de ChatColor no coinciden directamente
     * con los nombres de los materiales de lana en Minecraft moderno.
     *
     * @param color nombre del ChatColor en mayúsculas (p. ej. {@code "GOLD"}, {@code "DARK_AQUA"})
     * @return material de lana correspondiente, o {@link Material#WHITE_WOOL} si no se reconoce
     */
    private static Material getWoolForColor(String color) {
        switch (color) {
            case "RED":          return Material.RED_WOOL;
            case "DARK_RED":     return Material.RED_WOOL;
            case "BLUE":         return Material.BLUE_WOOL;
            case "DARK_BLUE":    return Material.BLUE_WOOL;
            case "DARK_AQUA":    return Material.CYAN_WOOL;
            case "AQUA":         return Material.LIGHT_BLUE_WOOL;
            case "GOLD":         return Material.ORANGE_WOOL;
            case "YELLOW":       return Material.YELLOW_WOOL;
            case "GREEN":        return Material.LIME_WOOL;
            case "DARK_GREEN":   return Material.GREEN_WOOL;
            case "LIGHT_PURPLE": return Material.PINK_WOOL;
            case "DARK_PURPLE":  return Material.PURPLE_WOOL;
            case "WHITE":        return Material.WHITE_WOOL;
            case "GRAY":         return Material.LIGHT_GRAY_WOOL;
            case "DARK_GRAY":    return Material.GRAY_WOOL;
            case "BLACK":        return Material.BLACK_WOOL;
            default:             return Material.WHITE_WOOL;
        }
    }

    /**
     * Devuelve el ChatColor correspondiente al nombre indicado.
     * Si el nombre no es válido devuelve {@link ChatColor#WHITE} en lugar de lanzar una excepción.
     *
     * @param color nombre del ChatColor en mayúsculas
     * @return ChatColor correspondiente, o WHITE si no se reconoce
     */
    private static ChatColor getChatColorSafe(String color) {
        try {
            return ChatColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            return ChatColor.WHITE;
        }
    }
}