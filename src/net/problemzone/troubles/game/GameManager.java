package net.problemzone.troubles.game;

import net.problemzone.troubles.Main;
import net.problemzone.troubles.scoreboard.ScoreboardHandler;
import net.problemzone.troubles.util.Countdown;
import net.problemzone.troubles.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final int WARM_UP_TIME = 30;
    private final int MIN_PLAYERS = 4;

    private final ScoreboardHandler scoreboardHandler;

    private final Map<Player, Role> playerRoleMap = new HashMap<>();
    private List<Player> possiblePlayers;
    private GameState gameState = GameState.STARTING;

    public GameManager(ScoreboardHandler scoreboardHandler) {
        this.scoreboardHandler = scoreboardHandler;
    }

    public void initiateGame() {
        initiateGame(WARM_UP_TIME);
    }

    //Starts Lobby Countdown
    public void initiateGame(int seconds) {

        //Initialize Countdown
        Countdown.initiateCountdown(seconds * 20, Language.TITLE_START);

        //Set GameState to WarmUp
        new BukkitRunnable() {
            @Override
            public void run() {
                startWarmUp();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

    }

    //Starts Warm Up Phase
    private void startWarmUp() {

        if (Bukkit.getOnlinePlayers().size() < MIN_PLAYERS) {
            Bukkit.broadcastMessage(Language.NOT_ENOUGH_PLAYERS.getFormattedText());
            gameState = GameState.STARTING;
            return;
        }

        possiblePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        //TODO: Teleport Players to Arena

        gameState = GameState.WARM_UP;

        //Initialize Countdown
        Countdown.initiateCountdown(WARM_UP_TIME * 20, Language.TITLE_START);

        //Set GameState to Countdown
        new BukkitRunnable() {
            @Override
            public void run() {
                startGame();
            }
        }.runTaskLater(Main.getJavaPlugin(), WARM_UP_TIME * 20L);
    }

    private void startGame() {

        if (possiblePlayers.size() < MIN_PLAYERS) {
            Bukkit.broadcastMessage(Language.NOT_ENOUGH_PLAYERS.getFormattedText());
            gameState = GameState.FINISHED;
            //TODO: Cancel Game
            return;
        }

        //Distribute Game Roles
        determinePlayerRoles();
        playerRoleMap.keySet().forEach(this::initializePlayer);

        gameState = GameState.RUNNING;
    }

    private void determinePlayerRoles() {

        int traitor_count = (possiblePlayers.size() + 1) / 3; //5->2, 8->3, 11->4
        int detective_count = possiblePlayers.size() / 6 + 1; //6->2 12->3
        Collections.shuffle(possiblePlayers);
        possiblePlayers.subList(0, traitor_count).forEach(player -> playerRoleMap.put(player, Role.TRAITOR));
        possiblePlayers.subList(traitor_count, traitor_count + detective_count).forEach(player -> playerRoleMap.put(player, Role.DETECTIVE));
        possiblePlayers.subList(traitor_count + detective_count, possiblePlayers.size()).forEach(player -> playerRoleMap.put(player, Role.INNOCENT));
    }

    private void initializePlayer(Player player) {
        Role role = playerRoleMap.get(player);

        player.sendTitle(role.getRoleName().getText(), role.getRoleDescription().getText(), 10, 60, 10);
        player.sendMessage(String.format(Language.ROLE_ASSIGNED.getFormattedText(), role.getRoleName().getText()) + " " + ChatColor.GRAY + role.getRoleDescription().getText());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);

        scoreboardHandler.setScoreboard(player, role.getRoleName().getText());
    }

    private void checkForWin(){
        if(!playerRoleMap.containsValue(Role.TRAITOR)){
            announceWin(Role.INNOCENT);
            return;
        }
        if(!playerRoleMap.containsValue(Role.INNOCENT) && !playerRoleMap.containsValue(Role.DETECTIVE)){
            announceWin(Role.TRAITOR);
        }
    }

    private void announceWin(Role role){
        gameState = GameState.FINISHED;

        //TODO: Teleport Players to Lobby

        Bukkit.broadcastMessage(String.format(Language.ROLE_WIN.getFormattedText(), role.getRoleName().getText()));
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(String.format(Language.ROLE_WIN.getText(), role.getRoleName().getText()), "", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);
        });
    }

    public GameState getGameState() {
        return gameState;
    }

    public void removePlayer(Player player) {
        possiblePlayers.remove(player);
        playerRoleMap.remove(player);
        checkForWin();
    }
}
