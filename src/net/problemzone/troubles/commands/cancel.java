package net.problemzone.troubles.commands;

import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class cancel implements CommandExecutor {

    private final GameManager gameManager;

    public cancel(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {

        if (args.length != 0) return false;
        gameManager.cancelGameInitiation();
        Bukkit.broadcastMessage(Language.GAME_START_CANCEL.getFormattedText());
        return true;
    }

}
