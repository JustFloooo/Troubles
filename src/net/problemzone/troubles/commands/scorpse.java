package net.problemzone.troubles.commands;

import net.problemzone.troubles.modules.game.corpses.CorpseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class scorpse implements CommandExecutor {

    private final CorpseManager corpseManager;

    public scorpse(CorpseManager corpseManager) {
        this.corpseManager = corpseManager;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        CorpseManager.CorpseData data;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED
                    + "Only players can run this command. Sorry about that.");
            return true;
        }
        if (args.length != 0) return false;

        Player p = (Player) sender;
        corpseManager.spawnCorpse(p, p.getLocation());
        p.sendMessage(ChatColor.GREEN + "Corpse of yourself spawned!");
        return true;
    }

}
