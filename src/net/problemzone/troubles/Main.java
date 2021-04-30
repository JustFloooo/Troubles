package net.problemzone.troubles;

import com.comphenix.protocol.ProtocolLibrary;
import net.problemzone.troubles.commands.start;
import net.problemzone.troubles.modules.game.GameListener;
import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.items.EntityEquipmentListener;
import net.problemzone.troubles.modules.items.ItemListener;
import net.problemzone.troubles.modules.items.ItemManager;
import net.problemzone.troubles.modules.player.PlayerManager;
import net.problemzone.troubles.modules.scoreboard.ScoreboardManager;
import net.problemzone.troubles.modules.scoreboard.ScoreboardListener;
import net.problemzone.troubles.modules.spectator.SpectatorListener;
import net.problemzone.troubles.modules.spectator.SpectatorManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private static JavaPlugin javaPlugin;

    private final ScoreboardManager scoreboardManager = new ScoreboardManager();
    private final ItemManager itemManager = new ItemManager();
    private final SpectatorManager spectatorManager = new SpectatorManager();
    private final PlayerManager playerManager = new PlayerManager(scoreboardManager);
    private final GameManager gameManager = new GameManager(playerManager);

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

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

    private void registerCommands() {
        Objects.requireNonNull(getCommand("start")).setExecutor(new start(gameManager));
    }

    private void registerListeners() {
        //Event Listeners
        getServer().getPluginManager().registerEvents(new ScoreboardListener(scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new ItemListener(itemManager, spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(gameManager), this);

        //Package Listeners
        ProtocolLibrary.getProtocolManager().addPacketListener(new EntityEquipmentListener(gameManager));
    }

}
