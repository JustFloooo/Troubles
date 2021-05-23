package net.problemzone.troubles.modules;

import net.problemzone.troubles.modules.game.Role;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.modules.game.spectator.SpectatorManager;
import net.problemzone.troubles.util.Language;
import net.problemzone.troubles.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerManager {

    private final ScoreboardManager scoreboardManager;
    private final SpectatorManager spectatorManager;

    public PlayerManager(ScoreboardManager scoreboardManager, SpectatorManager spectatorManager) {
        this.scoreboardManager = scoreboardManager;
        this.spectatorManager = spectatorManager;
    }

    public void initiatePlayer(Player player){
        //TODO: Choose World
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue());
        player.teleport(Objects.requireNonNull(Bukkit.getWorld("Skeld")).getSpawnLocation());
    }

    public void wrapUpGame(Player player){
        player.getInventory().clear();
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue());
        scoreboardManager.removeScoreboard(player);
        spectatorManager.removePlayerFromSpectator(player);
        player.teleport(Objects.requireNonNull(Bukkit.getWorld("Lobby")).getSpawnLocation());
    }

    public void announceWin(Player player, Role role){
        player.sendTitle(String.format(Language.ROLE_WIN.getText(), role.getRoleName().getText()), "", 10, 60, 10);
        if (role == Role.TRAITOR) Sounds.GAME_WIN_TRAITOR.playSoundForPlayer(player);
        else Sounds.GAME_WIN_INNO.playSoundForPlayer(player);
    }

    public void sendPlayerToHub(Player player){
        player.performCommand("/hub");
    }

}
