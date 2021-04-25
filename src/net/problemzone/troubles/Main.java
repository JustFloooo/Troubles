package net.problemzone.troubles;

import net.problemzone.troubles.commands.start;
import net.problemzone.troubles.game.GameListener;
import net.problemzone.troubles.game.GameManager;
import net.problemzone.troubles.items.ItemListener;
import net.problemzone.troubles.items.ItemManager;
import net.problemzone.troubles.scoreboard.ScoreboardHandler;
import net.problemzone.troubles.scoreboard.ScoreboardListener;
import net.problemzone.troubles.spectator.SpectatorListener;
import net.problemzone.troubles.spectator.SpectatorManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private static JavaPlugin javaPlugin;

    private final ScoreboardHandler scoreboardHandler = new ScoreboardHandler();
    private final GameManager gameManager = new GameManager(scoreboardHandler);
    private final ItemManager itemManager = new ItemManager();
    private final SpectatorManager spectatorManager = new SpectatorManager();

    @Override
    public void onEnable() {
        getLogger().info("Loading Troubles Plugin.");
        javaPlugin = this;

        //getLogger().info("Reading Troubles Configuration.");
        //loadConfiguration();

        getLogger().info("Loading Troubles Commands.");
        registerCommands();

        getLogger().info("Loading Troubles Listeners.");
        registerListeners();

        getLogger().info("Troubles primed and ready.");
    }

    private void registerCommands(){
        Objects.requireNonNull(getCommand("start")).setExecutor(new start(gameManager));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ScoreboardListener(scoreboardHandler), this);
        getServer().getPluginManager().registerEvents(new ItemListener(itemManager, spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(gameManager), this);
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

}
