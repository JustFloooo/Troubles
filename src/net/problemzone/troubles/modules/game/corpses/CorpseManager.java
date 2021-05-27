package net.problemzone.troubles.modules.game.corpses;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import net.problemzone.troubles.Main;
import net.problemzone.troubles.util.NMSPackets;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CorpseManager {

    private static final int REMOVE_TIMER = 2;
    private static final FieldAccessor ENTITY_ID_ACCESSOR = Accessors.getFieldAccessor(MinecraftReflection.getEntityClass(), "entityCount", true);

    private final List<CorpseData> corpses = new ArrayList<>();

    public CorpseData spawnCorpse(Player p, Location loc) {
        int entityId = getNextEntityIdAtomic();
        WrappedGameProfile prof = cloneProfileWithRandomUUID(WrappedGameProfile.fromPlayer(p), "");

        Location locUnder = getNonClippableBlockUnderPlayer(loc, 1);
        Location used = locUnder != null ? locUnder : loc;
        used.setYaw(loc.getYaw());
        used.setPitch(loc.getPitch());

        CorpseData data = new CorpseData(prof, used, entityId);

        data.corpseName = p.getName();
        corpses.add(data);
        Objects.requireNonNull(data.getOrigLocation().getWorld()).getPlayers().forEach(data::sendCorpseToPlayer);

        return data;
    }

    public WrappedGameProfile cloneProfileWithRandomUUID(WrappedGameProfile oldProf, String name) {
        WrappedGameProfile newProf = new WrappedGameProfile(UUID.randomUUID(), name);
        newProf.getProperties().putAll(oldProf.getProperties());
        return newProf;
    }

    public Location getNonClippableBlockUnderPlayer(Location loc, int addToYPos) {
        if (loc.getBlockY() < 0) {
            return null;
        }
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Material m = Objects.requireNonNull(loc.getWorld()).getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType();
            if (m.isSolid()) {
                return new Location(loc.getWorld(), loc.getX(), y + addToYPos,
                        loc.getZ());
            }
        }
        return null;
    }

    public int getNextEntityIdAtomic() {

        try{
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(ENTITY_ID_ACCESSOR.getField(), ENTITY_ID_ACCESSOR.getField().getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AtomicInteger atomicId = (AtomicInteger) ENTITY_ID_ACCESSOR.get(null);
        int id = atomicId.incrementAndGet();
        ENTITY_ID_ACCESSOR.set(null, atomicId);
        return id;
    }

    public List<CorpseData> getAllCorpses() {
        return corpses;
    }

    public void cowHit(Player player, CorpseData data) {
        player.sendMessage(data.getCorpseName());
    }

    public static class CorpseData {

        private final WrappedGameProfile prof;
        private final Location loc;
        private final int entityId;
        private String corpseName;
        private String killerName;

        public CorpseData(WrappedGameProfile prof, Location loc, int entityId) {
            this.prof = prof;
            this.loc = loc;
            this.entityId = entityId;
        }

        public void sendCorpseToPlayer(final Player p) {
            PacketContainer spawnPacket = NMSPackets.createHumanSpawnPacket(entityId, prof.getUUID(), loc);
            PacketContainer movePacket = NMSPackets.createEntityMoveDownPacket(entityId);
            PacketContainer addInfoPacket = NMSPackets.createPlayerInfoPacket(EnumWrappers.PlayerInfoAction.ADD_PLAYER, prof);
            PacketContainer nameTagPacket = NMSPackets.createScoreboardTeamPacket("");
            PacketContainer removeInfoPacket = NMSPackets.createPlayerInfoPacket(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, prof);

            NMSPackets.sendPacket(p, addInfoPacket);
            NMSPackets.sendPacket(p, spawnPacket);
            NMSPackets.sendPacket(p, movePacket);
            NMSPackets.sendPacket(p, nameTagPacket);

            WrappedWatchableObject skin = new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(16, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0xff);
            WrappedWatchableObject pose = new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(6, WrappedDataWatcher.Registry.get(EnumWrappers.getEntityPoseClass())), EnumWrappers.EntityPose.SLEEPING.toNms());
            List<WrappedWatchableObject> wrappedWatchableObjectList = List.of(skin, pose);
            PacketContainer metadataPacket = NMSPackets.createPlayerMetadataPacket(entityId, wrappedWatchableObjectList);
            NMSPackets.sendPacket(p, metadataPacket);

            new BukkitRunnable() {
                @Override
                public void run() {
                    NMSPackets.sendPacket(p, removeInfoPacket);
                }
            }.runTaskLater(Main.getJavaPlugin(), REMOVE_TIMER * 20L);

        }

        public Location getOrigLocation() {
            return loc;
        }

        public int getEntityId() {
            return entityId;
        }

        public String getCorpseName() {
            return corpseName;
        }

        public String getKillerName() {
            return killerName;
        }

        public void setKillerName(String killerName) {
            this.killerName = killerName;
        }

    }

}
