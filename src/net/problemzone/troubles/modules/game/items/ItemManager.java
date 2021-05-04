package net.problemzone.troubles.modules.game.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemManager {

    private final Set<Set<ItemStack>> chestItemChoice = Set.of(
            Set.of(new ItemStack(Material.WOODEN_SWORD)),
            Set.of(new ItemStack(Material.STONE_SWORD)),
            Set.of(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16)),
            Set.of(new ItemStack(Material.WOODEN_AXE)),
            Set.of(new ItemStack(Material.STONE_AXE)),
            Set.of(new ItemStack(Material.SHIELD)),
            Set.of(new ItemStack(Material.CROSSBOW), new ItemStack(Material.ARROW, 10))
    );

    private final ItemStack enderItem = new ItemStack(Material.IRON_SWORD);

    public boolean giveChestItemToPlayer(Player player) {
        //Get all ItemSets not in Inventory
        Set<Set<ItemStack>> potentialItemChoice = chestItemChoice.stream().filter(potentialChoice -> potentialChoice.stream().noneMatch(potentialItem -> player.getInventory().contains(potentialItem.getType()))).collect(Collectors.toSet());

        if (potentialItemChoice.size() == 0) return false;

        //Add all Items for a Random Set to Inventory
        potentialItemChoice.stream().skip((int) (Math.random() * potentialItemChoice.size())).findFirst().orElse(Set.of(new ItemStack(Material.AIR))).forEach(player.getInventory()::addItem);

        return true;
    }

    public boolean giveEnderItemToPlayer(Player player) {
        if (player.getInventory().contains(enderItem)) return false;

        player.getInventory().addItem(enderItem);
        return true;
    }

}
