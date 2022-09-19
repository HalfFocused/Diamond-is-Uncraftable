package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import io.github.halffocused.diamond_is_uncraftable.util.IWalkingStand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CWalkingStandTargetPacket implements IMessage<CWalkingStandTargetPacket> {
    @Override
    public void encode(CWalkingStandTargetPacket message, PacketBuffer buffer) {
    }

    @Override
    public CWalkingStandTargetPacket decode(PacketBuffer buffer) {
        return new CWalkingStandTargetPacket();
    }

    @Override
    public void handle(CWalkingStandTargetPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                assert sender != null;
                World world = sender.world;
                if (world != null)
                    if (!world.isRemote) {
                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                .filter(entity -> entity instanceof AbstractStandEntity)
                                .filter(entity -> entity instanceof IWalkingStand)
                                .filter(entity -> ((AbstractStandEntity) entity).getMaster().equals(sender))
                                .forEach(entity -> ((IWalkingStand) entity).getWalkingController().changeTargetting());
                    }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}