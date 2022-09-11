package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CRemoveEntityPacket implements IMessage<CRemoveEntityPacket> {
    private int entityID;

    public CRemoveEntityPacket() {
    }

    public CRemoveEntityPacket(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public void encode(CRemoveEntityPacket message, PacketBuffer buffer) {
        buffer.writeInt(message.entityID);
    }

    @Override
    public CRemoveEntityPacket decode(PacketBuffer buffer) {
        return new CRemoveEntityPacket(buffer.readInt());
    }

    @Override
    public void handle(CRemoveEntityPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                World world = DiamondIsUncraftable.PROXY.getWorld();
                if (world == null) return;
                Entity entity = world.getEntityByID(message.entityID);
                if(entity == null) return;
                entity.remove();

            });
        }
        ctx.get().setPacketHandled(true);
    }
}
