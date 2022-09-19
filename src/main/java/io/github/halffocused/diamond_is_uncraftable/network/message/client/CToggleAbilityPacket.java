package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.event.custom.AbilityEvent;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CToggleAbilityPacket implements IMessage<CToggleAbilityPacket> {
    @Override
    public void encode(CToggleAbilityPacket message, PacketBuffer buffer) {
    }

    @Override
    public CToggleAbilityPacket decode(PacketBuffer buffer) {
        return new CToggleAbilityPacket();
    }

    @Override
    public void handle(CToggleAbilityPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null) return;
                Stand.getLazyOptional(sender).ifPresent(props -> {
                    int standID = props.getStandID();
                    int act = props.getAct();

                    props.setAbility(!props.getAbility());

                    if (props.getAbility()) {
                        switch (standID) {
                            case Util.StandID.KING_CRIMSON:
                            case Util.StandID.THE_WORLD:
                            case Util.StandID.KILLER_QUEEN:
                                break;
                            default: {
                                sender.sendStatusMessage(new StringTextComponent("Ability: ON"), true);
                                break;
                            }
                        }
                    } else {
                        switch (standID) {
                            case Util.StandID.KING_CRIMSON:
                            case Util.StandID.THE_WORLD:
                            case Util.StandID.KILLER_QUEEN:
                                break;
                            default: {
                                sender.sendStatusMessage(new StringTextComponent("Ability: OFF"), true);
                                break;
                            }
                        }
                    }
                    MinecraftForge.EVENT_BUS.post(props.getAbility() ? new AbilityEvent.AbilityActivated(sender) : new AbilityEvent.AbilityDeactivated(sender));
                });
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
