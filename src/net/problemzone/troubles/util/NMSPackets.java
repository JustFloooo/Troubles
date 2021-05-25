package net.problemzone.troubles.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class NMSPackets {

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

    public static PacketContainer createScoreboardTeamPacket(String uuid) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

        //Team Name
        packet.getStrings().write(0, "invisible");

        //Name Tag Visibility
        packet.getStrings().write(1, "never");

        //Team Members
        packet.getSpecificModifier(Collection.class).write(0, Collections.singletonList(uuid));

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
        for (Player player : players) {
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

    public static PacketContainer createPlayerInfoPacket(EnumWrappers.PlayerInfoAction action, WrappedGameProfile profile) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

        //Player Info Action
        packet.getPlayerInfoAction().write(0, action);

        //Player Info Data
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 20, EnumWrappers.NativeGameMode.NOT_SET, WrappedChatComponent.fromText(""));
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        return packet;
    }

    public static PacketContainer createHumanSpawnPacket(int id, UUID uuid, Location loc) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

        //Entity ID
        packet.getIntegers().write(0, id);

        //UUID
        packet.getUUIDs().write(0, uuid);

        //Coordinates
        packet.getDoubles().write(0, loc.getX());
        packet.getDoubles().write(1, loc.getY());
        packet.getDoubles().write(2, loc.getZ());

        //Angles
        packet.getBytes().write(0, (byte) (loc.getYaw() * 256F / 360F));
        packet.getBytes().write(1, (byte) (loc.getPitch() * 256F / 360F));

        return packet;
    }

    public static PacketContainer createEntityMoveDownPacket(int id) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);

        //Entity ID
        packet.getIntegers().write(0, id);

        //Y Change
        packet.getShorts().write(1, (short) (-61.8));

        return packet;

    }

    public static PacketContainer createPlayerMetadataPacket(int id, List<WrappedWatchableObject> metadata) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        //Entity ID
        packet.getIntegers().write(0, id);

        //Metadata
        packet.getWatchableCollectionModifier().write(0, metadata);

        return packet;
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
