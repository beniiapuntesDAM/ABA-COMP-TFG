package org.example;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.commands.CycleCommand;
import org.example.commands.EndCommand;
import org.example.commands.StartCommand;
import org.example.commands.StatsCommand;
import org.example.database.SupabaseClient;
import org.example.entity.objetivo.JumpPadEntity;
import org.example.gamemode.ctw.CTWHandler;
import org.example.gamemode.koth.KOTHHandler;
import org.example.listeners.*;
import org.example.mapa.GameMap;
import org.example.partida.GameMatch;
import org.example.partida.Match;
import org.example.partida.MatchManager;
import org.example.scoreboard.WoolScoreboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main extends JavaPlugin {

    private static Main instance;
    private GameMap currentMap;
    private GameMatch match;
    private Object handler;

    public Object getHandler() { return handler; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Bukkit.getScheduler().runTaskLater(this, () -> {

            for (World world : Bukkit.getWorlds()) {
                world.setAutoSave(false);
            }

            // Restaurar backup ANTES de cargar el mapa
            String mapName = getConfig().getString("default-map", "Abstract");
            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), mapName);
                File backupFolder = new File(Bukkit.getWorldContainer(), mapName + "_backup");
                if (backupFolder.exists()) {
                    deleteFolder(worldFolder);
                    copyFolder(backupFolder, worldFolder);
                    getLogger().info("Mapa " + mapName + " restaurado desde backup.");
                }
            } catch (Exception e) {
                getLogger().severe("Error al restaurar backup: " + e.getMessage());
            }

            // Cargar mapa
            currentMap = new GameMap(this, mapName);
            currentMap.load();

            World world = Bukkit.getWorld(mapName);
            if (world != null) {
                world.setTime(6000);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setStorm(false);
                world.setThundering(false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.MOB_GRIEFING, false);
            }


            // Inicializar Supabase
            String supabaseUrl = getConfig().getString("supabase.url");
            String supabaseKey = getConfig().getString("supabase.key");
            SupabaseClient.init(supabaseUrl, supabaseKey);

            // Crear partida
            MatchManager.get().createPartida(currentMap.getMapaEntity());
            Match partida = MatchManager.get().getPartida();

            match = new GameMatch(currentMap, partida);

            if (!currentMap.getWools().isEmpty()) {
                WoolScoreboard woolScoreboard = new WoolScoreboard(currentMap, partida);
                match.setWoolScoreboard(woolScoreboard);
            }

            // Crear handler según el modo de juego
            if (currentMap.getGameMode().equalsIgnoreCase("CTW")) {
                handler = new CTWHandler(this, currentMap.getCtwConfig());
                getLogger().info("CTWHandler cargado.");
            }

            if (currentMap.getGameMode().equalsIgnoreCase("KOTH")) {
                handler = new KOTHHandler(this, currentMap.getKothConfig());
                getLogger().info("KOTHHandler cargado.");
            }

            // Registrar comandos
            getCommand("start").setExecutor(new StartCommand(match));
            getCommand("end").setExecutor(new EndCommand(match));
            getCommand("cycle").setExecutor(new CycleCommand(this));
            getCommand("stats").setExecutor(new StatsCommand());

            // Registrar listeners
            getServer().getPluginManager().registerEvents(new JoinListener(), this);
            getServer().getPluginManager().registerEvents(new ProtecctionListener(), this);
            getServer().getPluginManager().registerEvents(new TeamSelectorListener(), this);
            getServer().getPluginManager().registerEvents(new TeamLeaveListener(), this);
            getServer().getPluginManager().registerEvents(new TeamSelectorInventoryListener(), this);
            getServer().getPluginManager().registerEvents(new DeathListener(), this);
            getServer().getPluginManager().registerEvents(new VoidListener(), this);
            getServer().getPluginManager().registerEvents(new ObserverProtectionListener(), this);
            getServer().getPluginManager().registerEvents(new ItemPickupListener(), this);
            getServer().getPluginManager().registerEvents(new KillRewardListener(), this);
            getServer().getPluginManager().registerEvents(new BlockDropListener(), this);
            getServer().getPluginManager().registerEvents(new RegionProtectionListener(), this);
            getServer().getPluginManager().registerEvents(new WoolListener(this), this);
            getServer().getPluginManager().registerEvents(new QuitListener(), this);
            getServer().getPluginManager().registerEvents(new EnterRegionListener(), this);
            getServer().getPluginManager().registerEvents(new JumpPadListener(),this);

            getLogger().info("Mapa cargado correctamente después del delay.");

        }, 40L);
    }

    @Override
    public void onDisable() {
        for (World world : Bukkit.getWorlds()) {
            world.setAutoSave(false);
        }

        // Restaurar backup al cerrar
        String mapName = getConfig().getString("default-map", "Abstract");
        try {
            File worldFolder = new File(Bukkit.getWorldContainer(), mapName);
            File backupFolder = new File(Bukkit.getWorldContainer(), mapName + "_backup");
            if (backupFolder.exists()) {
                deleteFolder(worldFolder);
                copyFolder(backupFolder, worldFolder);
                getLogger().info("Mapa " + mapName + " restaurado desde backup al cerrar.");
            }
        } catch (Exception e) {
            getLogger().severe("Error al restaurar el mapa al cerrar: " + e.getMessage());
        }
    }

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

    public static Main get() { return instance; }
    public void setCurrentMap(GameMap map) { this.currentMap = map; }
    public void setMatch(GameMatch match) { this.match = match; }
    public GameMap getCurrentMap() { return currentMap; }
    public GameMatch getMatch() { return match; }
}