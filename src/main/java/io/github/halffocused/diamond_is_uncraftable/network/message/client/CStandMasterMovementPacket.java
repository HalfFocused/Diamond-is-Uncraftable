package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

/**
 * Controls Stand movement animations with keybinds send from EventHandleKeybinds
 */
@SuppressWarnings("ConstantConditions")
public class CStandMasterMovementPacket implements IMessage<CStandMasterMovementPacket> {
    private Direction direction;


    public CStandMasterMovementPacket(Direction direction) {
        this.direction = direction;
    }



    public CStandMasterMovementPacket() {
    }

    @Override
    public void handle(CStandMasterMovementPacket message, Supplier<Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                assert sender != null;
                World world = sender.world;
                if (world != null)
                    if (!world.isRemote) {
                        world.getServer().getWorld(sender.dimension).getEntities()
                                .filter(entity -> entity instanceof AbstractStandEntity)
                                .filter(entity -> ((AbstractStandEntity) entity).getMaster().equals(sender))
                                .forEach(entity -> {
                                    if(((AbstractStandEntity) entity).getController() != null){
                                        switch (message.direction) {
                                            case BACKWARDS: { //Don't needs forwards because it is detected by sprinting
                                                ((AbstractStandEntity) entity).getController().setMasterInputKeybind(0);
                                                break;
                                            }
                                            case RIGHT: {
                                                ((AbstractStandEntity) entity).getController().setMasterInputKeybind(2);
                                                break;
                                            }
                                            case LEFT: {
                                                ((AbstractStandEntity) entity).getController().setMasterInputKeybind(1);
                                                break;
                                            }
                                            case NOT_MOVING: {
                                                ((AbstractStandEntity) entity).getController().setMasterInputKeybind(3);
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                    }else {
                                        switch (message.direction) {
                                            case BACKWARDS: { //Don't needs forwards because it is detected by sprinting
                                                ((AbstractStandEntity) entity).setMasterKeybindInput(0);
                                                break;
                                            }
                                            case RIGHT: {
                                                ((AbstractStandEntity) entity).setMasterKeybindInput(2);
                                                break;
                                            }
                                            case LEFT: {
                                                ((AbstractStandEntity) entity).setMasterKeybindInput(1);
                                                break;
                                            }
                                            case NOT_MOVING: {
                                                ((AbstractStandEntity) entity).setMasterKeybindInput(3);
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                    }
                                });
                    }
            });
        }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(CStandMasterMovementPacket message, PacketBuffer buffer) {
        buffer.writeEnumValue(message.direction);

    }

    @Override
    public CStandMasterMovementPacket decode(PacketBuffer buffer) {
        return new CStandMasterMovementPacket(
                buffer.readEnumValue(Direction.class)
        );
    }

    public enum Direction {BACKWARDS, RIGHT, LEFT, NOT_MOVING}
}
