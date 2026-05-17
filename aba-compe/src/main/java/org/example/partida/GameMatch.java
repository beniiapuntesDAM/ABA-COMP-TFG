package org.example.partida;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.example.Main;
import org.example.entity.jugador.JugadorEntity;
import org.example.entity.mapa.EquipoEntity;
import org.example.gamemode.koth.KOTHHandler;
import org.example.mapa.GameMap;
import org.example.scoreboard.KOTHScoreboard;
import org.example.scoreboard.WoolScoreboard;
import org.example.stats.StatsManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameMatch {

    private int countdown = 30;
    private BukkitTask countdownTask;
    private BossBar countdownBar;
    private BossBar matchBar;
    private BukkitTask matchTask;
    private final GameMap gameMap;
    private final int playersPerTeam;
    private final Set<String> equiposTocaron = new HashSet<>();
    private final Match match;
    private int timeLeft;
    private boolean started = false;
    private WoolScoreboard woolScoreboard;
    private KOTHHandler kothHandler;

    public GameMatch(GameMap gameMap, Match match) {
        this.gameMap = gameMap;
        this.match = match;

        EquipoEntity eq = gameMap.getMapaEntity().getEquipos().get(0);
        this.playersPerTeam = eq.getMaxPlayers();

        if (playersPerTeam <= 5) {
            timeLeft = 20 * 60;
        } else {
            timeLeft = 40 * 60;
        }
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public void start() {
        System.out.println("GAMEMATCH START EJECUTADO");

        if (started) return;
        started = true;

        // 1. Eliminar bloques marcados en deleteOnStart
        World world = Bukkit.getWorld(gameMap.getMapName());
        if (world != null && !gameMap.getDeleteOnStart().isEmpty()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                            Block block = chunk.getBlock(x, y, z);
                            if (gameMap.getDeleteOnStart().contains(block.getType())) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
            }
        }

        Bukkit.broadcastMessage("§aLa partida ha comenzado.");

        // 2. Ocultar jugadores sin equipo
        for (Player p : Bukkit.getOnlinePlayers()) {
            String team = match.getTeamOf(p);
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (p == other) continue;
                String otherTeam = match.getTeamOf(other);
                if (team != null && otherTeam == null) {
                    p.hidePlayer(Main.get(), other);
                } else {
                    p.showPlayer(Main.get(), other);
                }
            }
        }

        // 3. MODO CTW → crear scoreboard ANTES de teleportar jugadores
        System.out.println("[CTW] gameMode: " + gameMap.getGameMode());
        System.out.println("[CTW] ctwConfig null? " + (gameMap.getCtwConfig() == null));
        System.out.println("[CTW] equipos en mapa: " + gameMap.getMapaEntity().getEquipos().size());
        System.out.println("[CTW] wools en mapa: " + gameMap.getWools().size());

        if (gameMap.getGameMode().equals("CTW") && gameMap.getCtwConfig() != null) {
            woolScoreboard = new WoolScoreboard(gameMap, match);
            System.out.println("[CTW] WoolScoreboard creado correctamente");
        } else {
            System.out.println("[CTW] WoolScoreboard NO creado - mode=" + gameMap.getGameMode() + " ctwConfig=" + gameMap.getCtwConfig());
        }

        // 4. Teleport, kits y armadura
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setSaturation(20);

            String team = match.getTeamOf(p);

            if (team == null) {
                p.setGameMode(GameMode.CREATIVE);
                continue;
            }

            List<Location> spawns = gameMap.getTeamSpawns(team);
            if (!spawns.isEmpty()) p.teleport(spawns.get(0));

            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();

            List<ItemStack> kit = gameMap.getSpawnKit();
            for (int i = 0; i < kit.size(); i++) {
                ItemStack item = kit.get(i);
                if (item != null) p.getInventory().setItem(i, item.clone());
            }

            String kitId = team + "-kit";
            List<ItemStack> armor = gameMap.getTeamArmor(kitId);
            ItemStack[] armorArray = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                ItemStack piece = armor.get(i);
                armorArray[i] = piece != null ? piece.clone() : null;
            }
            p.getInventory().setArmorContents(armorArray);

            // Asignar scoreboard por equipo
            if (woolScoreboard != null) {
                woolScoreboard.asignarAJugador(p, team);
            }

            Bukkit.broadcastMessage("DEBUG TEAM OF " + p.getName() + ": " + match.getTeamOf(p));
        }

        MatchManager.get().setInGame(true);

        // 5. BossBar del tiempo restante
        matchBar = Bukkit.createBossBar(
                "Tiempo restante: " + formatTime(timeLeft),
                BarColor.GREEN,
                BarStyle.SOLID
        );
        for (Player p : Bukkit.getOnlinePlayers()) matchBar.addPlayer(p);

        int totalTime = (playersPerTeam <= 5 ? 20 * 60 : 40 * 60);

        // 6. Temporizador principal
        matchTask = Bukkit.getScheduler().runTaskTimer(Main.get(), () -> {
            timeLeft--;
            matchBar.setTitle("Tiempo restante: " + formatTime(timeLeft));
            matchBar.setProgress(Math.max(0, (double) timeLeft / totalTime));

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setPlayerListHeader("§bTiempo restante: §e" + formatTime(timeLeft));
            }

            if (timeLeft % 60 == 0 || timeLeft <= 10) {
                Bukkit.broadcastMessage("§bTiempo restante: §e" + formatTime(timeLeft));
            }

            if (timeLeft <= 0) {
                if (gameMap.getGameMode().equals("KOTH") && kothHandler != null) {
                    String winner = kothHandler.getTeamPoints().entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(null);
                    end(winner);
                    return;
                }
                if (equiposTocaron.size() == 1) {
                    end(equiposTocaron.iterator().next());
                    return;
                }
                end(null);
            }
        }, 20L, 20L);

        // 7. MODO KOTH
        if (gameMap.getGameMode().equals("KOTH") && gameMap.getKothConfig() != null) {
            kothHandler = new KOTHHandler(Main.get(), gameMap.getKothConfig());
            kothHandler.setGameMap(gameMap);
            kothHandler.setMatch(match);
            kothHandler.setOnWin(() -> {
                String winner = kothHandler.getTeamPoints().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                end(winner);
            });
            kothHandler.start();
            KOTHScoreboard kothScoreboard = new KOTHScoreboard(match, kothHandler, gameMap);
            kothHandler.setScoreboard(kothScoreboard);
        }
    }

    public void end(String winnerTeam) {
        if (!started) return;
        started = false;

        if (matchTask != null) matchTask.cancel();
        if (matchBar != null) matchBar.removeAll();
        if (kothHandler != null) kothHandler.stop();

        MatchManager.get().setInGame(false);
        MatchManager.get().setNeedsCycle(true);

        // Mensaje de victoria
        if (winnerTeam != null) {
            Bukkit.broadcastMessage("§6¡El equipo §e" + winnerTeam + " §6ha ganado la partida!");
        } else {
            Bukkit.broadcastMessage("§6¡La partida ha terminado en empate!");
        }

        // Stats y sacar de equipo
        for (Player p : Bukkit.getOnlinePlayers()) {
            String team = match.getTeamOf(p);
            if (team != null) {
                JugadorEntity j = StatsManager.get().getJugador(p.getUniqueId(), p.getName());
                if (winnerTeam != null && team.equalsIgnoreCase(winnerTeam)) {
                    j.addWin();
                } else {
                    j.addLoss();
                }
                match.quitarDeEquipoSinScoreboard(p);
            }

            p.getInventory().clear();
            p.getInventory().setArmorContents(new ItemStack[4]);
            p.setGameMode(GameMode.CREATIVE);
            Location spectatorSpawn = gameMap.getSpectatorSpawn();
            if (spectatorSpawn != null) p.teleport(spectatorSpawn);
            p.setPlayerListHeader("§6" + (winnerTeam != null ? "§eGana: §6" + winnerTeam : "§6Empate"));
            p.setPlayerListFooter("§7Esperando siguiente mapa...");
        }
    }

    public void startCountdown(int seconds) {
        if (started) return;

        this.countdown = seconds;

        countdownBar = Bukkit.createBossBar(
                "La partida comienza en " + countdown + "s",
                BarColor.BLUE,
                BarStyle.SOLID
        );
        for (Player p : Bukkit.getOnlinePlayers()) countdownBar.addPlayer(p);

        countdownBar.setProgress(1.0);
        Bukkit.broadcastMessage("§aLa partida comenzará en §e" + countdown + " §asegundos.");

        countdownTask = Bukkit.getScheduler().runTaskTimer(Main.get(), () -> {
            countdown--;
            countdownBar.setTitle("La partida comienza en " + countdown + "s");
            countdownBar.setProgress(Math.max(0, (double) countdown / seconds));

            if (countdown == seconds || countdown == 10 || countdown <= 5) {
                Bukkit.broadcastMessage("§bLa partida comienza en §e" + countdown + "§bs.");
            }

            if (countdown <= 0) {
                countdownTask.cancel();
                countdownBar.removeAll();
                start();
            }
        }, 20L, 20L);

    }


    public void addPlayerToMatchBar(Player p) {
        if (matchBar != null) matchBar.addPlayer(p);
    }

    public boolean isStarted() { return started; }
    public void setWoolScoreboard(WoolScoreboard woolScoreboard) { this.woolScoreboard = woolScoreboard; }
    public WoolScoreboard getWoolScoreboard() { return woolScoreboard; }
    public Set<String> getEquiposTocaron() { return equiposTocaron; }
    public KOTHHandler getKothHandler() { return kothHandler; }
}