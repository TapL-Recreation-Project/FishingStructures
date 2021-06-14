package me.swipez.fishingstructures.utils;

import me.swipez.fishingstructures.FishingStructures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TileState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StructureUtil {

    private static final Random rng = new Random();

    public static void placeWithDissolutionNEW(String name, final Location placementOrigin, JavaPlugin plugin,
                                               LootTable chestsLootTable, EntityType spawnersEntity) {
        File file = new File(plugin.getDataFolder(), "structures/" + name + ".yml");
        FileConfiguration structureFile = new YamlConfiguration();
        try {
            structureFile.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
            return;
        }

        // Maps the layer number to a list of strings, in which every string has the xy coordinates of z-stripe of blocks
        // In other words, this is storing all blocks in each layer
        HashMap<Integer, ArrayList<String>> layerXYsMap = new HashMap<>();
        HashMap<String, ArrayList<Integer>> xyPendingZsMap = new HashMap<>();

        int maxY = Integer.MIN_VALUE;
        Map<Integer, ArrayList<Vector>> layerBlocksPositionsToPlaceMap = new HashMap<>();
        for (String xyKey : structureFile.getKeys(true)) {
            // Don't catch anything not in that format
            if (!xyKey.contains(".") || !xyKey.contains("=")) continue;

            // Gets the y from that key
            String yPart = xyKey.substring(0, xyKey.indexOf('.'));
            int yOffset = Integer.parseInt(yPart.replace("y=", ""));
            String xPart = xyKey.substring(xyKey.indexOf('.') + 1);
            int xOffset = Integer.parseInt(xPart.replace("x=", ""));

            // Let's fill the map with that info
            int layer = Integer.parseInt(yPart.replace("y=", ""));

            List<String> allDataPerZ = structureFile.getStringList(xyKey);
            for (String zData : allDataPerZ) {
                int zOffset = Integer.parseInt(zData.substring(0, zData.indexOf(';')));
                xyPendingZsMap.putIfAbsent(xyKey, new ArrayList<>());
                xyPendingZsMap.get(xyKey).add(zOffset);
                layerBlocksPositionsToPlaceMap.putIfAbsent(layer, new ArrayList<>());
                layerBlocksPositionsToPlaceMap.get(layer).add(new Vector(xOffset, yOffset, zOffset));
            }

            // determine maxY
            if (layer > maxY) maxY = layer;

            layerXYsMap.putIfAbsent(layer, new ArrayList<>());
            layerXYsMap.get(layer).add(xyKey);
        }

        HashMap<Integer, ArrayList<Vector>> layerPendingBlocksMap = new HashMap<>(layerBlocksPositionsToPlaceMap);

        int placementDelay = 0;
        boolean firstPass = true;
        while (isThereSomethingLeftToPlaceNEW(layerPendingBlocksMap)) {
            for (int y = 0; y <= maxY; y++) {
                // Get all blocks for that layer
                ArrayList<Vector> layerPendingBlocks = layerPendingBlocksMap.get(y);
                if (layerPendingBlocks == null || layerPendingBlocks.size() == 0) continue;

                Collections.shuffle(layerPendingBlocks);

                // Pick x, y, z blocks to place
                int numBlocks;

                if (firstPass) {
                    // Place most blocks right now, for huge structures (e.g. bastions)
                    numBlocks = (int) (layerPendingBlocks.size() * 0.5);
                } else {
                    numBlocks = Math.max(30, layerPendingBlocks.size() / 4);
                }

                List<Vector> blocksToPlace = new ArrayList<>();
                for (int i = 0; i < numBlocks; i++) {
                    if (layerPendingBlocks.size() == 0) break;
                    blocksToPlace.add(layerPendingBlocks.get(0));
                    layerPendingBlocks.remove(0);
                }

                placementDelay++;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Vector block : blocksToPlace) {
                            String key = "y=" + block.getBlockY() + ".x=" + block.getBlockX();
                            List<TileState> tileEntities = placeStructureBlock(structureFile, key, block.getBlockZ(), placementOrigin).getTileEntities();

                            for (BlockState state : tileEntities) {
                                if (chestsLootTable != null && state.getBlock().getType() == Material.CHEST) {
                                    setLootTable((Chest) state, chestsLootTable);
                                }
                                if (spawnersEntity != null && state.getBlock().getType() == Material.SPAWNER) {
                                    setSpawnerType((CreatureSpawner) state, spawnersEntity);
                                }
                            }
                        }
                    }
                }.runTaskLater(plugin, 1L * placementDelay);
            }
            firstPass = false;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnMobs(placementOrigin, structureFile);
            }
        }.runTaskLater(plugin, 1L * placementDelay);
    }

    static void spawnMobs(Location origin, FileConfiguration structureFile) {
        Object mobCountObj = structureFile.get("mobcount");
        if (mobCountObj == null) return;
        int mobCount = structureFile.getInt("mobcount");
        for (int i = 0; i < mobCount; i ++) {
            EntityType type = EntityType.valueOf(structureFile.getString("mobs." + i + ".type"));
            String stringLoc = structureFile.getString("mobs." + i + ".pos");

            double x = Double.parseDouble(stringLoc.split(";")[0]);
            double y = Double.parseDouble(stringLoc.split(";")[1]);
            double z = Double.parseDouble(stringLoc.split(";")[2]);

            origin.getWorld().spawnEntity(origin.clone().add(x, y, z), type);
        }
    }

    private static boolean isThereSomethingLeftToPlaceNEW(HashMap<Integer, ArrayList<Vector>> layerPendingBlocksMap) {
        for (Map.Entry<Integer,ArrayList<Vector>> entry : layerPendingBlocksMap.entrySet()) {
            if (entry.getValue().size() > 0) return true;
        }
        return false;
    }

    public static StructurePlacement placeLayer(FileConfiguration structureFile, final Location placementOrigin, int layer) {
        ArrayList<String> coordKeys = new ArrayList<>();
        for (String key : structureFile.getKeys(true)) {
            if (!key.contains(".")) continue;
            String yPart = key.substring(0, key.indexOf('.'));
            int yOffset = Integer.parseInt(yPart.replace("y=", ""));
            if (yOffset == layer) {
                coordKeys.add(key);
            }
        }
        return placeStructure(structureFile, coordKeys, placementOrigin);
    }

    public static StructurePlacement placeStructure(FileConfiguration structureFile, final Location placementOrigin) {
        ArrayList<String> coordKeys = new ArrayList<>();
        for (String key : structureFile.getKeys(true)) {
            if (key.contains(".")) {
                coordKeys.add(key);
            }
        }
        return placeStructure(structureFile, coordKeys, placementOrigin);
    }

    private static StructurePlacement placeStructureBlock(FileConfiguration structureFile, String xyKey, int z, final Location placementOrigin) {
        ArrayList<TileState> tileEntities = new ArrayList<>();

        String yPart = xyKey.substring(0, xyKey.indexOf('.'));
        int yOffset = Integer.parseInt(yPart.replace("y=", ""));
        String xPart = xyKey.substring(xyKey.indexOf('.') + 1);
        int xOffset = Integer.parseInt(xPart.replace("x=", ""));

        for (String zPart : structureFile.getStringList(xyKey)) {
            int zOffset = Integer.parseInt(zPart.substring(0, zPart.indexOf(';')));
            if (z != zOffset) continue;

            String blockDataStr = zPart.substring(zPart.indexOf(';') + 1);
            Location placement = placementOrigin.clone();
            placement = placement.add(-xOffset, yOffset, -zOffset);
            placement.getBlock().setBlockData(Bukkit.getServer().createBlockData(blockDataStr), false);
            if (placement.getBlock().getState() instanceof TileState) {
                tileEntities.add((TileState) placement.getBlock().getState());
            }
            if (placement.getBlock().getType() == Material.YELLOW_CONCRETE) {
                placement.getBlock().setType(Material.AIR);
            }

            if (z == zOffset) break;
        }
        return new StructurePlacement(tileEntities);
    }

    private static StructurePlacement placeStructure(FileConfiguration structureFile, List<String> serializedCoordinates, final Location placementOrigin) {
        ArrayList<TileState> tileEntities = new ArrayList<>();
        for (String key : serializedCoordinates) {
            String yPart = key.substring(0, key.indexOf('.'));
            int yOffset = Integer.parseInt(yPart.replace("y=", ""));
            String xPart = key.substring(key.indexOf('.') + 1);
            int xOffset = Integer.parseInt(xPart.replace("x=", ""));

            for (String zPart : structureFile.getStringList(key)) {
                int zOffset = Integer.parseInt(zPart.substring(0, zPart.indexOf(';')));
                String blockDataStr = zPart.substring(zPart.indexOf(';') + 1);
                Location placement = placementOrigin.clone();
                placement = placement.add(- xOffset, yOffset, -zOffset);
                placement.getBlock().setBlockData(Bukkit.getServer().createBlockData(blockDataStr), false);
                if (placement.getBlock().getState() instanceof TileState) {
                    tileEntities.add((TileState) placement.getBlock().getState());
                }
                if (placement.getBlock().getType() == Material.YELLOW_CONCRETE) {
                    placement.getBlock().setType(Material.AIR);
                }
            }
        }
        return new StructurePlacement(tileEntities);
    }

    public static void loadAndSlowlyPlace(String name, Location placementOrigin, int maxY,
                                              EntityType spawnersEntity, LootTable chestsLootTable, JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "structures/" + name + ".yml");
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
            return;
        }
        for (int y = 0; y <= maxY; y++) {
            final int layer = y;
            new BukkitRunnable() {
                @Override
                public void run() {
                  List<TileState> tileEntities = placeLayer(fileConfiguration, placementOrigin, layer).getTileEntities();
                  for (BlockState state : tileEntities) {
                      if (chestsLootTable != null && state.getBlock().getType() == Material.CHEST) {
                          setLootTable((Chest) state, chestsLootTable);
                      }
                      if (spawnersEntity != null && state.getBlock().getType() == Material.SPAWNER) {
                          setSpawnerType((CreatureSpawner) state, spawnersEntity);
                      }
                  }
                }
            }.runTaskLater(plugin, 5L * y);
        }
    }

    public static void setLootTable(Chest chest, LootTable lootTable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                chest.setLootTable(lootTable);
                chest.update(true);
            }
        }.runTaskLater(FishingStructures.plugin, 1);
    }

    public static void setSpawnerType(CreatureSpawner spawner, EntityType entityType) {
        spawner.setMaxSpawnDelay(300);
        spawner.setMaxNearbyEntities(6);
        spawner.setSpawnedType(entityType);
        spawner.update();
    }
}