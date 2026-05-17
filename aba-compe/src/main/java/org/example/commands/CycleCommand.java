package org.example.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.example.Main;
import org.example.mapa.GameMap;
import org.example.partida.GameMatch;
import org.example.partida.Match;
import org.example.partida.MatchManager;
import org.example.scoreboard.WoolScoreboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Comando que rota el mapa actual por el indicado como argumento.
 * Descarga el mundo, restaura su carpeta desde el backup, lo recarga
 * y reinicia la partida colocando a todos los jugadores en modo espectador.
 */
public class CycleCommand implements CommandExecutor {

    /** Instancia principal del plugin. */
    private final Main plugin;

    /**
     * Crea una nueva instancia del comando.
     *
     * @param plugin instancia principal del plugin
     */
    public CycleCommand(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Ejecuta la rotación de mapa. Teleporta a los jugadores fuera del mundo,
     * lo descarga, restaura la carpeta desde el backup en un hilo asíncrono
     * y recarga el mundo y la partida en el hilo principal.
     *
     * @param sender ejecutor del comando
     * @param cmd    comando ejecutado
     * @param label  alias utilizado
     * @param args   argumentos; args[0] debe ser el nombre del mapa
     * @return {@code true} siempre
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUso: /cycle <mapa>");
            return true;
        }

        String mapName = args[0];

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        org.bukkit.World worldObj = Bukkit.getWorld(mapName);
        if (worldObj != null) {
            worldObj.setAutoSave(false);
            org.bukkit.World fallback = Bukkit.getWorlds().get(0);
            for (Player p : worldObj.getPlayers()) {
                p.teleport(fallback.getSpawnLocation());
            }
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv unload " + mapName);
        sender.sendMessage("§eReseteando mapa, espera...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), mapName);
                File backupFolder = new File(Bukkit.getWorldContainer(), mapName + "_backup");

                if (!backupFolder.exists()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage("§cNo existe backup para " + mapName));
                    return;
                }

                deleteFolder(worldFolder);
                copyFolder(backupFolder, worldFolder);

            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage("§cError al resetear el mapa."));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv load " + mapName);

                GameMap map = new GameMap(plugin, mapName);
                map.load();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    World w = Bukkit.getWorld(mapName);
                    if (w != null) {
                        w.setTime(6000);
                        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                        w.setStorm(false);
                        w.setThundering(false);
                        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                        w.setGameRule(GameRule.MOB_GRIEFING, false);
                    }
                }, 20L);
                if (map.getMapaEntity() == null) {
                    sender.sendMessage("§cNo se pudo cargar el mapa " + mapName);
                    return;
                }

                if (MatchManager.get().getPartida() != null) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        MatchManager.get().getPartida().quitarDeEquipo(p);
                    }
                }

                plugin.setCurrentMap(map);
                MatchManager.get().createPartida(map.getMapaEntity());
                Match partida = MatchManager.get().getPartida();

                GameMatch match = new GameMatch(map, partida);
                plugin.setMatch(match);

                if (!map.getWools().isEmpty()) {
                    WoolScoreboard woolScoreboard = new WoolScoreboard(map, partida);
                    match.setWoolScoreboard(woolScoreboard);
                }

                plugin.getCommand("start").setExecutor(new StartCommand(match));
                plugin.getCommand("end").setExecutor(new EndCommand(match));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    partida.aplicarScoreboardEspectador(p);
                    p.setPlayerListHeader("§bABA-Compe");
                    p.setPlayerListFooter("§7Selecciona un equipo para comenzar");
                    p.teleport(map.getSpectatorSpawn());
                    p.setGameMode(GameMode.CREATIVE);
                    p.setAllowFlight(true);
                    p.setFlying(true);
                    p.getInventory().clear();

                    ItemStack selector = new ItemStack(Material.NETHER_STAR);
                    ItemMeta meta = selector.getItemMeta();
                    meta.setDisplayName("§eSeleccionar equipo");
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(Main.get(), "team_selector"),
                            PersistentDataType.INTEGER, 1
                    );
                    selector.setItemMeta(meta);
                    p.getInventory().setItem(0, selector);
                }

                MatchManager.get().setNeedsCycle(false);
                sender.sendMessage("§aMapa ciclado a §e" + mapName);
            });
        });

        return true;
    }

    /**
     * Elimina una carpeta y todo su contenido de forma recursiva.
     *
     * @param folder carpeta a eliminar
     */
    private void deleteFolder(File folder) {
        if (!folder.exists()) return;
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) deleteFolder(file);
            else file.delete();
        }
        folder.delete();
    }

    /**
     * Copia una carpeta y todo su contenido de forma recursiva.
     * Si el destino no existe, lo crea.
     *
     * @param src  carpeta origen
     * @param dest carpeta destino
     * @throws IOException si ocurre un error durante la copia
     */
    private void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            dest.mkdirs();
            String[] files = src.list();
            if (files == null) return;
            for (String file : files) {
                copyFolder(new File(src, file), new File(dest, file));
            }
        } else {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}