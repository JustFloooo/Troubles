package net.problemzone.troubles.modules;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.problemzone.troubles.modules.game.Role;
import net.problemzone.troubles.modules.game.items.ItemManager;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.modules.game.spectator.SpectatorManager;
import net.problemzone.troubles.util.Language;
import net.problemzone.troubles.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerManager {

    private static final int IDENTIFIER_SLOT = 8;

    private final ScoreboardManager scoreboardManager;
    private final SpectatorManager spectatorManager;
    private final ItemManager itemManager;

    public PlayerManager(ScoreboardManager scoreboardManager, SpectatorManager spectatorManager, ItemManager itemManager) {
        this.scoreboardManager = scoreboardManager;
        this.spectatorManager = spectatorManager;
        this.itemManager = itemManager;
    }

    public void initiatePlayer(Player player) {
        //Cleanup
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue());
        spectatorManager.removePlayerFromSpectator(player);
        player.setGameMode(GameMode.SURVIVAL);

        //Preparation
        player.teleport(Objects.requireNonNull(Bukkit.getWorld("Skeld")).getSpawnLocation()); //TODO: Choose World
        player.getInventory().setItem(IDENTIFIER_SLOT, itemManager.getIdentifier());

    }

    public void wrapUpGame(Player player) {
        player.getInventory().clear();
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue());
        scoreboardManager.removeScoreboard(player);
        spectatorManager.removePlayerFromSpectator(player);
        player.teleport(Objects.requireNonNull(Bukkit.getWorld("Lobby")).getSpawnLocation());
    }

    public void announceWin(Player player, Role role) {
        player.sendTitle(String.format(Language.ROLE_WIN.getText(), role.getRoleName().getText()), "", 10, 60, 10);
        if (role == Role.TRAITOR) Sounds.GAME_WIN_TRAITOR.playSoundForPlayer(player);
        else Sounds.GAME_WIN_INNO.playSoundForPlayer(player);
    }

    public void sendPlayerToHub(Player player) {
        IPlayerManager playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
        ICloudPlayer cloudPlayer = playerManager.getOnlinePlayer(player.getUniqueId());
        if(cloudPlayer == null) return;
        playerManager.getPlayerExecutor(cloudPlayer).connect("Lobby-1"); //TODO: Fallback??
    }

}
