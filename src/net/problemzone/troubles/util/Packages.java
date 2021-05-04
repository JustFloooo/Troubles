package net.problemzone.troubles.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Packages {

    public static PacketContainer createPlayerNameColorPacket(List<Player> players, ChatColor color, String name) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);

        //Friendly Fire On
        packet.getIntegers().write(1, 1);

        //Team Name
        packet.getStrings().write(0, name);

        //Team Display Name (Needed?)
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(name));

        //Team Color
        packet.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);

        //Team Members
        packet.getSpecificModifier(Collection.class).write(0, players.stream().map(Player::getDisplayName).collect(Collectors.toList()));

        return packet;
    }

    public static List<PacketContainer> createFakeArmorPacket(List<Player> players, Color color) {
        List<PacketContainer> packets = new ArrayList<>();

        //Create Leather Armor with specified color
        ItemStack leatherArmor = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherArmor.getItemMeta();
        assert leatherArmorMeta != null;
        leatherArmorMeta.setColor(color);
        leatherArmor.setItemMeta(leatherArmorMeta);

        //Create Armor Package for each given player
        for(Player player : players){
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

            //Entity ID
            packet.getIntegers().write(0, player.getEntityId());

            //Item Slot
            packet.getItemSlots().write(0, EnumWrappers.ItemSlot.CHEST);

            //Item
            packet.getItemModifier().write(0, leatherArmor);

            packets.add(packet);
        }

        return packets;
    }

    public static void sendPacket(Player player, PacketContainer... packets) {
        for (PacketContainer packet : packets) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot send packet " + packet, e);
            }
        }
    }


}
