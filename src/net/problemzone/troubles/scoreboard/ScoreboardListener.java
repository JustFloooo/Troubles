package net.problemzone.troubles.scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class ScoreboardListener implements Listener {

    private final ScoreboardHandler scoreboardHandler;

    public ScoreboardListener(ScoreboardHandler scoreboardHandler) {
        this.scoreboardHandler = scoreboardHandler;
    }

    @EventHandler
    public void scoreboardPlayerDeath(PlayerDeathEvent event) {

        if(Objects.requireNonNull(event.getEntity().getLastDamageCause()).getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (event.getEntity().getKiller() != null) {
                scoreboardHandler.increaseKillCounter(event.getEntity().getKiller());
            }
        }
    }
}

