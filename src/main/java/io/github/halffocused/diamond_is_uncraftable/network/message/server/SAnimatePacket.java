package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.function.Supplier;

public class SAnimatePacket implements IMessage<SAnimatePacket> {
    private int entityID;
    private String animation;
    private boolean shouldLoop;

    public SAnimatePacket() {
    }

    public SAnimatePacket(int entityID, String animation, boolean shouldLoop) {
        this.entityID = entityID;
        this.animation = animation;
        this.shouldLoop = shouldLoop;
    }

    @Override
    public void encode(SAnimatePacket message, PacketBuffer buffer) {
        buffer.writeInt(message.entityID);
        buffer.writeString(message.animation);
        buffer.writeBoolean(message.shouldLoop);
    }

    @Override
    public SAnimatePacket decode(PacketBuffer buffer) {
        return new SAnimatePacket(buffer.readInt(), buffer.readString(), buffer.readBoolean());
    }

    @Override
    public void handle(SAnimatePacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                ClientWorld world = Minecraft.getInstance().world;
                if (world == null) return;
                Entity entity = world.getEntityByID(message.entityID);
                if (!(entity instanceof IAnimatable)) return;

                ((AbstractStandEntity) entity).setAnimation(message.animation, message.shouldLoop);


            });
        }
        ctx.get().setPacketHandled(true);
    }
}
