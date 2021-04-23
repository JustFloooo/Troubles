package net.problemzone.troubles.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ScoreboardHandler {

    private final Map<Player, Integer> playerKills = new HashMap<>();
    private int playerCount;

    public void setScoreboard(Player player, String role) {

        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = board.registerNewObjective("Infos", "dummy", ChatColor.RED + "TROUBLES");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore("").setScore(9);

        Score roleName = obj.getScore(ChatColor.WHITE + "Rolle:");
        roleName.setScore(8);
        Team roleCounter = board.registerNewTeam("roleCounter");
        roleCounter.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        roleCounter.setPrefix(role);
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(7);

        obj.getScore(" ").setScore(6);

        Score onlineName = obj.getScore(ChatColor.WHITE + "Überlebende:");
        onlineName.setScore(5);
        Team onlineCounter = board.registerNewTeam("playerCounter");
        onlineCounter.addEntry(ChatColor.BLUE + "" + ChatColor.WHITE);
        onlineCounter.setPrefix(ChatColor.RED + "" + playerCount);
        obj.getScore(ChatColor.BLUE + "" + ChatColor.WHITE).setScore(4);

        obj.getScore("  ").setScore(3);

        Score kills = obj.getScore(ChatColor.WHITE + "Kills:");
        kills.setScore(2);
        Team killCounter = board.registerNewTeam("killCounter");
        killCounter.addEntry(ChatColor.GREEN + "" + ChatColor.WHITE);
        killCounter.setPrefix(ChatColor.RED + "" + playerKills.get(player));
        obj.getScore(ChatColor.GREEN + "" + ChatColor.WHITE).setScore(1);

        player.setScoreboard(board);
    }

    private void updatePlayer() {
        playerCount--;
        for(Player player : Bukkit.getOnlinePlayers()){
            Scoreboard board = player.getScoreboard();
            Objects.requireNonNull(board.getTeam("playerCounter")).setPrefix(ChatColor.RED + "" + playerCount);
        }
    }


    public void increaseKillCounter(Player player) {
        playerKills.put(player, playerKills.get(player) + 1);
        Scoreboard board = player.getScoreboard();
        Objects.requireNonNull(board.getTeam("killCounter")).setPrefix(ChatColor.RED + "" + playerKills.get(player));
        updatePlayer();
    }

    public void init() {
        playerCount = Bukkit.getOnlinePlayers().size();
        for(Player player : Bukkit.getOnlinePlayers()){
            playerKills.put(player, 0);
        }
    }

}