package net.problemzone.troubles.modules.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.problemzone.troubles.modules.game.Role;
import net.problemzone.troubles.modules.scoreboard.ScoreboardManager;
import net.problemzone.troubles.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

public class PlayerManager {

    private final ScoreboardManager scoreboardManager;

    public PlayerManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    public void tellRoleToPlayer(Player player, Role role) {
        player.sendTitle(role.getRoleName().getText(), role.getRoleDescription().getText(), 10, 60, 10);
        player.sendMessage(String.format(Language.ROLE_ASSIGNED.getFormattedText(), role.getRoleName().getText()) + " " + ChatColor.GRAY + role.getRoleDescription().getText());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);

        scoreboardManager.setScoreboard(player, role);
    }

    public PacketContainer createPlayerNameColorPacket(List<Player> player, ChatColor color, String name) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        //Mode (Byte?)
        packet.getIntegers().write(1, 0);

        //Team Name
        packet.getStrings().write(0, name);

        //Team Display Name (Needed?)
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(name));

        //Team Prefix
        packet.getChatComponents().write(1, WrappedChatComponent.fromText(color + " "));

        //Team Members
        packet.getSpecificModifier(Collection.class).write(0, player);

        return packet;
    }

    public PacketContainer createFakeArmorPacket(Player player){
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        return packet;
        //TODO: create packet, rethink logic
    }

    public void sendPacket(Player player, PacketContainer packet){
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet " + packet, e);
        }
    }



}
