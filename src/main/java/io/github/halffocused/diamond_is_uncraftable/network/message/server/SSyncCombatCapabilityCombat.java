package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.capability.CombatCapability;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SSyncCombatCapabilityCombat implements IMessage<SSyncCombatCapabilityCombat> {
    private INBT data;

    public SSyncCombatCapabilityCombat() {
    }

    private SSyncCombatCapabilityCombat(CompoundNBT compoundNBT) {
        data = compoundNBT;
    }

    public SSyncCombatCapabilityCombat(CombatCapability props) {
        data = CombatCapability.COMBAT.getStorage().writeNBT(CombatCapability.COMBAT, props, null);
    }

    @Override
    public void encode(SSyncCombatCapabilityCombat message, PacketBuffer buffer) {
        buffer.writeCompoundTag((CompoundNBT) message.data);
    }

    @Override
    public SSyncCombatCapabilityCombat decode(PacketBuffer buffer) {
        return new SSyncCombatCapabilityCombat(buffer.readCompoundTag());
    }

    @Override
    public void handle(SSyncCombatCapabilityCombat message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                if (Minecraft.getInstance().world == null) return;
                Minecraft.getInstance().world.getAllEntities().forEach(entity -> CombatCapability.getLazyOptional(entity).ifPresent(props ->
                        CombatCapability.COMBAT.getStorage().readNBT(CombatCapability.COMBAT, props, null, message.data)
                ));
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
