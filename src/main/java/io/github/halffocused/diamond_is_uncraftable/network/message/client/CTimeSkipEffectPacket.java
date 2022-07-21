package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

/**
 * Controls Aerosmith's actions through keybinds,
 */
@SuppressWarnings("ConstantConditions")
public class CTimeSkipEffectPacket implements IMessage<CTimeSkipEffectPacket> {
    private int entityId;

    public CTimeSkipEffectPacket(int entityId) {
        this.entityId = entityId;
    }

    public CTimeSkipEffectPacket() {
    }

    @Override
    public void handle(CTimeSkipEffectPacket message, Supplier<Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                assert sender != null;
                World world = sender.world;
                if (world != null)
                    if (!world.isRemote) {
                        Stand.getLazyOptional(sender).ifPresent(props -> {
                            props.setTimeSkipEffectTicker(Math.max(props.getTimeSkipEffectTicker() - 1, 0));
                        });
                    }
            });
        }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(CTimeSkipEffectPacket message, PacketBuffer buffer) {
        buffer.writeInt(message.entityId);
    }

    @Override
    public CTimeSkipEffectPacket decode(PacketBuffer buffer) {
        return new CTimeSkipEffectPacket(
            buffer.readInt()
        );
    }

    public enum Direction {FORWARDS, BACKWARDS, RIGHT, LEFT, UP, DOWN}
}
