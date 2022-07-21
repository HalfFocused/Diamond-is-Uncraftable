package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CChargeAttackPacket implements IMessage<CChargeAttackPacket> {
    private boolean isAttackKeyDown;
    private boolean isBarraging;

    public CChargeAttackPacket(boolean isAttackKeyDown, boolean isBarraging) {
        this.isAttackKeyDown = isAttackKeyDown;
        this.isBarraging = isBarraging;
    }

    public CChargeAttackPacket() {
    }

    @Override
    public void encode(CChargeAttackPacket message, PacketBuffer buffer) {
        buffer.writeBoolean(message.isAttackKeyDown);
        buffer.writeBoolean(message.isBarraging);
    }

    @Override
    public CChargeAttackPacket decode(PacketBuffer buffer) {
        return new CChargeAttackPacket(buffer.readBoolean(),buffer.readBoolean());
    }

    @Override
    public void handle(CChargeAttackPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null) return;
                if (!sender.world.isRemote)
                    sender.getServerWorld().getEntities()
                            .filter(entity -> entity instanceof AbstractStandEntity)
                            .filter(entity -> ((AbstractStandEntity) entity).getMaster().equals(sender))
                            .forEach(entity -> ((AbstractStandEntity) entity).chargeAttack(message.isAttackKeyDown, message.isBarraging));
            });
        }
        ctx.get().setPacketHandled(true);
    }
}

