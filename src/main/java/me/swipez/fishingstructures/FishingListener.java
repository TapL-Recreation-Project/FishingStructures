package me.swipez.fishingstructures;

import me.swipez.fishingstructures.utils.StructureUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTables;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class FishingListener implements Listener {

    Random random = new Random();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        player.setResourcePack("https://cdn.discordapp.com/attachments/812394140577824808/853819975718797332/CustomFishingRods.zip");
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event){
        if (event.getCaught() != null && event.getCaught() instanceof Item){
            Player player = event.getPlayer();
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getItemMeta() == null){
                return;
            }
            if (itemStack.getItemMeta().hasDisplayName()){
                String name = itemStack.getItemMeta().getDisplayName();
                event.getCaught().remove();
                if (name.toLowerCase().contains("bastion")){
                    spawnStructureWithAnimation("bastion", Material.BASALT, ChatColor.LIGHT_PURPLE+"Bastion", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("village")){
                    spawnStructureWithAnimation("villagesmall", Material.OAK_LOG, ChatColor.LIGHT_PURPLE+"Village", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("ship")){
                    spawnStructureWithAnimation("sunken_ship", Material.ACACIA_LOG, ChatColor.LIGHT_PURPLE+"Shipwreck", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("ruined")){
                    spawnStructureWithAnimation("ruined_portal", Material.CRYING_OBSIDIAN, ChatColor.LIGHT_PURPLE+"Ruined Portal", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("desert")){
                    spawnStructureWithAnimation("pyramid", Material.CHISELED_SANDSTONE, ChatColor.LIGHT_PURPLE+"Desert Temple", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("spawner")){
                    spawnStructureWithAnimation("spawner", Material.SPAWNER, ChatColor.LIGHT_PURPLE+"Spawner", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("igloo")){
                    spawnStructureWithAnimation("igloo", Material.SNOW_BLOCK, ChatColor.LIGHT_PURPLE+"Igloo", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("fortress")){
                    spawnStructureWithAnimation("fortress", Material.NETHER_BRICKS, ChatColor.LIGHT_PURPLE+"Fortress", event.getCaught().getLocation());
                }
                if (name.toLowerCase().contains("stronghold")){
                    spawnStructureWithAnimation("stronghold", Material.END_PORTAL_FRAME, ChatColor.LIGHT_PURPLE+"Stronghold", event.getCaught().getLocation());
                }
            }
        }
    }

    public void spawnStructureWithAnimation(String structure, Material itemMaterial, String nameOfItem, Location location){
        ItemStack itemStack = new ItemStack(itemMaterial);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.CHANNELING, 1, true);
        itemStack.setItemMeta(meta);

        Item item = (Item) location.getWorld().spawnEntity(location, EntityType.DROPPED_ITEM);

        item.setItemStack(itemStack);
        item.setGravity(false);
        item.setCustomName(nameOfItem);
        item.setCustomNameVisible(true);
        item.setPickupDelay(4000);

        item.setVelocity(item.getVelocity().setY(item.getVelocity().getY()+3));
        for (int i = 0; i < 50; i++){
            int loop = i;
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    item.setVelocity(item.getVelocity().setY(item.getVelocity().getY()/2));
                    item.setVelocity(item.getVelocity().setX(0));
                    item.setVelocity(item.getVelocity().setZ(0));
                    if (loop == 48){
                       item.remove();
                    }
                }
            }.runTaskLater(FishingStructures.plugin, i*2);
        }
        int randomX = random.nextInt(50);
        int randomZ = random.nextInt(50);
        switch (structure){
            case "bastion":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(randomX+20,3,randomZ+20), FishingStructures.plugin,  LootTables.BASTION_OTHER.getLootTable(), null);
                break;
            case "fortress":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(randomX+20,3, randomZ+20), FishingStructures.plugin,  LootTables.NETHER_BRIDGE.getLootTable(), EntityType.BLAZE);
                break;
            case "igloo":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(random.nextInt(10),18, random.nextInt(10)), FishingStructures.plugin,  LootTables.IGLOO_CHEST.getLootTable(), null);
                break;
            case "spawner":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(random.nextInt(10),0, random.nextInt(10)), FishingStructures.plugin,  LootTables.SIMPLE_DUNGEON.getLootTable(), EntityType.ZOMBIE);
                break;
            case "pyramid":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(random.nextInt(10)+10,15,random.nextInt(10)+10), FishingStructures.plugin,  LootTables.DESERT_PYRAMID.getLootTable(), null);
                break;
            case "sunken_ship":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(random.nextInt(10),3,random.nextInt(10)), FishingStructures.plugin,  LootTables.SHIPWRECK_SUPPLY.getLootTable(), null);
                break;
            case "ruined_portal":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(random.nextInt(10),10,random.nextInt(10)), FishingStructures.plugin,  LootTables.RUINED_PORTAL.getLootTable(), null);
                break;
            case "villagesmall":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(random.nextInt(10),-5,random.nextInt(10)),  FishingStructures.plugin,  LootTables.VILLAGE_WEAPONSMITH.getLootTable(), null);
                break;
            case "stronghold":
                StructureUtil.placeWithDissolutionNEW(structure, location.subtract(40,4,40), FishingStructures.plugin,  LootTables.STRONGHOLD_CORRIDOR.getLootTable(), EntityType.SILVERFISH);
        }
    }
}
