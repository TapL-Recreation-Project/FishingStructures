package me.swipez.fishingstructures.utils;

import me.swipez.fishingstructures.FishingStructures;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class StructurePlacement {
    private final List<TileState> tileEntityList;

    public StructurePlacement(List<TileState> tileEntities) {
        tileEntityList = tileEntities;
    }

    public final List<TileState> getTileEntities() {
        return tileEntityList;
    }

    public void setChestsLootTable(LootTable lootTable) {
        for (TileState tile : tileEntityList) {
            if (!(tile instanceof Chest)) continue;
            final Chest chest = (Chest) tile;
            new BukkitRunnable() {
                @Override
                public void run() {
                    chest.setLootTable(lootTable);
                    chest.update(true);
                }
            }.runTaskLater(FishingStructures.plugin, 1);
        }
    }
}
