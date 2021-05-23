package net.problemzone.troubles.modules.game.spectator;

import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.game.GameState;
import net.problemzone.troubles.modules.game.corpses.CorpseManager;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.util.Language;
import net.problemzone.troubles.util.Sounds;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class SpectatorListener implements Listener {

    private final SpectatorManager spectatorManager;
    private final ScoreboardManager scoreboardManager;
    private final GameManager gameManager;
    private final CorpseManager corpseManager;

    public SpectatorListener(SpectatorManager spectatorManager, ScoreboardManager scoreboardManager, GameManager gameManager, CorpseManager corpseManager) {
        this.spectatorManager = spectatorManager;
        this.scoreboardManager = scoreboardManager;
        this.gameManager = gameManager;
        this.corpseManager = corpseManager;
    }

    @EventHandler
    public void onSpectatorJoin(PlayerJoinEvent e) {
        if (gameManager.getGameState() != GameState.WARM_UP && gameManager.getGameState() != GameState.RUNNING) return;

        spectatorManager.setPlayerAsSpectator(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeathDamage(EntityDamageEvent e) {
        if(e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if (!(player.getHealth() - e.getFinalDamage() <= 0)) return;

        e.setCancelled(true);

        Sounds.DEATH.playSoundForPlayer(player);
        CorpseManager.CorpseData corpse = corpseManager.spawnCorpse(player, player.getLocation());

        spectatorManager.setPlayerAsSpectator(player);
        gameManager.removePlayer(player);

        if(!(e instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

        if(event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Sounds.KILL.playSoundForPlayer(player);
            scoreboardManager.increaseKillCounter(damager);
            damager.sendMessage(String.format(damager.getInventory().getItemInMainHand().getType().isAir() ? Language.KILLER_FIST.getFormattedText() : Language.KILLER_SLAIN.getFormattedText(), player.getDisplayName()));
            player.sendMessage(String.format(Language.VICTIM_SLAIN.getFormattedText(), damager.getDisplayName()));
            corpse.setKillerName(damager.getDisplayName());
        }

        if (event.getDamager() instanceof Arrow){
            Arrow arrow = (Arrow) event.getDamager();
            if(!(arrow.getShooter() instanceof Player)) return;
            Player damager = (Player) arrow.getShooter();
            Sounds.KILL.playSoundForPlayer(player);
            scoreboardManager.increaseKillCounter(damager);
            damager.sendMessage(String.format(Language.KILLER_SHOT.getFormattedText(), player.getDisplayName()));
            player.sendMessage(String.format(Language.VICTIM_SHOT.getFormattedText(), damager.getDisplayName()));
            corpse.setKillerName(damager.getDisplayName());
        }

    }

    @EventHandler
    public void onSpectatorChat(AsyncPlayerChatEvent e) {
        if (gameManager.getGameState() != GameState.RUNNING) return;
        if (!spectatorManager.isSpectator(e.getPlayer())) return;
        e.setCancelled(true);
        spectatorManager.getSpectators().forEach(player -> player.sendMessage(String.format(Language.SPECTATOR_MESSAGE.getText(), e.getPlayer().getDisplayName(), e.getMessage())));
    }

    @EventHandler
    public void onSpectatorDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && spectatorManager.isSpectator((Player) e.getDamager())) e.setCancelled(true);

        if (e.getEntity() instanceof Player && spectatorManager.isSpectator((Player) e.getEntity())) e.setCancelled(true);
    }

    @EventHandler
    public void onSpectatorArrowPickup(PlayerPickupArrowEvent e) {
        if (spectatorManager.isSpectator(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onSpectatorItemPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player && spectatorManager.isSpectator((Player) e.getEntity())) e.setCancelled(true);
    }


}
