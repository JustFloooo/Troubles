package net.problemzone.troubles.modules.game.corpses;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;

public class CorpseListener implements Listener {

    private final CorpseManager corpseManager;

    public CorpseListener(CorpseManager corpseManager) {
        this.corpseManager = corpseManager;
    }

    @EventHandler
    public void showCorpseOnJoin(PlayerJoinEvent e) {
        corpseManager.getAllCorpses().forEach(corpseData -> corpseData.sendCorpseToPlayer(e.getPlayer()));
    }

    @EventHandler
    public void onNearBlockClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Location loc = e.getClickedBlock().getLocation();
        CorpseManager.CorpseData cd = getCorpseInRadius(loc, 2);

        if (cd == null) return;
        corpseManager.cowHit(e.getPlayer(), cd);
    }

    @Nullable
    public CorpseManager.CorpseData getCorpseInRadius(Location locClick, double radius) {
        for (CorpseManager.CorpseData cd : corpseManager.getAllCorpses()) {
            Location locCorpse = cd.getOrigLocation();
            if (locClick.distance(locCorpse) <= radius) {
                return cd;
            }
        }
        return null;
    }

}
