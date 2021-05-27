package net.problemzone.troubles.modules.game.items;

import net.problemzone.troubles.util.Sounds;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemManager {

    private static final Material IDENTIFIER_MATERIAL = Material.STICK;
    private static final String IDENTIFIER_NAME = ChatColor.BLUE + "Stab der Identifizierung";

    private static final Set<Set<ItemStack>> CHEST_ITEM_CHOICE = Set.of(
            Set.of(new ItemStack(Material.WOODEN_PICKAXE)), //2
            Set.of(new ItemStack(Material.GOLDEN_PICKAXE)),
            Set.of(new ItemStack(Material.WOODEN_SHOVEL)), //2.5
            Set.of(new ItemStack(Material.GOLDEN_SHOVEL)),
            Set.of(new ItemStack(Material.STONE_PICKAXE)), //3
            Set.of(new ItemStack(Material.STONE_SHOVEL)), //3.5
            Set.of(new ItemStack(Material.WOODEN_SWORD)), //4
            Set.of(new ItemStack(Material.IRON_PICKAXE)),
            Set.of(new ItemStack(Material.IRON_SHOVEL)), //4.5
            Set.of(new ItemStack(Material.STONE_SWORD)), //5
            Set.of(new ItemStack(Material.DIAMOND_PICKAXE)),
            Set.of(new ItemStack(Material.DIAMOND_SHOVEL)), //5.5
            Set.of(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16)),
            Set.of(new ItemStack(Material.CROSSBOW), new ItemStack(Material.ARROW, 10)),
            Set.of(new ItemStack(Material.FISHING_ROD))
    );

    private static final ItemStack ENDER_ITEM = new ItemStack(Material.IRON_SWORD); //6

    protected boolean giveChestItemToPlayer(Player player) {
        //Get all ItemSets not in Inventory
        Set<Set<ItemStack>> potentialItemChoice = CHEST_ITEM_CHOICE.stream().filter(potentialChoice -> potentialChoice.stream().noneMatch(potentialItem -> player.getInventory().contains(potentialItem.getType()))).collect(Collectors.toSet());

        if (potentialItemChoice.size() == 0) return false;

        //Add all Items for a Random Set to Inventory
        potentialItemChoice.stream().skip((int) (Math.random() * potentialItemChoice.size())).findFirst().orElse(Set.of(new ItemStack(Material.AIR))).forEach(player.getInventory()::addItem);
        Sounds.CHEST_ITEM.playSoundForPlayer(player);

        return true;
    }

    protected boolean giveEnderItemToPlayer(Player player) {
        if (player.getInventory().contains(ENDER_ITEM)) return false;

        player.getInventory().addItem(ENDER_ITEM);
        Sounds.ENDER_CHEST_ITEM.playSoundForPlayer(player);

        return true;
    }

    public ItemStack getIdentifier() {
        ItemStack identifier = new ItemStack(IDENTIFIER_MATERIAL);

        ItemMeta identifierMeta = identifier.getItemMeta();
        Objects.requireNonNull(identifierMeta).setDisplayName(IDENTIFIER_NAME);

        identifier.setItemMeta(identifierMeta);
        return identifier;
    }


}
