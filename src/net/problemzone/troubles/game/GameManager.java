package net.problemzone.troubles.game;

import net.problemzone.troubles.Main;
import net.problemzone.troubles.scoreboard.ScoreboardHandler;
import net.problemzone.troubles.util.Countdown;
import net.problemzone.troubles.util.language.Language;
import net.problemzone.troubles.util.language.LanguageKeyword;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private final int COUNTDOWN_SECONDS = 30;

    private final ScoreboardHandler scoreboardHandler;

    private final List<Player> traitors = new ArrayList<>();
    private final List<Player> detectives = new ArrayList<>();
    private GameState gameState = GameState.STARTING;

    public GameManager(ScoreboardHandler scoreboardHandler) {
        this.scoreboardHandler = scoreboardHandler;
    }

    public void initiateGame() {
        initiateGame(COUNTDOWN_SECONDS);
    }

    public void initiateGame(int seconds) {

        //Initialize Countdown
        Countdown.initiateCountdown(seconds * 20);

        gameState = GameState.WARM_UP;

        //Start after Countdown
        new BukkitRunnable() {
            @Override
            public void run() {
                start();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

    }

    private void start() {

        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (allPlayers.size() > 0) {

            //Initialize Scoreboard
            scoreboardHandler.init();

            //Determine roles
            int traitor_count = (allPlayers.size() + 1) / 3;
            int detective_count = allPlayers.size() / 4 + 1;
            assignPlayersToRole(allPlayers, traitor_count, traitors);
            assignPlayersToRole(allPlayers, detective_count, detectives);

            //Tell players the role
            for(Player player : Bukkit.getOnlinePlayers()){
                LanguageKeyword role = LanguageKeyword.INNOCENT_ROLE;
                LanguageKeyword description = LanguageKeyword.INNOCENT_DESCRIPTION;
                if(traitors.contains(player)){
                    role = LanguageKeyword.TRAITOR_ROLE;
                    description = LanguageKeyword.TRAITOR_DESCIRPTION;
                }
                if(detectives.contains(player)){
                    role = LanguageKeyword.DETECTIVE_ROLE;
                    description = LanguageKeyword.DETECTIVE_DESCRIPTION;
                }
                player.sendTitle(Language.getUnformattedStringFromKeyword(role), Language.getUnformattedStringFromKeyword(description), 10, 60, 10);
                player.sendMessage(String.format(Language.getStringFromKeyword(LanguageKeyword.ROLE_ASSIGNED), Language.getUnformattedStringFromKeyword(role)) + " " + ChatColor.GRAY + Language.getUnformattedStringFromKeyword(description));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);

                scoreboardHandler.setScoreboard(player, Language.getUnformattedStringFromKeyword(role));
            }
            gameState = GameState.RUNNING;
        } else {
            gameState = GameState.FINISHED;
        }
    }

    private void assignPlayersToRole(List<Player> listFrom, int count, List<Player> listTo) {
        while (count > 0) {
            Player player = listFrom.stream().skip((int) (listFrom.size() * Math.random())).findFirst().orElse(null);
            if (player != null) {
                listTo.add(player);
                listFrom.remove(player);
            }
            count--;
        }
    }

    public GameState getGameState() {
        return gameState;
    }
}
