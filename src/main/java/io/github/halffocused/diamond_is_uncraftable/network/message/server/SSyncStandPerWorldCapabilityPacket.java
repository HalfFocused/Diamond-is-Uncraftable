package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.StandPerWorldCapability;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SSyncStandPerWorldCapabilityPacket implements IMessage<SSyncStandPerWorldCapabilityPacket> {
    private INBT data;

    public SSyncStandPerWorldCapabilityPacket() {
    }

    private SSyncStandPerWorldCapabilityPacket(CompoundNBT compoundNBT) {
        data = compoundNBT;
    }

    public SSyncStandPerWorldCapabilityPacket(StandPerWorldCapability props) {
        data = StandPerWorldCapability.WORLD.getStorage().writeNBT(StandPerWorldCapability.WORLD, props, null);
    }

    @Override
    public SSyncStandPerWorldCapabilityPacket decode(PacketBuffer buffer) {
        return new SSyncStandPerWorldCapabilityPacket(buffer.readCompoundTag());
    }

    @Override
    public void handle(SSyncStandPerWorldCapabilityPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                if(DiamondIsUncraftable.PROXY.getWorld() == null) return;
                StandPerWorldCapability.getLazyOptional(DiamondIsUncraftable.PROXY.getWorld()).ifPresent(props -> StandPerWorldCapability.WORLD.getStorage().readNBT(StandPerWorldCapability.WORLD, props, null, message.data));
            });
        }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(SSyncStandPerWorldCapabilityPacket message, PacketBuffer buffer) {
        buffer.writeCompoundTag((CompoundNBT) message.data);
    }
}
