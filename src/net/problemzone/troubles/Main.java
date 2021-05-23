package net.problemzone.troubles;

import net.problemzone.troubles.commands.cancel;
import net.problemzone.troubles.commands.scorpse;
import net.problemzone.troubles.commands.start;
import net.problemzone.troubles.modules.PlayerManager;
import net.problemzone.troubles.modules.WorldProtectionListener;
import net.problemzone.troubles.modules.game.GameListener;
import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.game.corpses.CorpseListener;
import net.problemzone.troubles.modules.game.corpses.CorpseManager;
import net.problemzone.troubles.modules.game.items.ItemListener;
import net.problemzone.troubles.modules.game.items.ItemManager;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardListener;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.modules.game.spectator.SpectatorListener;
import net.problemzone.troubles.modules.game.spectator.SpectatorManager;
import net.problemzone.troubles.modules.game.tester.TesterListener;
import net.problemzone.troubles.modules.game.tester.TesterManager;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private static JavaPlugin javaPlugin;

    private final ScoreboardManager scoreboardManager = new ScoreboardManager();
    private final CorpseManager corpseManager = new CorpseManager(scoreboardManager);
    private final ItemManager itemManager = new ItemManager();
    private final SpectatorManager spectatorManager = new SpectatorManager(scoreboardManager);
    private final PlayerManager playerManager = new PlayerManager(scoreboardManager, spectatorManager);
    private final TesterManager testerManager = new TesterManager();

    private final GameManager gameManager = new GameManager(scoreboardManager, playerManager);

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    @Override
    public void onEnable() {
        getLogger().info("Loading Troubles Plugin.");
        initiatePlugin();

        getLogger().info("Load Troubles Worlds.");
        loadWorlds();

        getLogger().info("Loading Troubles Commands.");
        registerCommands();

        getLogger().info("Loading Troubles Listeners.");
        registerListeners();

        getLogger().info("Troubles primed and ready.");
    }

    private void initiatePlugin(){
        javaPlugin = this;
    }

    private void loadWorlds() {
        getServer().createWorld(new WorldCreator("Skeld"));
    }

    private void registerCommands() {
        //Commands
        Objects.requireNonNull(getCommand("start")).setExecutor(new start(gameManager));
        Objects.requireNonNull(getCommand("cancel")).setExecutor(new cancel(gameManager));
        Objects.requireNonNull(getCommand("scorpse")).setExecutor(new scorpse(corpseManager));
    }

    private void registerListeners() {
        //Event Listeners
        getServer().getPluginManager().registerEvents(new ScoreboardListener(scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new ItemListener(itemManager, spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, scoreboardManager, gameManager, corpseManager), this);
        getServer().getPluginManager().registerEvents(new TesterListener(testerManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(gameManager, spectatorManager), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new CorpseListener(corpseManager), this);
    }

}
