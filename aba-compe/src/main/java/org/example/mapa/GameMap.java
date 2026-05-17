package org.example.mapa;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.example.Main;
import org.example.entity.mapa.*;
import org.example.entity.objetivo.ControlPointEntity;
import org.example.entity.objetivo.JumpPadEntity;
import org.example.gamemode.ctw.CTWConfig;
import org.example.gamemode.koth.KOTHConfig;
import org.example.entity.objetivo.WoolEntity;

import java.io.File;
import java.util.*;
import org.bukkit.util.Vector;

public class GameMap {

    private boolean buildingAllowed = true;
    public boolean isBuildingAllowed() { return buildingAllowed; }
    private final List<ItemStack> spawnKit = new ArrayList<>();
    private final List<ItemStack> killRewards = new ArrayList<>();
    private final List<BlockDropEntity> blockDrops = new ArrayList<>();
    private final List<Material> itemRemove = new ArrayList<>();
    private final Map<String, RegionEntity> regions = new HashMap<>();
    private final List<String> neverBuildRegions = new ArrayList<>();
    private final List<RegionEntity> buildRegions = new ArrayList<>();
    private final List<RegionEntity> excludeRegions = new ArrayList<>();
    private final List<WoolEntity> wools = new ArrayList<>();
    private final List<Material> deleteOnStart = new ArrayList<>();
    private final List<EnterRuleEntity> enterRules = new ArrayList<>();
    private int maxBuildHeight = 0;
    private int minBuildHeight = 0;
    private final List<ItemStack> spawnArmor = new ArrayList<>(Arrays.asList(null, null, null, null));
    private final Map<String, List<ItemStack>> teamArmor = new HashMap<>();
    private final Main plugin;
    private final String mapName;
    private Location spectatorSpawn;
    private MapaEntity mapaEntity;
    private final Map<String, List<Location>> teamSpawns = new HashMap<>();
    private String gameMode = "CTW";
    private KOTHConfig kothConfig;
    private CTWConfig ctwConfig;
    private final List<JumpPadEntity> jumpPads = new ArrayList<>();
    private final Map<String, Material> originalFloorBlocks = new HashMap<>();

    public GameMap(Main plugin, String mapName) {
        this.plugin = plugin;
        this.mapName = mapName;
    }

    public void load() {
        File mapFile = new File(plugin.getDataFolder() + "/maps/" + mapName + "/map.yml");
        System.out.println("Cargando mapa desde: " + mapFile.getPath());
        System.out.println("Existe el archivo?: " + mapFile.exists());

        if (!mapFile.exists()) {
            Bukkit.getLogger().severe("No se encontró el mapa: " + mapFile.getPath());
            return;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(mapFile);

        World world = Bukkit.getWorld(mapName);
        if (world == null) world = Bukkit.getWorlds().get(0);

        double sx = cfg.getDouble("map.spectator.x");
        double sy = cfg.getDouble("map.spectator.y");
        double sz = cfg.getDouble("map.spectator.z");
        spectatorSpawn = new Location(world, sx, sy, sz);

        mapaEntity = new MapaEntity(1, cfg.getString("map.name"));
        buildingAllowed = cfg.getBoolean("map.building", true);
        putTeams(cfg);
        putSpawns(cfg, world);
        putKillReward(cfg);
        putDeleteOnStart(cfg);
        blockDropsMap(cfg);
        kitTeam(cfg);
        itemRemoves(cfg);
        putRegions(cfg);
        putWools(cfg);
        putArmor(cfg);

        gameMode = cfg.getString("map.mode", "CTW").toUpperCase();
        if (gameMode.equals("KOTH")) putKoth(cfg,this);
        if (gameMode.equals("CTW")) putCTW(cfg);
    }

    private void putCTW(YamlConfiguration cfg) {
        int points = cfg.getInt("map.ctw.points-per-wool", 1);
        boolean recover = cfg.getBoolean("map.ctw.allow-recover", true);
        boolean anywhere = cfg.getBoolean("map.ctw.allow-place-anywhere", false);
        ctwConfig = new CTWConfig(wools, points, recover, anywhere);
    }

    private void putKoth(YamlConfiguration cfg, GameMap gameMap) {

        int pointsToWin = cfg.getInt("map.points-to-win", 2000);
        int captureTime = cfg.getInt("map.capture-time", 8);
        boolean incremental = cfg.getBoolean("map.incremental", true);
        boolean neutralState = cfg.getBoolean("map.neutral-state", true);
        boolean scaledTime = cfg.getBoolean("map.scaled-time", false);
        double timeMultiplier = cfg.getDouble("map.time-multiplier", 1.0);
        String floorMatStr = cfg.getString("map.floor-material", "white_terracotta");

        List<ControlPointEntity> points = new ArrayList<>();

        List<Map<?, ?>> list = cfg.getMapList("map.control-points");
        if (list == null || list.isEmpty()) {
            System.out.println("No control-points en config");
            return;
        }

        for (Map<?, ?> cpData : list) {

            String name = (String) cpData.get("name");
            if (name == null) continue;

            int pts = cpData.containsKey("points")
                    ? ((Number) cpData.get("points")).intValue()
                    : 1;

            Map<?, ?> regionData = (Map<?, ?>) cpData.get("capture-region");
            if (regionData == null) continue;

            Object minObj = regionData.get("min");
            Object maxObj = regionData.get("max");

            if (minObj == null || maxObj == null) {
                System.out.println("CP sin min/max: " + name);
                continue;
            }

            String[] min = minObj.toString().split(",");
            String[] max = maxObj.toString().split(",");

            RegionEntity captureRegion = new RegionEntity(
                    name, "cuboid",
                    Double.parseDouble(min[0].trim()), Double.parseDouble(min[1].trim()), Double.parseDouble(min[2].trim()),
                    Double.parseDouble(max[0].trim()), Double.parseDouble(max[1].trim()), Double.parseDouble(max[2].trim())
            );

            // Letter (fill region)
            RegionEntity fillRegion = null;
            Map<?, ?> letterData = (Map<?, ?>) regionData.get("letter");
            if (letterData != null) {
                Object lMinObj = letterData.get("min");
                Object lMaxObj = letterData.get("max");
                if (lMinObj != null && lMaxObj != null) {
                    String[] lMin = lMinObj.toString().split(",");
                    String[] lMax = lMaxObj.toString().split(",");
                    fillRegion = new RegionEntity(
                            name + "_fill", "cuboid",
                            Double.parseDouble(lMin[0].trim()), Double.parseDouble(lMin[1].trim()), Double.parseDouble(lMin[2].trim()),
                            Double.parseDouble(lMax[0].trim()), Double.parseDouble(lMax[1].trim()), Double.parseDouble(lMax[2].trim())
                    );
                }
            }

            // Floor region
            RegionEntity floorRegion = null;
            Map<?, ?> floorData = (Map<?, ?>) regionData.get("floor");
            if (floorData != null) {
                Object fMinObj = floorData.get("min");
                Object fMaxObj = floorData.get("max");
                if (fMinObj != null && fMaxObj != null) {
                    String[] fMin = fMinObj.toString().split(",");
                    String[] fMax = fMaxObj.toString().split(",");
                    floorRegion = new RegionEntity(
                            name + "_floor", "cuboid",
                            Double.parseDouble(fMin[0].trim()), Double.parseDouble(fMin[1].trim()), Double.parseDouble(fMin[2].trim()),
                            Double.parseDouble(fMax[0].trim()), Double.parseDouble(fMax[1].trim()), Double.parseDouble(fMax[2].trim())
                    );
                }
            }

            points.add(new ControlPointEntity(name, pts, captureRegion, fillRegion, floorRegion, floorMatStr, captureTime));
            System.out.println("ControlPoint cargado: " + name);

            // Jump pad
            Map<?, ?> jumpData = (Map<?, ?>) cpData.get("jump_block");
            if (jumpData == null) continue;

            World world = Bukkit.getWorld(gameMap.getMapName());
            if (world == null) {
                System.out.println("World null");
                continue;
            }

            List<Location> blocks = new ArrayList<>();

            Object rawBlocks = jumpData.get("blocks");
            if (rawBlocks instanceof List<?>) {
                for (Object obj : (List<?>) rawBlocks) {
                    String[] p = obj.toString().split(",");
                    if (p.length < 3) continue;
                    blocks.add(new Location(world,
                            Integer.parseInt(p[0].trim()),
                            Integer.parseInt(p[1].trim()),
                            Integer.parseInt(p[2].trim())
                    ));
                }
            } else {
                Object minJ = jumpData.get("min");
                Object maxJ = jumpData.get("max");
                if (minJ == null || maxJ == null) continue;

                String[] jMin = minJ.toString().split(",");
                String[] jMax = maxJ.toString().split(",");
                if (jMin.length < 3 || jMax.length < 3) continue;

                blocks.add(new Location(world,
                        Integer.parseInt(jMin[0].trim()),
                        Integer.parseInt(jMin[1].trim()),
                        Integer.parseInt(jMin[2].trim())
                ));
                blocks.add(new Location(world,
                        Integer.parseInt(jMax[0].trim()),
                        Integer.parseInt(jMax[1].trim()),
                        Integer.parseInt(jMax[2].trim())
                ));
            }

            double force = jumpData.containsKey("force")
                    ? ((Number) jumpData.get("force")).doubleValue()
                    : 1.5;

            org.bukkit.util.Vector direction = new org.bukkit.util.Vector(0, 1, 0);
            if (jumpData.get("direction") != null) {
                String[] dir = jumpData.get("direction").toString().split(",");
                if (dir.length == 3) {
                    direction = new org.bukkit.util.Vector(
                            Double.parseDouble(dir[0].trim()),
                            Double.parseDouble(dir[1].trim()),
                            Double.parseDouble(dir[2].trim())
                    );
                }
            }

            jumpPads.add(new JumpPadEntity(blocks, force, direction));
            System.out.println("JumpPad cargado: " + name + " blocks=" + blocks.size() + " force=" + force + " dir=" + direction);
        }

        kothConfig = new KOTHConfig(pointsToWin, incremental, neutralState, scaledTime, timeMultiplier, points);
        System.out.println("KOTHConfig cargado: " + points.size());
    }

    private void putArmor(YamlConfiguration cfg) {
        if (cfg.isConfigurationSection("map.kits.spawn.armor")) {
            List<ItemStack> armor = loadArmorSection(cfg, "map.kits.spawn.armor", null);
            spawnArmor.clear();
            spawnArmor.addAll(armor);
            System.out.println("Armor spawn cargada: " + armor.size() + " piezas");
        }

        if (cfg.isConfigurationSection("map.kits")) {
            for (String kitId : cfg.getConfigurationSection("map.kits").getKeys(false)) {
                if (kitId.equals("spawn")) continue;

                String kitPath = "map.kits." + kitId;
                List<ItemStack> parentArmor = new ArrayList<>(Arrays.asList(null, null, null, null));

                if (cfg.isList(kitPath + ".parents")) {
                    for (String parent : cfg.getStringList(kitPath + ".parents")) {
                        if (parent.equals("spawn")) parentArmor = new ArrayList<>(spawnArmor);
                    }
                }

                List<ItemStack> armor = loadArmorSection(cfg, kitPath + ".armor", parentArmor);
                teamArmor.put(kitId.toLowerCase(), armor);
                System.out.println("Armor kit '" + kitId + "' cargada: " + armor.size() + " piezas");
            }
        }
    }

    private List<ItemStack> loadArmorSection(YamlConfiguration cfg, String path, List<ItemStack> parentArmor) {
        String[] slots = {"boots", "leggings", "chestplate", "helmet"};
        List<ItemStack> result = new ArrayList<>(Arrays.asList(null, null, null, null));

        if (parentArmor != null) {
            for (int i = 0; i < 4; i++) result.set(i, parentArmor.get(i));
        }

        if (!cfg.isConfigurationSection(path)) return result;

        for (int i = 0; i < slots.length; i++) {
            String slot = slots[i];
            String slotPath = path + "." + slot;
            if (!cfg.isConfigurationSection(slotPath)) continue;

            String matName = cfg.getString(slotPath + ".material");
            if (matName == null) continue;

            Material mat = Material.matchMaterial(matName);
            if (mat == null) {
                System.out.println("ERROR armor material inválido: " + matName);
                continue;
            }

            boolean unbreakable = cfg.getBoolean(slotPath + ".unbreakable", false);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setUnbreakable(unbreakable);

                if (meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta leatherMeta) {
                    String colorHex = cfg.getString(slotPath + ".color");
                    if (colorHex != null) {
                        try {
                            int rgb = Integer.parseInt(colorHex, 16);
                            leatherMeta.setColor(Color.fromRGB(rgb));
                        } catch (NumberFormatException e) {
                            System.out.println("ERROR color inválido: " + colorHex);
                        }
                    }
                }

                if (cfg.isConfigurationSection(slotPath + ".enchantments")) {
                    for (String enchantName : cfg.getConfigurationSection(slotPath + ".enchantments").getKeys(false)) {
                        int level = cfg.getInt(slotPath + ".enchantments." + enchantName);
                        org.bukkit.enchantments.Enchantment enchantment = Registry.ENCHANTMENT.get(
                                org.bukkit.NamespacedKey.minecraft(enchantName)
                        );
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        } else {
                            System.out.println("ERROR encantamiento inválido en armor: " + enchantName);
                        }
                    }
                }

                item.setItemMeta(meta);
            }

            result.set(i, item);
            System.out.println("Armor pieza cargada: " + slot + " → " + matName);
        }

        return result;
    }

    private void putTeams(YamlConfiguration cfg) {
        if (cfg.isConfigurationSection("map.teams")) {
            for (String key : cfg.getConfigurationSection("map.teams").getKeys(false)) {
                String id = cfg.getString("map.teams." + key + ".id");
                String color = cfg.getString("map.teams." + key + ".color");
                int max = cfg.getInt("map.teams." + key + ".max");
                mapaEntity.addEquipo(new EquipoEntity(id, color, max));
            }
        }
    }

    private void putDeleteOnStart(YamlConfiguration cfg) {
        if (cfg.isList("map.delete-on-start")) {
            for (Map<?, ?> data : cfg.getMapList("map.delete-on-start")) {
                String matName = (String) data.get("material");
                Material mat = Material.matchMaterial(matName);
                if (mat == null) continue;
                deleteOnStart.add(mat);
                System.out.println("DeleteOnStart: " + matName);
            }
        }
    }

    private void putKillReward(YamlConfiguration cfg) {
        if (cfg.isList("map.kill-rewards")) {
            for (Map<?, ?> rewardData : cfg.getMapList("map.kill-rewards")) {
                if (rewardData.containsKey("items")) {
                    for (Map<?, ?> itemData : (List<Map<?, ?>>) rewardData.get("items")) {
                        String matName = (String) itemData.get("material");
                        int amount = itemData.containsKey("amount") ? ((Number) itemData.get("amount")).intValue() : 1;
                        Material mat = Material.matchMaterial(matName);
                        if (mat == null) {
                            System.out.println("ERROR kill-reward material inválido: " + matName);
                            continue;
                        }
                        killRewards.add(new ItemStack(mat, amount));
                    }
                }
            }
        }
    }

    private void putSpawns(YamlConfiguration cfg, World world) {
        if (cfg.isConfigurationSection("map.spawns")) {
            for (String teamId : cfg.getConfigurationSection("map.spawns").getKeys(false)) {
                List<Location> list = new ArrayList<>();
                for (Map<?, ?> spawnData : cfg.getMapList("map.spawns." + teamId)) {
                    double x = ((Number) spawnData.get("x")).doubleValue();
                    double y = ((Number) spawnData.get("y")).doubleValue();
                    double z = ((Number) spawnData.get("z")).doubleValue();
                    float yaw = spawnData.containsKey("yaw") ? ((Number) spawnData.get("yaw")).floatValue() : 0f;
                    list.add(new Location(world, x, y, z, yaw, 0f));
                }
                teamSpawns.put(teamId.toLowerCase(), list);
            }
        }
    }

    private void blockDropsMap(YamlConfiguration cfg) {
        if (cfg.isList("map.block-drops")) {
            System.out.println("Block-drops encontrados: " + cfg.getMapList("map.block-drops").size());
            for (Map<?, ?> dropData : cfg.getMapList("map.block-drops")) {
                boolean wrongTool = dropData.containsKey("wrong-tool") && Boolean.TRUE.equals(dropData.get("wrong-tool"));

                Map<?, ?> filter = (Map<?, ?>) dropData.get("filter");
                if (filter == null) continue;

                List<?> materials = (List<?>) filter.get("materials");
                if (materials == null) continue;

                List<ItemStack> drops = new ArrayList<>();
                for (Map<?, ?> itemData : (List<Map<?, ?>>) dropData.get("drops")) {
                    String matName = (String) itemData.get("material");
                    int amount = itemData.containsKey("amount") ? ((Number) itemData.get("amount")).intValue() : 1;
                    Material mat = Material.matchMaterial(matName);
                    if (mat != null) drops.add(new ItemStack(mat, amount));
                    else System.out.println("ERROR block-drop material inválido: " + matName);
                }

                List<Material> tools = new ArrayList<>();
                if (dropData.containsKey("tools")) {
                    for (String toolName : (List<String>) dropData.get("tools")) {
                        Material mat = Material.matchMaterial(toolName);
                        if (mat != null) tools.add(mat);
                        else System.out.println("ERROR tool inválida: " + toolName);
                    }
                }

                for (Object matName : materials) {
                    Material mat = Material.matchMaterial((String) matName);
                    if (mat != null) blockDrops.add(new BlockDropEntity(mat, drops, wrongTool, tools));
                    else System.out.println("ERROR block-drop filter material inválido: " + matName);
                }
            }
        }
    }

    private void kitTeam(YamlConfiguration cfg) {
        if (cfg.isList("map.kits.spawn.items")) {
            List<Map<?, ?>> items = cfg.getMapList("map.kits.spawn.items");
            System.out.println("Items encontrados: " + items.size());

            for (Map<?, ?> itemData : items) {
                int slot = ((Number) itemData.get("slot")).intValue();
                String matName = (String) itemData.get("material");
                boolean unbreakable = itemData.containsKey("unbreakable") && Boolean.TRUE.equals(itemData.get("unbreakable"));
                int amount = itemData.containsKey("amount") ? ((Number) itemData.get("amount")).intValue() : 1;

                Material mat = Material.matchMaterial(matName);
                if (mat == null) {
                    System.out.println("ERROR: Material inválido: " + matName);
                    continue;
                }

                ItemStack item = new ItemStack(mat, amount);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setUnbreakable(unbreakable);
                    if (itemData.containsKey("enchantments")) {
                        Map<?, ?> enchants = (Map<?, ?>) itemData.get("enchantments");
                        for (Map.Entry<?, ?> entry : enchants.entrySet()) {
                            String enchantName = (String) entry.getKey();
                            int level = ((Number) entry.getValue()).intValue();
                            org.bukkit.enchantments.Enchantment enchantment = Registry.ENCHANTMENT.get(
                                    org.bukkit.NamespacedKey.minecraft(enchantName)
                            );
                            if (enchantment != null) meta.addEnchant(enchantment, level, true);
                            else System.out.println("ERROR: Encantamiento inválido: " + enchantName);
                        }
                    }
                    item.setItemMeta(meta);
                }

                while (spawnKit.size() <= slot) spawnKit.add(null);
                spawnKit.set(slot, item);
                System.out.println("Item añadido: " + matName + " en slot " + slot);
            }
        }
    }

    private void itemRemoves(YamlConfiguration cfg) {
        if (cfg.isList("map.itemremove.items")) {
            for (String matName : cfg.getStringList("map.itemremove.items")) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) itemRemove.add(mat);
                else System.out.println("ERROR itemremove material inválido: " + matName);
            }
            System.out.println("ItemRemove cargado: " + itemRemove.size() + " materiales");
        }
    }

    private void putWools(YamlConfiguration cfg) {
        if (!cfg.isList("map.wools")) {
            System.out.println("[CTW] No se encontraron wools en map.wools");
            return;
        }

        for (Map<?, ?> woolData : cfg.getMapList("map.wools")) {
            String team = (String) woolData.get("team");
            String color = (String) woolData.get("color");

            String[] loc = ((String) woolData.get("location")).split(",");
            double locX = Double.parseDouble(loc[0].trim());
            double locY = Double.parseDouble(loc[1].trim());
            double locZ = Double.parseDouble(loc[2].trim());

            Map<?, ?> monument = (Map<?, ?>) woolData.get("monument");
            String[] mon = ((String) monument.get("block")).split(",");
            double monX = Double.parseDouble(mon[0].trim());
            double monY = Double.parseDouble(mon[1].trim());
            double monZ = Double.parseDouble(mon[2].trim());

            Material mat = Material.matchMaterial(color + "_wool");
            if (mat == null) {
                System.out.println("[CTW] ERROR wool color inválido: " + color);
                continue;
            }

            wools.add(new WoolEntity(team, mat, locX, locY, locZ, monX, monY, monZ));
            System.out.println("[CTW] Wool cargada: team=" + team + " color=" + color + " mon=" + monX + "," + monY + "," + monZ);
        }
    }

    private void putRegions(YamlConfiguration cfg) {
        // NEVER BUILD y ENTER RULES desde apply
        if (cfg.isList("map.regions.apply")) {
            for (Map<?, ?> applyData : cfg.getMapList("map.regions.apply")) {

                // NEVER BUILD
                if ("never".equals(applyData.get("block"))) {
                    Object regionObj = applyData.get("region");
                    if (regionObj instanceof List) {
                        for (Object r : (List<?>) regionObj) {
                            neverBuildRegions.add((String) r);
                            System.out.println("Never build region: " + r);
                        }
                    } else if (regionObj instanceof String) {
                        neverBuildRegions.add((String) regionObj);
                        System.out.println("Never build region: " + regionObj);
                    }
                }

                // ENTER RULES
                if (applyData.containsKey("enter")) {
                    String filterName = (String) applyData.get("enter");
                    String message = applyData.containsKey("message") ? (String) applyData.get("message") : "§cNo puedes entrar aquí.";

                    String teamId = null;
                    if (filterName.startsWith("only-")) {
                        teamId = filterName.replace("only-", "");
                    }
                    if (teamId == null) continue;

                    Object regionObj = applyData.get("region");
                    List<String> regionIds = new ArrayList<>();
                    if (regionObj instanceof List) {
                        for (Object r : (List<?>) regionObj) regionIds.add((String) r);
                    } else if (regionObj instanceof String) {
                        regionIds.add((String) regionObj);
                    }

                    // Las regiones pueden no estar cargadas aún, las guardamos por ID para resolver después
                    for (String regionId : regionIds) {
                        // Guardamos como EnterRule pendiente con regionId como placeholder
                        enterRules.add(new EnterRuleEntity(teamId, regionId, message));
                        System.out.println("EnterRule registrada: solo " + teamId + " en " + regionId);
                    }
                }
            }
        }

        // REGIONES CUBOID SIMPLES
        if (cfg.isConfigurationSection("map.regions")) {
            for (String regionId : cfg.getConfigurationSection("map.regions").getKeys(false)) {
                String path = "map.regions." + regionId;
                String type = cfg.getString(path + ".type");
                if (type == null) continue;

                String minStr = cfg.getString(path + ".min");
                String maxStr = cfg.getString(path + ".max");
                if (minStr == null || maxStr == null) continue;

                if (type.equals("cuboid")) {
                    String[] min = minStr.split(",");
                    String[] max = maxStr.split(",");
                    regions.put(regionId, new RegionEntity(regionId, "cuboid",
                            Double.parseDouble(min[0].trim()), Double.parseDouble(min[1].trim()), Double.parseDouble(min[2].trim()),
                            Double.parseDouble(max[0].trim()), Double.parseDouble(max[1].trim()), Double.parseDouble(max[2].trim())
                    ));
                    System.out.println("Región cuboid cargada: " + regionId);
                }
            }
        }

        // BUILD REGIONS
        int regionCounter = 0;
        for (Map<?, ?> child : cfg.getMapList("map.regions.regions.children")) {
            String id = (String) child.get("id");
            String type = (String) child.get("type");
            if (type == null) continue;

            String minStr = (String) child.get("min");
            String maxStr = (String) child.get("max");
            if (minStr == null || maxStr == null) continue;

            if (id == null) id = "region_" + regionCounter++;

            RegionEntity region = null;

            if (type.equals("cuboid")) {
                String[] min = minStr.split(",");
                String[] max = maxStr.split(",");
                region = new RegionEntity(id, "cuboid",
                        Double.parseDouble(min[0].trim()), Double.parseDouble(min[1].trim()), Double.parseDouble(min[2].trim()),
                        Double.parseDouble(max[0].trim()), Double.parseDouble(max[1].trim()), Double.parseDouble(max[2].trim())
                );
            } else if (type.equals("rectangle")) {
                String[] min = minStr.split(",");
                String[] max = maxStr.split(",");
                region = new RegionEntity(id, "rectangle",
                        Double.parseDouble(min[0].trim()), 0, Double.parseDouble(min[1].trim()),
                        Double.parseDouble(max[0].trim()), 0, Double.parseDouble(max[1].trim())
                );
            }

            if (region != null) {
                buildRegions.add(region);
                if (child.containsKey("id")) regions.put(id, region);
                System.out.println("BuildRegion cargada: " + id);
            }
        }

        // EXCLUDE REGIONS
        for (Map<?, ?> subtract : cfg.getMapList("map.regions.build-region.subtract")) {
            String id = (String) subtract.get("id");
            String type = (String) subtract.get("type");
            if (type == null) continue;

            String minStr = (String) subtract.get("min");
            String maxStr = (String) subtract.get("max");
            if (minStr == null || maxStr == null) continue;

            if (id == null) id = "exclude_" + excludeRegions.size();

            String[] min = minStr.split(",");
            String[] max = maxStr.split(",");
            RegionEntity region = new RegionEntity(id, "cuboid",
                    Double.parseDouble(min[0].trim()), Double.parseDouble(min[1].trim()), Double.parseDouble(min[2].trim()),
                    Double.parseDouble(max[0].trim()), Double.parseDouble(max[1].trim()), Double.parseDouble(max[2].trim())
            );
            excludeRegions.add(region);
            System.out.println("ExcludeRegion cargada: " + id);
        }

        // Resolver EnterRules con las regiones ya cargadas
        for (EnterRuleEntity rule : enterRules) {
            if (rule.getRegion() == null) {
                RegionEntity region = regions.get(rule.getRegionId());
                if (region != null) {
                    rule.setRegion(region);
                    System.out.println("EnterRule resuelta: solo " + rule.getTeamId() + " en " + rule.getRegionId());
                } else {
                    System.out.println("ERROR EnterRule: región no encontrada: " + rule.getRegionId());
                }
            }
        }

        minBuildHeight = cfg.getInt("map.regions.minBuildHeight", 0);
        maxBuildHeight = cfg.getInt("map.maxbuildheight", 0);
    }

    // Getters
    public Location getSpectatorSpawn() { return spectatorSpawn; }
    public MapaEntity getMapaEntity() { return mapaEntity; }
    public List<Location> getTeamSpawns(String teamId) { return teamSpawns.getOrDefault(teamId.toLowerCase(), new ArrayList<>()); }
    public List<ItemStack> getSpawnKit() { return spawnKit; }
    public List<ItemStack> getKillRewards() { return killRewards; }
    public List<BlockDropEntity> getBlockDrops() { return blockDrops; }
    public List<Material> getItemRemove() { return itemRemove; }
    public Map<String, RegionEntity> getRegions() { return regions; }
    public List<String> getNeverBuildRegions() { return neverBuildRegions; }
    public int getMaxBuildHeight() { return maxBuildHeight; }
    public int getMinBuildHeight() { return minBuildHeight; }
    public List<WoolEntity> getWools() { return wools; }
    public List<RegionEntity> getExcludeRegions() { return excludeRegions; }
    public List<RegionEntity> getBuildRegions() { return buildRegions; }
    public String getMapName() { return mapName; }
    public List<Material> getDeleteOnStart() { return deleteOnStart; }
    public List<ItemStack> getSpawnArmor() { return spawnArmor; }
    public List<ItemStack> getTeamArmor(String kitId) { return teamArmor.getOrDefault(kitId.toLowerCase(), spawnArmor); }
    public String getGameMode() { return gameMode; }
    public KOTHConfig getKothConfig() { return kothConfig; }
    public CTWConfig getCtwConfig() { return ctwConfig; }
    public List<EnterRuleEntity> getEnterRules() { return enterRules; }
    public List<JumpPadEntity> getJumpPads() { return jumpPads; }
    public Map<String, Material> getOriginalFloorBlocks() { return originalFloorBlocks; }
}