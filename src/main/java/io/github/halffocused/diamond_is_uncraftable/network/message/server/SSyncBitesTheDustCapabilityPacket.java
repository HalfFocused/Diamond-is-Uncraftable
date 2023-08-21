package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.capability.BitesTheDustCapability;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SSyncBitesTheDustCapabilityPacket implements IMessage<SSyncBitesTheDustCapabilityPacket> {
    private INBT data;

    public SSyncBitesTheDustCapabilityPacket() {
    }

    private SSyncBitesTheDustCapabilityPacket(CompoundNBT compoundNBT) {
        data = compoundNBT;
    }

    public SSyncBitesTheDustCapabilityPacket(BitesTheDustCapability props) {
        data = BitesTheDustCapability.BITES_THE_DUST.getStorage().writeNBT(BitesTheDustCapability.BITES_THE_DUST, props, null);
    }

    @Override
    public SSyncBitesTheDustCapabilityPacket decode(PacketBuffer buffer) {
        return new SSyncBitesTheDustCapabilityPacket(buffer.readCompoundTag());
    }

    @Override
    public void handle(SSyncBitesTheDustCapabilityPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                if (Minecraft.getInstance().world == null) return;
                Minecraft.getInstance().world.getAllEntities().forEach(entity -> BitesTheDustCapability.getLazyOptional(entity).ifPresent(props ->
                        BitesTheDustCapability.BITES_THE_DUST.getStorage().readNBT(BitesTheDustCapability.BITES_THE_DUST, props, null, message.data)
                ));
            });
        }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(SSyncBitesTheDustCapabilityPacket message, PacketBuffer buffer) {
        buffer.writeCompoundTag((CompoundNBT) message.data);
    }
}
