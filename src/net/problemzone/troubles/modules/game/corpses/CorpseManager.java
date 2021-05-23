package net.problemzone.troubles.modules.game.corpses;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import net.problemzone.troubles.Main;
import net.problemzone.troubles.modules.game.scoreboard.ScoreboardManager;
import net.problemzone.troubles.util.NMSPackets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CorpseManager {

    private static final int REMOVE_TIMER = 2;

    private final ScoreboardManager scoreboardManager;

    private final List<CorpseData> corpses = new ArrayList<>();

    public CorpseManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    public static DataWatcher clonePlayerDatawatcher(Player player, int currentEntId) {

        Location loc = player.getLocation();
        EntityHuman h = new EntityHuman(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(loc.getX(), loc.getY(), loc.getZ()), loc.getYaw(), ((CraftPlayer) player).getProfile()) {
            public BlockPosition getChunkCoordinates() {
                return null;
            }

            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return false;
            }
        };
        h.e(currentEntId);
        return h.getDataWatcher();
    }

    public static Location bedLocation(Location loc) {
        Location l = loc.clone();
        l.setY(1);
        return l;
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

    public CorpseData spawnCorpse(Player p, Location loc) {
        int entityId = getNextEntityIdAtomic().get();

        WrappedGameProfile prof = cloneProfileWithRandomUUID(WrappedGameProfile.fromPlayer(p), "");

        DataWatcher dw = clonePlayerDatawatcher(p, entityId);

        CorpseData data = getNMSCorpseData(loc, p.getInventory(), entityId, prof, dw);

        data.corpseName = p.getName();
        corpses.add(data);
        Objects.requireNonNull(data.getOrigLocation().getWorld()).getPlayers().forEach(data::sendCorpseToPlayer);

        return data;
    }

    private CorpseData getNMSCorpseData(Location loc, Inventory items, int entityId, WrappedGameProfile gp, DataWatcher dw) {
        DataWatcherObject<Byte> skinFlags = new DataWatcherObject<>(16, DataWatcherRegistry.a);
        dw.set(skinFlags, (byte) 0x7F);

        Location locUnder = getNonClippableBlockUnderPlayer(loc, 1);
        Location used = locUnder != null ? locUnder : loc;
        used.setYaw(loc.getYaw());
        used.setPitch(loc.getPitch());

        return new CorpseData(gp, used, entityId, items);
    }

    public void cowHit(Player player, CorpseData data) {
        player.sendMessage(data.getCorpseName());
    }

    //1.14 Change -- EntityCount is a AtomicInteger now
    public AtomicInteger getNextEntityIdAtomic() {
        try {
            Field entityCount = Entity.class.getDeclaredField("entityCount");
            entityCount.setAccessible(true);

            //Fix for final field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(entityCount, entityCount.getModifiers() & ~Modifier.FINAL);

            AtomicInteger id = (AtomicInteger) entityCount.get(null);
            id.incrementAndGet();
            entityCount.set(null, id);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return new AtomicInteger((int) Math.round(Math.random() * Integer.MAX_VALUE * 0.25));
        }
    }

    public List<CorpseData> getAllCorpses() {
        return corpses;
    }

    static class CustomEntityPlayer extends EntityPlayer {

        public CustomEntityPlayer(Player p, WrappedGameProfile prof) {
            super(((CraftWorld) p.getWorld()).getHandle().getMinecraftServer(), ((CraftWorld) p.getWorld()).getHandle(), new GameProfile(prof.getUUID(), prof.getName()), new PlayerInteractManager(((CraftWorld) p.getWorld()).getHandle()));
        }

    }

    public class CorpseData {

        private final WrappedGameProfile prof;
        private final Location loc;
        private final int entityId;
        private final Inventory items;
        private final int rotation;
        private String corpseName;
        private String killerName;

        public CorpseData(WrappedGameProfile prof, Location loc, int entityId, Inventory items) {
            this.prof = prof;
            this.loc = loc;
            this.entityId = entityId;
            this.items = items;
            this.rotation = yawToFacing(loc.getYaw());
        }

        private int yawToFacing(float yaw) {
            int facing = 0;
            if (yaw >= -45 && yaw <= 45 || yaw >= 315 || yaw <= -315) {
                facing = 1;
            } else if (yaw >= 45 && yaw <= 135 || yaw <= -225 && yaw >= -315) {
                facing = 2;
            } else if (yaw <= -45 && yaw >= -135 || yaw >= 225 && yaw <= 315) {
                facing = 3;
            }
            return facing;
        }

        @SuppressWarnings("deprecation")
        public void sendCorpseToPlayer(final Player p) {
            PacketContainer spawnPacket = NMSPackets.createHumanSpawnPacket(entityId, prof.getUUID(), loc);
            PacketContainer movePacket = NMSPackets.createEntityMoveDownPacket(entityId);
            PacketContainer addInfoPacket = NMSPackets.createPlayerInfoPacket(EnumWrappers.PlayerInfoAction.ADD_PLAYER, prof);
            PacketContainer nameTagPacket = NMSPackets.createScoreboardTeamPacket("");
            PacketContainer removeInfoPacket = NMSPackets.createPlayerInfoPacket(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, prof);

            Location bedLocation = bedLocation(loc);

            p.sendBlockChange(bedLocation, Material.RED_BED, (byte) rotation);
            NMSPackets.sendPacket(p, addInfoPacket);
            NMSPackets.sendPacket(p, spawnPacket);
            NMSPackets.sendPacket(p, movePacket);
            NMSPackets.sendPacket(p, nameTagPacket);

            List<WrappedWatchableObject> wrappedWatchableObjectList = Collections.singletonList(new WrappedWatchableObject(0, (byte) 0x01));
            Bukkit.broadcastMessage("CHECK");
            PacketContainer metadataPacket = NMSPackets.createPlayerMetadataPacket(entityId, wrappedWatchableObjectList);
            //NMSPackets.sendPacket(p, metadataPacket);

            makePlayerSleep(p, getBlockPositionFromBukkitLocation(bedLocation));

            new BukkitRunnable() {
                @Override
                public void run() {
                    NMSPackets.sendPacket(p, removeInfoPacket);
                }
            }.runTaskLater(Main.getJavaPlugin(), REMOVE_TIMER * 20L);

        }

        private BlockPosition getBlockPositionFromBukkitLocation(Location loc) {
            return new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        private void makePlayerSleep(Player p, BlockPosition bedPos) {
            PlayerConnection conn = ((CraftPlayer) p).getHandle().playerConnection;

            EntityPlayer entityPlayer = new CustomEntityPlayer(p, prof);
            entityPlayer.e(entityId); //sets the entity id

            try {
                //These lines force an entity player into the sleeping position
                Field poseF = Entity.class.getDeclaredField("POSE");
                poseF.setAccessible(true);
                DataWatcherObject<EntityPose> POSE = (DataWatcherObject<EntityPose>) poseF.get(null);
                entityPlayer.getDataWatcher().set(POSE, EntityPose.SLEEPING);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //entityPlayer.entitySleep(bedPos); //go to sleep
            conn.sendPacket(new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), false));
        }

        public Location getOrigLocation() {
            return loc;
        }

        public int getEntityId() {
            return entityId;
        }

        public Inventory getLootInventory() {
            return items;
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
