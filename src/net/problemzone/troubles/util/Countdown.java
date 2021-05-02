package net.problemzone.troubles.util;

import net.problemzone.troubles.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Countdown {

    public static void xpBarCountdown(int ticks, Language title) {

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setExp(1);
            player.setLevel(ticks / 20);
        });

        final float division = 1F / ticks;

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getExp() <= division) {
                        player.setExp(0);
                        if (!this.isCancelled()) this.cancel();
                    } else {
                        player.setExp(player.getExp() - division);
                    }
                });
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getLevel() <= 0) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);
                        if (!this.isCancelled()) this.cancel();
                        return;
                    }

                    player.setLevel(player.getLevel() - 1);

                    if (player.getLevel() <= 3) {
                        player.sendTitle(title.getText(), ChatColor.GREEN + "" + player.getLevel(), 0, 20, 0);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 0.5F);
                    }
                });
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

    public static void chatCountdown(int seconds, Set<Integer> exactCalls, Language title) {
        AtomicInteger remaining = new AtomicInteger(seconds);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (remaining.get() <= 0) this.cancel();
                if (exactCalls.contains(remaining.get())) {
                    Bukkit.broadcastMessage(title.getFormattedText() + remaining);
                    Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 0.5F));
                }
                remaining.getAndDecrement();
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

}