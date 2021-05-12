package net.problemzone.troubles.modules.game.tester;

import net.problemzone.troubles.Main;
import net.problemzone.troubles.modules.game.Role;
import net.problemzone.troubles.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TesterManager {

    private static final int TESTING_TIME = 5;
    private static final double FAILURE_CHANCE = 0.1;

    private Tester tester;
    private TesterState testerState;

    public void testPlayer(Player player, Role role) {

        closeTester();
        Bukkit.broadcastMessage(String.format(Language.TESTER.getFormattedText(), player.getDisplayName()));
        player.teleport(tester.getMiddle());

        //TODO: Do other stuff bling bling möp möp

        new BukkitRunnable() {
            @Override
            public void run() {
                revealTest(role);
            }
        }.runTaskLater(Main.getJavaPlugin(), TESTING_TIME * 20L);
    }

    private void openTester() {
        testerState = TesterState.IDLE;
        tester.getRow().forEach(block -> block.setType(Material.AIR));
    }

    private void closeTester() {
        testerState = TesterState.TESTING;
        tester.getRow().forEach(block -> block.setType(Material.WHITE_STAINED_GLASS_PANE));
    }

    private void revealTest(Role role) {

        if(Math.random() < FAILURE_CHANCE || role != Role.TRAITOR){
            tester.getLights().forEach(block -> block.setType(Material.GREEN_STAINED_GLASS));
        } else {
            tester.getLights().forEach(block -> block.setType(Material.RED_STAINED_GLASS));
        }

        openTester();
    }


    //Tester initialization
    public void defineTester(Block button, BlockFace blockFace) {

        Vector orthogonal = blockFace.getDirection().getCrossProduct(new Vector(0, 1, 0));

        Block middle = button.getRelative(blockFace).getRelative(BlockFace.DOWN);

        List<Block> lights = new ArrayList<>();
        lights.add(button.getRelative(blockFace, 3).getRelative(BlockFace.UP).getRelative(orthogonal.getBlockX() * 2, 0, orthogonal.getBlockZ() * 2));
        lights.add(button.getRelative(blockFace, 3).getRelative(BlockFace.UP).getRelative(orthogonal.getBlockX() * -2, 0, orthogonal.getBlockZ() * -2));

        List<Block> row = new ArrayList<>();
        row.add(middle.getRelative(blockFace, 2));
        row.add(middle.getRelative(blockFace, 2).getRelative(orthogonal.getBlockX(), 0, orthogonal.getBlockZ()));
        row.add(middle.getRelative(blockFace, 2).getRelative(orthogonal.getBlockX() * -1, 0, orthogonal.getBlockZ() * -1));

        tester = new Tester(button, middle.getLocation(), lights, row);
    }


    //Public Getter
    public boolean isTester(Block button) {
        return hasTester() && tester.getButton() == button;
    }

    public boolean hasTester() {
        return tester != null;
    }

    public TesterState getTesterState() {
        return testerState;
    }


    //Tester Class
    static class Tester {
        Block button;
        Location middle;
        List<Block> lights;
        List<Block> row;

        public Tester(Block button, Location middle, List<Block> lights, List<Block> row) {
            this.button = button;
            this.middle = middle;
            this.lights = lights;
            this.row = row;
        }

        public List<Block> getLights() {
            return lights;
        }

        public List<Block> getRow() {
            return row;
        }

        public Block getButton() {
            return button;
        }

        public Location getMiddle() {
            return middle;
        }
    }

}
