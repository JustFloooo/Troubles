package net.problemzone.troubles;

import net.problemzone.troubles.commands.cancel;
import net.problemzone.troubles.commands.start;
import net.problemzone.troubles.modules.game.GameListener;
import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.game.items.ItemListener;
import net.problemzone.troubles.modules.game.items.ItemManager;
import net.problemzone.troubles.modules.PlayerManager;
import net.problemzone.troubles.modules.WorldProtectionListener;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardListener;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.modules.game.spectator.SpectatorListener;
import net.problemzone.troubles.modules.game.spectator.SpectatorManager;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private static JavaPlugin javaPlugin;

    private final ScoreboardManager scoreboardManager = new ScoreboardManager();
    private final ItemManager itemManager = new ItemManager();
    private final SpectatorManager spectatorManager = new SpectatorManager();
    private final PlayerManager playerManager = new PlayerManager(scoreboardManager, spectatorManager);

    private final GameManager gameManager = new GameManager(scoreboardManager, playerManager);

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    @Override
    public void onEnable() {
        getLogger().info("Loading Troubles Plugin.");
        javaPlugin = this;

        getLogger().info("Load Troubles Worlds.");
        loadWorlds();

        getLogger().info("Loading Troubles Commands.");
        registerCommands();

        getLogger().info("Loading Troubles Listeners.");
        registerListeners();

        getLogger().info("Troubles primed and ready.");
    }

    private void loadWorlds() {
        getServer().createWorld(new WorldCreator("Skeld"));
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("start")).setExecutor(new start(gameManager));
        Objects.requireNonNull(getCommand("cancel")).setExecutor(new cancel(gameManager));
    }

    private void registerListeners() {
        //Event Listeners
        getServer().getPluginManager().registerEvents(new ScoreboardListener(scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new ItemListener(itemManager, spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(), this);
    }

}
