package net.problemzone.troubles.spectator;

import net.problemzone.troubles.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorListener implements Listener {

    private final SpectatorManager spectatorManager;

    public SpectatorListener(SpectatorManager spectatorManager) {
        this.spectatorManager = spectatorManager;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        e.setRespawnLocation(e.getRespawnLocation().add(0,10,0));

        new BukkitRunnable() {
            @Override
            public void run() {
                spectatorManager.setPlayerAsSpectator(player);
            }
        }.runTaskLater(Main.getJavaPlugin(), 1);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof Player && spectatorManager.isSpectator((Player)e.getDamager())) e.setCancelled(true);

        if(e.getEntity() instanceof Player && spectatorManager.isSpectator((Player)e.getEntity())) e.setCancelled(true);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e){
        if(spectatorManager.isSpectator(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player && spectatorManager.isSpectator((Player) e.getEntity())) e.setCancelled(true);
    }


}
