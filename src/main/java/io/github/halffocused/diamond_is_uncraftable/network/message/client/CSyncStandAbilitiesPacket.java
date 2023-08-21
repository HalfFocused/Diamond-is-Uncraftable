package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandEffects;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.*;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import io.github.halffocused.diamond_is_uncraftable.util.globalabilities.BitesTheDustHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.StandID.*;

public class CSyncStandAbilitiesPacket implements IMessage<CSyncStandAbilitiesPacket> {
    private byte action;

    public CSyncStandAbilitiesPacket() {
    }

    public CSyncStandAbilitiesPacket(byte action) {
        this.action = action;
    }

    @Override
    public void encode(CSyncStandAbilitiesPacket message, PacketBuffer buffer) {
        buffer.writeByte(message.action);
    }

    @Override
    public CSyncStandAbilitiesPacket decode(PacketBuffer buffer) {
        return new CSyncStandAbilitiesPacket(buffer.readByte());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(CSyncStandAbilitiesPacket message, Supplier<Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                if (sender == null) return;
                World world = sender.world;
                if (world != null) {
                    if (!world.isRemote) {
                        Stand.getLazyOptional(sender).ifPresent(props -> {
                            if(props.getStandOn()) {
                                switch (props.getStandID()) {
                                    case KING_CRIMSON: {
                                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                                .filter(entity -> entity instanceof KingCrimsonEntity)
                                                .filter(entity -> ((KingCrimsonEntity) entity).getMaster().equals(sender))
                                                .forEach(entity -> {
                                                    if (message.action == 1)
                                                        ((KingCrimsonEntity) entity).epitaph();
                                                    else
                                                        ((KingCrimsonEntity) entity).teleport(8.01714285714);
                                                });
                                        break;
                                    }
                                    case KILLER_QUEEN:
                                    case KILLER_QUEEN_BTD: {
                                        if (StandEffects.getCapabilityFromEntity(sender).isThreeFreeze()) return;
                                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                                .filter(entity -> entity instanceof KillerQueenEntity)
                                                .filter(entity -> ((KillerQueenEntity) entity).getMaster().equals(sender))
                                                .forEach(entity -> {
                                                    switch (message.action) {
                                                        case 1: {
                                                            ((KillerQueenEntity) entity).detonate();
                                                            break;
                                                        }
                                                        case 2: {
                                                            ((KillerQueenEntity) entity).toggleSheerHeartAttack();
                                                            break;
                                                        }
                                                        case 3: {
                                                            ((KillerQueenEntity) entity).turnItemOrBlockIntoBomb();
                                                            break;
                                                        }
                                                        default:
                                                            break;
                                                    }
                                                });
                                        break;
                                    }
                                    case THE_WORLD: {
                                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                                .filter(entity -> entity instanceof TheWorldEntity)
                                                .filter(entity -> ((TheWorldEntity) entity).getMaster().equals(sender))
                                                .forEach(entity -> {
                                                    switch (message.action) {
                                                        case 1: {
                                                            ((TheWorldEntity) entity).timeStop();
                                                            break;
                                                        }
                                                        case 2: {
                                                            ((TheWorldEntity) entity).teleport();
                                                            break;
                                                        }
                                                        default:
                                                            break;
                                                    }
                                                });
                                        break;
                                    }
                                    case STICKY_FINGERS: {
                                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                                .filter(entity -> entity instanceof StickyFingersEntity)
                                                .filter(entity -> ((StickyFingersEntity) entity).getMaster().equals(sender))
                                                .forEach(entity -> {
                                                    switch (message.action) {
                                                        default:
                                                            break;
                                                        case 1: {
                                                            ((StickyFingersEntity) entity).toggleZippedBlock();
                                                            break;
                                                        }
                                                        case 2: {
                                                            ((StickyFingersEntity) entity).addZippedBlock();
                                                            break;
                                                        }
                                                        case 3: {
                                                            ((StickyFingersEntity) entity).zipLine();
                                                            break;
                                                        }
                                                    }
                                                });
                                        break;
                                    }

                                    case SILVER_CHARIOT: {
                                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                                .filter(entity -> entity instanceof SilverChariotEntity)
                                                .filter(entity -> ((SilverChariotEntity) entity).getMaster().equals(sender))
                                                .forEach(entity -> {
                                                    if (message.action == 1)
                                                        ((SilverChariotEntity) entity).ability();
                                                });
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            }else{ //Stand off abilities
                                switch (props.getStandID()) {
                                    case KILLER_QUEEN_BTD: {
                                        world.getServer().getWorld(sender.world.getDimensionKey()).getEntities()
                                                .filter(entity -> entity instanceof PlayerEntity)
                                                .forEach(entity -> {
                                                    switch (message.action) {
                                                        case 1: {
                                                            System.out.println("Sneaking: " + entity.isSneaking());
                                                            System.out.println("Correct Player: " + BitesTheDustHelper.bitesTheDustPlayer.equals(entity));
                                                            System.out.println("Bites The Dust Active: " + BitesTheDustHelper.bitesTheDustActive);
                                                            if(entity.isSneaking() && BitesTheDustHelper.bitesTheDustPlayer.equals(entity) && BitesTheDustHelper.bitesTheDustActive){
                                                                BitesTheDustHelper.rewindBitesTheDust();
                                                            }
                                                            break;
                                                        }
                                                        default:
                                                            break;
                                                    }
                                                });
                                        break;
                                    }

                                }
                            }
                        });
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
