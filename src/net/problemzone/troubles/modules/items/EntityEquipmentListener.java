package net.problemzone.troubles.modules.items;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.problemzone.troubles.Main;
import net.problemzone.troubles.modules.game.GameManager;
import net.problemzone.troubles.modules.game.Role;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class EntityEquipmentListener extends PacketAdapter {

    private final GameManager gameManager;

    public EntityEquipmentListener(GameManager gameManager) {
        super(Main.getJavaPlugin(), PacketType.Play.Server.ENTITY_EQUIPMENT);
        this.gameManager = gameManager;
    }

    @Override
    public void onPacketSending(PacketEvent event) {

        PacketContainer packet = event.getPacket();
        ItemStack stack = packet.getItemModifier().read(0);

        if (stack != null && stack.getType() == Material.LEATHER_CHESTPLATE) {

            //Traitors will see other traitors with red armor
            if(gameManager.getPlayerRoleByPlayer(event.getPlayer()) != Role.TRAITOR) return;
            if(gameManager.getPlayerRoleByEntityID(packet.getIntegers().getValues().get(0)) != Role.TRAITOR) return;

            //Clone the Packet to not modify the original
            packet = event.getPacket().deepClone();
            event.setPacket(packet);
            stack = packet.getItemModifier().read(0);

            // Update the color
            LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
            assert meta != null;
            meta.setColor(Color.RED);
            stack.setItemMeta(meta);
        }
    }
}
