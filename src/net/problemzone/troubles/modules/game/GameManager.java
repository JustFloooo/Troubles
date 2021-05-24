package net.problemzone.troubles.modules.game;

import com.comphenix.protocol.events.PacketContainer;
import net.problemzone.troubles.Main;
import net.problemzone.troubles.modules.PlayerManager;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.util.Countdown;
import net.problemzone.troubles.util.Language;
import net.problemzone.troubles.util.NMSPackets;
import net.problemzone.troubles.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private static final int STARTING_LOBBY_TIME = 60;
    private static final int WARM_UP_TIME = 30;
    private static final int FINAL_LOBBY_TIME = 20;
    private static final int MIN_PLAYERS = 2;

    private final ScoreboardManager scoreboardManager;
    private final PlayerManager playerManager;

    private final Map<Player, Role> playerRoleMap = new HashMap<>();
    private List<Player> possiblePlayers;
    private BukkitTask currentScheduledTask;

    private GameState gameState = GameState.WAITING;

    public GameManager(ScoreboardManager scoreboardManager, PlayerManager playerManager) {
        this.scoreboardManager = scoreboardManager;
        this.playerManager = playerManager;
    }

    public void initiateGame() {
        initiateGame(STARTING_LOBBY_TIME);
    }

    //Starts Lobby Countdown
    public void initiateGame(int seconds) {

        if (gameState != GameState.WAITING && gameState != GameState.STARTING) return;

        gameState = GameState.STARTING;

        //Initialize Countdown
        Countdown.createXpBarCountdown(seconds);
        Countdown.createLevelCountdown(seconds, Language.GAME_START_TITLE);
        Countdown.createChatCountdown(seconds, Language.GAME_START);

        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();

        currentScheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                startWarmUp();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

    }

    public void cancelGameInitiation() {
        Countdown.cancelXpBarCountdown();
        Countdown.cancelLevelCountdown();
        Countdown.cancelChatCountdown();
        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();
        gameState = GameState.WAITING;
    }

    //Starts Warm Up Phase
    private void startWarmUp() {
        if (Bukkit.getOnlinePlayers().size() < MIN_PLAYERS) {
            Bukkit.broadcastMessage(Language.NOT_ENOUGH_PLAYERS.getFormattedText());
            gameState = GameState.WAITING;
            return;
        }

        //Disable Lobby Plugin
        if(Bukkit.getPluginManager().getPlugin("Lobbibi") != null) Bukkit.getPluginManager().disablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Lobbibi")));

        possiblePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        //Teleport to Map
        possiblePlayers.forEach(playerManager::initiatePlayer);

        gameState = GameState.WARM_UP;

        //Initialize Countdown
        Countdown.createChatCountdown(WARM_UP_TIME, Language.WARM_UP);

        //Set GameState to Countdown
        new BukkitRunnable() {
            @Override
            public void run() {
                startGame();
            }
        }.runTaskLater(Main.getJavaPlugin(), WARM_UP_TIME * 20L);
    }

    //Assigns Roles and starts the Game
    private void startGame() {

        if (possiblePlayers.size() < MIN_PLAYERS) {
            gameState = GameState.FINISHED;

            Bukkit.broadcastMessage(Language.NOT_ENOUGH_PLAYERS.getFormattedText());
            possiblePlayers.forEach(playerManager::wrapUpGame);
            return;
        }

        //Distribute Game Roles
        Collections.shuffle(possiblePlayers);

        int traitor_count = (possiblePlayers.size() + 1) / 3; //5->2, 8->3, 11->4
        List<Player> traitors = possiblePlayers.subList(0, traitor_count);
        traitors.forEach(player -> playerRoleMap.put(player, Role.TRAITOR));

        int detective_count = (possiblePlayers.size() / 7) + 1; //6->2 12->3
        List<Player> detectives = possiblePlayers.subList(traitor_count, traitor_count + detective_count);
        detectives.forEach(player -> playerRoleMap.put(player, Role.DETECTIVE));

        possiblePlayers.subList(traitor_count + detective_count, possiblePlayers.size()).forEach(player -> playerRoleMap.put(player, Role.INNOCENT));

        //Create Name and Armor Packages
        PacketContainer traitorNames = NMSPackets.createPlayerNameColorPacket(traitors, ChatColor.RED, "traitors");
        PacketContainer detectiveNames = NMSPackets.createPlayerNameColorPacket(detectives, ChatColor.BLUE, "detectives");
        //List<PacketContainer> traitorArmor = Packages.createFakeArmorPacket(traitors, Color.RED);
        //List<PacketContainer> detectiveArmor = Packages.createFakeArmorPacket(traitors, Color.BLUE);

        //Send Packages to players
        playerRoleMap.keySet().forEach(player -> {
            tellRoleToPlayer(player, playerRoleMap.get(player));
            NMSPackets.sendPacket(player, detectiveNames);
            //Packages.sendPacket(player, detectiveArmor.toArray(PacketContainer[]::new));
            if (playerRoleMap.get(player) == Role.TRAITOR) {
                NMSPackets.sendPacket(player, traitorNames);
                //Packages.sendPacket(player, traitorArmor.toArray(PacketContainer[]::new));
            }
        });

        gameState = GameState.RUNNING;
    }

    public void tellRoleToPlayer(Player player, Role role) {
        player.sendTitle(role.getRoleName().getText(), role.getRoleDescription().getText(), 10, 60, 10);
        player.sendMessage(String.format(Language.ROLE_ASSIGNED.getFormattedText(), role.getRoleName().getText()) + " " + ChatColor.GRAY + role.getRoleDescription().getText());
        Sounds.RECEIVE_ROLE.playSoundForPlayer(player);

        scoreboardManager.setGameScoreboard(player, role);
    }

    //Winning Related Methods
    public void removePlayer(Player player) {
        if(possiblePlayers != null) possiblePlayers.remove(player);
        playerRoleMap.remove(player);
        if (gameState == GameState.RUNNING) checkForWin();
    }

    private void checkForWin() {
        if (!playerRoleMap.containsValue(Role.TRAITOR)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    finishGame(Role.INNOCENT);
                }
            }.runTaskLater(Main.getJavaPlugin(), 3 * 20L);
            return;
        }
        if (!playerRoleMap.containsValue(Role.INNOCENT) && !playerRoleMap.containsValue(Role.DETECTIVE)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    finishGame(Role.TRAITOR);
                }
            }.runTaskLater(Main.getJavaPlugin(), 3 * 20L);
        }
    }

    private void finishGame(Role role) {
        gameState = GameState.FINISHED;

        //Teleport to Lobby
        Bukkit.getOnlinePlayers().forEach(playerManager::wrapUpGame);

        //Announce Winner
        Bukkit.broadcastMessage(String.format(Language.ROLE_WIN.getFormattedText(), role.getRoleName().getText()));
        Bukkit.getOnlinePlayers().forEach(player -> playerManager.announceWin(player, role));

        Countdown.createChatCountdown(FINAL_LOBBY_TIME, Language.SERVER_CLOSE);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(playerManager::sendPlayerToHub);
                Bukkit.getServer().shutdown();
            }
        }.runTaskLater(Main.getJavaPlugin(), FINAL_LOBBY_TIME * 20L + 1);

    }

    public Role getPlayerRole(Player player) {
        return playerRoleMap.get(player);
    }

    //Public getter methods
    public GameState getGameState() {
        return gameState;
    }
}
