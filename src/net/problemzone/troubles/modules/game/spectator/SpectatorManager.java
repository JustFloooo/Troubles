package net.problemzone.troubles.modules.game.spectator;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpectatorManager {

    private final List<Player> spectators = new ArrayList<>();

    public void setPlayerAsSpectator(Player player){

        spectators.add(player);

        player.getInventory().clear();
        player.teleport(player.getWorld().getSpawnLocation());
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue());

        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");

        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void removePlayerFromSpectator(Player player){
        spectators.remove(player);
        player.setPlayerListName(player.getName());
        player.setInvisible(false);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    public boolean isSpectator(Player player){
        return spectators.contains(player);
    }

}
