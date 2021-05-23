package net.problemzone.troubles.util;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public enum Sounds {

    CHEST_ITEM(Sound.BLOCK_CHEST_OPEN, 1),
    ENDER_CHEST_ITEM(Sound.BLOCK_ENDER_CHEST_CLOSE, 1),
    RECEIVE_ROLE(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1),
    DEATH(Sound.BLOCK_ANVIL_LAND, 0.5F),
    KILL(Sound.ENTITY_PLAYER_LEVELUP, 1.5F),
    GAME_WIN_INNO(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.3F),
    GAME_WIN_TRAITOR(Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F),
    CLICK_TIMER(Sound.BLOCK_NOTE_BLOCK_HAT, 1),
    CLICK_TIMER_END(Sound.BLOCK_NOTE_BLOCK_HARP, 2);

    private final Sound sound;
    private final float pitch;

    Sounds(Sound sound, float pitch) {
        this.sound = sound;
        this.pitch = pitch;
    }

    public void playSoundForPlayer(Player player){
        player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT, 1, pitch);
    }

}
