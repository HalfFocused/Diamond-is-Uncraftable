package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SRemoveEntityPacket implements IMessage<SRemoveEntityPacket> {
    private int entityID;

    public SRemoveEntityPacket() {
    }

    public SRemoveEntityPacket(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public void encode(SRemoveEntityPacket message, PacketBuffer buffer) {
        buffer.writeInt(message.entityID);
    }

    @Override
    public SRemoveEntityPacket decode(PacketBuffer buffer) {
        return new SRemoveEntityPacket(buffer.readInt());
    }

    @Override
    public void handle(SRemoveEntityPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
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
