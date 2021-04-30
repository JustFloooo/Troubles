package net.problemzone.troubles.modules.game;

import com.comphenix.protocol.events.PacketContainer;
import net.problemzone.troubles.Main;
import net.problemzone.troubles.modules.player.PlayerManager;
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

    private final PlayerManager playerManager;

    private final Map<Player, Role> playerRoleMap = new HashMap<>();
    private List<Player> possiblePlayers;
    private GameState gameState = GameState.STARTING;

    public GameManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
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

    //Assigns Roles and starts the Game
    private void startGame() {

        if (possiblePlayers.size() < MIN_PLAYERS) {
            Bukkit.broadcastMessage(Language.NOT_ENOUGH_PLAYERS.getFormattedText());
            gameState = GameState.FINISHED;
            //TODO: Cancel Game
            return;
        }

        //Distribute Game Roles
        Collections.shuffle(possiblePlayers);

        int traitor_count = (possiblePlayers.size() + 1) / 3; //5->2, 8->3, 11->4
        List<Player> traitors = possiblePlayers.subList(0, traitor_count);
        traitors.forEach(player -> playerRoleMap.put(player, Role.TRAITOR));

        int detective_count = possiblePlayers.size() / 6 + 1; //6->2 12->3
        List<Player> detectives = possiblePlayers.subList(traitor_count, traitor_count + detective_count);
        detectives.forEach(player -> playerRoleMap.put(player, Role.DETECTIVE));

        possiblePlayers.subList(traitor_count + detective_count, possiblePlayers.size()).forEach(player -> playerRoleMap.put(player, Role.INNOCENT));

        PacketContainer traitorNames = playerManager.createPlayerNameColorPacket(traitors, ChatColor.RED, "traitors");
        PacketContainer detectiveNames = playerManager.createPlayerNameColorPacket(detectives, ChatColor.BLUE, "detectives");

        playerRoleMap.keySet().forEach(player -> {
            playerManager.tellRoleToPlayer(player, playerRoleMap.get(player));
            playerManager.sendPacket(player, detectiveNames);
            if(playerRoleMap.get(player) == Role.TRAITOR) playerManager.sendPacket(player, traitorNames);
        });

        gameState = GameState.RUNNING;
    }


    //Winning Related Methods
    public void removePlayer(Player player) {
        possiblePlayers.remove(player);
        playerRoleMap.remove(player);
        checkForWin();
    }

    private void checkForWin() {
        if (!playerRoleMap.containsValue(Role.TRAITOR)) {
            announceWin(Role.INNOCENT);
            return;
        }
        if (!playerRoleMap.containsValue(Role.INNOCENT) && !playerRoleMap.containsValue(Role.DETECTIVE)) {
            announceWin(Role.TRAITOR);
        }
    }

    private void announceWin(Role role) {
        gameState = GameState.FINISHED;

        //TODO: Teleport Players to Lobby

        Bukkit.broadcastMessage(String.format(Language.ROLE_WIN.getFormattedText(), role.getRoleName().getText()));
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(String.format(Language.ROLE_WIN.getText(), role.getRoleName().getText()), "", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);
        });
    }


    //Public getter methods
    public GameState getGameState() {
        return gameState;
    }

    public Role getPlayerRoleByPlayer(Player player){
        return playerRoleMap.get(player);
    }

    public Role getPlayerRoleByEntityID(int entityID){
        return playerRoleMap.keySet().stream().filter(player -> player.getEntityId() == entityID).findFirst().map(playerRoleMap::get).orElse(null);
    }
}
