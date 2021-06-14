package me.swipez.fishingstructures;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class FishingRods {

    public static ItemStack VILLAGE_ROD = generateFishingRod("Village Fishing Rod", 1);
    public static ItemStack SHIPWRECK_ROD = generateFishingRod("Ship Wreck Fishing Rod", 2);
    public static ItemStack RUINED_PORTAL_ROD = generateFishingRod("Ruined Portal Fishing Rod", 3);
    public static ItemStack DESERT_TEMPLE_ROD = generateFishingRod("Desert Temple Fishing Rod", 4);
    public static ItemStack SPAWNER_ROOM = generateFishingRod("Spawner Room Fishing Rod", 5);
    public static ItemStack IGLOO_ROD = generateFishingRod("Igloo Fishing Rod", 9);
    public static ItemStack FORTRESS_ROD = generateFishingRod("Nether Fortress Fishing Rod", 6);
    public static ItemStack BASTION_ROD = generateFishingRod("Bastion Fishing Rod", 7);
    public static ItemStack STRONGHOLD_ROD = generateFishingRod("Stronghold Fishing Rod", 8);

    public static void initRecipes(){
        makeFishingRodSurround("village_rod", new RecipeChoice.MaterialChoice(Tag.LOGS), VILLAGE_ROD);
        makeFishingRodSurround("shipwreck_rod", new RecipeChoice.MaterialChoice(Tag.ITEMS_BOATS), SHIPWRECK_ROD);
        makeFishingRodSurround("ruined_portal_rod", new RecipeChoice.MaterialChoice(Material.FLINT_AND_STEEL), RUINED_PORTAL_ROD);
        makeFishingRodSurround("desert_temple_rod", new RecipeChoice.MaterialChoice(Material.SANDSTONE), DESERT_TEMPLE_ROD);
        makeFishingRodSurround("spawner_rod", new RecipeChoice.MaterialChoice(Material.ROTTEN_FLESH), SPAWNER_ROOM);
        makeFishingRodSurround("igloo_rod", new RecipeChoice.MaterialChoice(Material.SNOW_BLOCK), IGLOO_ROD);
        makeFishingRodSurround("fortress_rod", new RecipeChoice.MaterialChoice(Material.NETHER_BRICKS), FORTRESS_ROD);
        makeFishingRodSurround("bastion_rod", new RecipeChoice.MaterialChoice(Material.BLACKSTONE), BASTION_ROD);
        makeFishingRodSurround("stronghold_rod", new RecipeChoice.MaterialChoice(Material.ENDER_EYE), STRONGHOLD_ROD);
    }

    private static void makeFishingRodSurround(String key, RecipeChoice surroundMaterial, ItemStack result){
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(FishingStructures.plugin, key), result)
                .shape("III","IFI","III")
                .setIngredient('I', surroundMaterial)
                .setIngredient('F', Material.FISHING_ROD);
        Bukkit.addRecipe(recipe);
    }


    private static ItemStack generateFishingRod(String name, int modelData){
        ItemStack itemStack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE+name);
        meta.setCustomModelData(modelData);
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
