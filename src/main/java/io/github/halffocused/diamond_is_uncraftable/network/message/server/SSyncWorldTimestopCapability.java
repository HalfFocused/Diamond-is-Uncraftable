package io.github.halffocused.diamond_is_uncraftable.network.message.server;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.WorldTimestopCapability;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SSyncWorldTimestopCapability implements IMessage<SSyncWorldTimestopCapability> {
    private INBT data;

    public SSyncWorldTimestopCapability() {
    }

    private SSyncWorldTimestopCapability(CompoundNBT compoundNBT) {
        data = compoundNBT;
    }

    public SSyncWorldTimestopCapability(WorldTimestopCapability props) {
        data = WorldTimestopCapability.WORLD.getStorage().writeNBT(WorldTimestopCapability.WORLD, props, null);
    }

    @Override
    public SSyncWorldTimestopCapability decode(PacketBuffer buffer) {
        return new SSyncWorldTimestopCapability(buffer.readCompoundTag());
    }

    @Override
    public void handle(SSyncWorldTimestopCapability message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                World world = DiamondIsUncraftable.PROXY.getWorld();
                if(world == null) return;
                WorldTimestopCapability.getLazyOptional(world).ifPresent(props -> WorldTimestopCapability.WORLD.getStorage().readNBT(WorldTimestopCapability.WORLD, props, null, message.data));
            });
        }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(SSyncWorldTimestopCapability message, PacketBuffer buffer) {
        buffer.writeCompoundTag((CompoundNBT) message.data);
    }
}
