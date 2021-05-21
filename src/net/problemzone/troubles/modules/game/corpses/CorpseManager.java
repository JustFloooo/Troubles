package net.problemzone.troubles.modules.game.corpses;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import net.problemzone.troubles.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CorpseManager {

    private final List<CorpseData> corpses = new ArrayList<>();

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

    public GameProfile cloneProfileWithRandomUUID(GameProfile oldProf, String name) {
        GameProfile newProf = new GameProfile(UUID.randomUUID(), name);
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
        GameProfile prof = cloneProfileWithRandomUUID(((CraftPlayer) p).getProfile(), p.getDisplayName());

        DataWatcher dw = clonePlayerDatawatcher(p, entityId);

        CorpseData data = getNMSCorpseData(loc, p.getInventory(), entityId, prof, dw);

        data.corpseName = p.getName();
        corpses.add(data);
        //spawnSlimeForCorpse(data);
        Objects.requireNonNull(data.getOrigLocation().getWorld()).getPlayers().forEach(data::sendCorpseToPlayer);

        return data;
    }

    private CorpseData getNMSCorpseData(Location loc, Inventory items, int entityId, GameProfile gp, DataWatcher dw) {
        DataWatcherObject<Byte> skinFlags = new DataWatcherObject<>(16, DataWatcherRegistry.a);
        dw.set(skinFlags, (byte) 0x7F);

        Location locUnder = getNonClippableBlockUnderPlayer(loc, 1);
        Location used = locUnder != null ? locUnder : loc;
        used.setYaw(loc.getYaw());
        used.setPitch(loc.getPitch());

        return new CorpseData(gp, used, entityId, items);
    }

    public void removeCorpse(CorpseData data) {
        corpses.remove(data);
        data.destroyCorpseFromEveryone();
        if (data.getLootInventory() != null) {
            data.getLootInventory().clear();
            List<HumanEntity> close = new ArrayList<>(data
                    .getLootInventory().getViewers());
            for (HumanEntity p : close) {
                p.closeInventory();
            }
        }
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

        public CustomEntityPlayer(Player p, GameProfile prof) {
            super(((CraftWorld) p.getWorld()).getHandle().getMinecraftServer(), ((CraftWorld) p.getWorld()).getHandle(), prof, new PlayerInteractManager(((CraftWorld) p.getWorld()).getHandle()));
        }

    }

    public class CorpseData {

        private final GameProfile prof;
        private final Location loc;
        private final int entityId;
        private final Inventory items;
        private final int rotation;
        private String corpseName;
        private String killerName;

        public CorpseData(GameProfile prof, Location loc, int entityId, Inventory items) {
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

        public PacketPlayOutNamedEntitySpawn getSpawnPacket() {
            PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, entityId);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet, prof.getId());
                Field c = packet.getClass().getDeclaredField("c");
                c.setAccessible(true);
                c.setDouble(packet, loc.getX());
                Field d = packet.getClass().getDeclaredField("d");
                d.setAccessible(true);
                d.setDouble(packet, loc.getY() + 1.0f / 16.0f);
                Field e = packet.getClass().getDeclaredField("e");
                e.setAccessible(true);
                e.setDouble(packet, loc.getZ());
                Field f = packet.getClass().getDeclaredField("f");
                f.setAccessible(true);
                f.setByte(packet, (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
                Field g = packet.getClass().getDeclaredField("g");
                g.setAccessible(true);
                g.setByte(packet,
                        (byte) (int) (loc.getPitch() * 256.0F / 360.0F));

            } catch (Exception e) {

                e.printStackTrace();
            }
            return packet;
        }

        public PacketPlayOutEntity.PacketPlayOutRelEntityMove getMovePacket() {
            return new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                    entityId, (short) (0), (short) (-61.8), (short) (0), false);
        }

        public PacketPlayOutPlayerInfo getInfoPacket() {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
            return getPacketPlayOutPlayerInfo(packet);
        }

        public PacketPlayOutPlayerInfo getRemoveInfoPacket() {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
            return getPacketPlayOutPlayerInfo(packet);
        }

        private PacketPlayOutPlayerInfo getPacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo packet) {
            try {
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<PacketPlayOutPlayerInfo.PlayerInfoData> data = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) b
                        .get(packet);
                data.add(packet.new PlayerInfoData(prof, -1,
                        EnumGamemode.SURVIVAL, new ChatMessage("[CR]")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return packet;
        }

        @SuppressWarnings("deprecation")
        public void sendCorpseToPlayer(final Player p) {
            PacketPlayOutNamedEntitySpawn spawnPacket = getSpawnPacket();
            PacketPlayOutEntity.PacketPlayOutRelEntityMove movePacket = getMovePacket();
            PacketPlayOutPlayerInfo infoPacket = getInfoPacket();
            final PacketPlayOutPlayerInfo removeInfo = getRemoveInfoPacket();


            PlayerConnection conn = ((CraftPlayer) p).getHandle().playerConnection;
            Location bedLocation = bedLocation(loc);

            p.sendBlockChange(bedLocation, Material.RED_BED, (byte) rotation);
            conn.sendPacket(infoPacket);
            conn.sendPacket(spawnPacket);

            conn.sendPacket(movePacket);

            makePlayerSleep(p, conn, getBlockPositionFromBukkitLocation(bedLocation));

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getJavaPlugin(), () -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(removeInfo), 40L);

        }

        private BlockPosition getBlockPositionFromBukkitLocation(Location loc) {
            return new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        private void makePlayerSleep(Player p, PlayerConnection conn, BlockPosition bedPos) {
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

            entityPlayer.entitySleep(bedPos); //go to sleep
            conn.sendPacket(new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), false));
        }

        public Location getOrigLocation() {
            return loc;
        }

        @SuppressWarnings("deprecation")
        public void destroyCorpseFromEveryone() {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(
                    entityId);
            Block b = loc.clone().subtract(0, 2, 0).getBlock();
            boolean removeBed = true;
            for (CorpseData cd : getAllCorpses()) {
                if (cd != this
                        && bedLocation(cd.getOrigLocation())
                        .getBlock().getLocation()
                        .equals(b.getLocation())) {
                    removeBed = false;
                    break;
                }
            }
            for (Player p : Objects.requireNonNull(loc.getWorld()).getPlayers()) {
                ((CraftPlayer) p).getHandle().playerConnection
                        .sendPacket(packet);
                if (removeBed) {
                    p.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                }
            }
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
