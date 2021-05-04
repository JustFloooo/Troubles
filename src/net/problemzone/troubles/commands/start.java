package net.problemzone.troubles.commands;

import net.problemzone.troubles.modules.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;

public class start implements CommandExecutor {

    private final static int START_COMMAND_TIME = 10;

    private final GameManager gameManager;

    public start(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {

        if (args.length == 0) {
            gameManager.initiateGame(START_COMMAND_TIME);
            return true;
        }

        if (args.length != 1) return false;
        if (!NumberUtils.isParsable(args[0])) return false;

        int time = Integer.parseInt(args[0]);
        gameManager.initiateGame(Math.max(time, 5));
        return true;
    }
}
