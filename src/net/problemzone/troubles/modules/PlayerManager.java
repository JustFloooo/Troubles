package net.problemzone.troubles.modules;

import net.problemzone.troubles.modules.game.Role;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.modules.game.spectator.SpectatorManager;
import net.problemzone.troubles.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerManager {

    private final ScoreboardManager scoreboardManager;
    private final SpectatorManager spectatorManager;

    public PlayerManager(ScoreboardManager scoreboardManager, SpectatorManager spectatorManager) {
        this.scoreboardManager = scoreboardManager;
        this.spectatorManager = spectatorManager;
    }

    public void intiiateGame(Player player){
        //TODO: Choose World
        player.teleport(Objects.requireNonNull(Bukkit.getWorld("Skeld")).getSpawnLocation());
    }

    public void wrapUpGame(Player player){
        player.getInventory().clear();
        scoreboardManager.removeScoreboard(player);
        spectatorManager.removePlayerFromSpectator(player);
        player.teleport(Objects.requireNonNull(Bukkit.getWorld("Lobby")).getSpawnLocation());
    }

    public void announceWin(Player player, Role role){
        player.sendTitle(String.format(Language.ROLE_WIN.getText(), role.getRoleName().getText()), "", 10, 60, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, 1, role == Role.TRAITOR ? 0.8F : 1.3F);
    }

}
