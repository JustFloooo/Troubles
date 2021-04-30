package net.problemzone.troubles.modules.items;

import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.game.GameState;
import net.problemzone.troubles.modules.spectator.SpectatorManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class ItemListener implements Listener {

    private final ItemManager itemManager;
    private final SpectatorManager spectatorManager;
    private final GameManager gameManager;

    public ItemListener(ItemManager itemManager, SpectatorManager spectatorManager, GameManager gameManager) {
        this.itemManager = itemManager;
        this.spectatorManager = spectatorManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent e) {
        if (!spectatorManager.isSpectator(e.getPlayer())) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                //Chest Items
                if (Objects.requireNonNull(e.getClickedBlock()).getType() == Material.CHEST) {
                    if (gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) {
                        if (itemManager.giveChestItemToPlayer(e.getPlayer())) e.getClickedBlock().setType(Material.AIR);
                        e.setCancelled(true);
                    }
                }

                //Enderchest Item
                if (Objects.requireNonNull(e.getClickedBlock()).getType() == Material.ENDER_CHEST) {
                    if (gameManager.getGameState() == GameState.RUNNING) {
                        if (itemManager.giveEnderItemToPlayer(e.getPlayer())) e.getClickedBlock().setType(Material.AIR);
                        e.setCancelled(true);
                    }

                }
            }
        }
    }
}
