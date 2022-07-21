package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.capability.StandPerWorldCapability;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.Supplier;


@SuppressWarnings("ConstantConditions")
public class CAddStandToWorldData implements IMessage<CAddStandToWorldData> {
    private int standId;

    public CAddStandToWorldData(int entityId) {
        this.standId = entityId;
    }

    public CAddStandToWorldData() {
    }

    @Override
    public void handle(CAddStandToWorldData message, Supplier<Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                assert sender != null;
                World world = sender.world;
                if (world != null)
                    if (!world.isRemote) {
                        Objects.requireNonNull(world.getServer()).getWorld(DimensionType.OVERWORLD);
                        StandPerWorldCapability.getLazyOptional(world.getServer().getWorld(DimensionType.OVERWORLD)).ifPresent(uniqueStandHandler -> {
                            uniqueStandHandler.addTakenStandId(standId);
                        });
                    }
                });
            }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(CAddStandToWorldData message, PacketBuffer buffer) {
        buffer.writeInt(message.standId);
    }

    @Override
    public CAddStandToWorldData decode(PacketBuffer buffer) {
        return new CAddStandToWorldData(
            buffer.readInt()
        );
    }
}
