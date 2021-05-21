package net.problemzone.troubles.modules.game.tester;

import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Objects;

public class TesterListener implements Listener {

    private final TesterManager testerManager;
    private final GameManager gameManager;

    public TesterListener(TesterManager testerManager, GameManager gameManager) {
        this.testerManager = testerManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onTesterButtonPress(PlayerInteractEvent e) {

        if (gameManager.getGameState() != GameState.RUNNING) return;
        if (testerManager.getTesterState() != TesterState.IDLE) return;
        if (gameManager.getPlayerRole(e.getPlayer()) == null) return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (Objects.requireNonNull(e.getClickedBlock()).getType() != Material.POLISHED_BLACKSTONE_BUTTON) return;

        if (!testerManager.hasTester()) {
            BlockFace direction = Arrays.stream(BlockFace.values()).filter(blockFace -> e.getClickedBlock().getRelative(blockFace).getType() == Material.CHISELED_QUARTZ_BLOCK).findFirst().orElse(BlockFace.UP);
            if (e.getClickedBlock().getRelative(direction, 2).getType() != Material.STRUCTURE_BLOCK) return;
            testerManager.defineTester(e.getClickedBlock(), direction.getOppositeFace());
        }

        if (!testerManager.isTester(e.getClickedBlock())) return;

        testerManager.testPlayer(e.getPlayer(), gameManager.getPlayerRole(e.getPlayer()));
    }

}
