package io.github.halffocused.diamond_is_uncraftable.network.message.client;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AerosmithEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.IMessage;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

/**
 * Controls Aerosmith's actions through keybinds,
 */
@SuppressWarnings("ConstantConditions")
public class CAerosmithMovePacket implements IMessage<CAerosmithMovePacket> {
    private boolean forwards;
    private boolean backwards;
    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;

    private Direction direction;
    private boolean sprint;

    public CAerosmithMovePacket(boolean forward, boolean sprint, boolean backwards, boolean left, boolean right, boolean up, boolean down) {
        this.forwards = forward;
        this.sprint = sprint;
        this.backwards = backwards;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }

    public CAerosmithMovePacket() {
    }

    @Override
    public void handle(CAerosmithMovePacket message, Supplier<Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity sender = ctx.get().getSender();
                assert sender != null;
                World world = sender.world;
                if (world != null)
                    if (!world.isRemote) {
                        world.getServer().getWorld(sender.dimension).getEntities()
                                .filter(entity -> entity instanceof AerosmithEntity)
                                .filter(entity -> ((AerosmithEntity) entity).getMaster().equals(sender))
                                .forEach(entity -> {
                                    Vec3d motion = Util.getEntityForwardsMotion(entity);

                                    double xMotion = 0;
                                    double zMotion = 0;

                                    if(message.forwards){
                                        if (message.sprint) {
                                            //entity.addVelocity(motion.getX(), 0, motion.getZ());
                                            xMotion += motion.getX();
                                            zMotion += motion.getZ();
                                            entity.setSprinting(true);
                                        } else
                                            //entity.addVelocity(motion.getX() * 0.5, entity.getMotion().getY(), motion.getZ() * 0.5);
                                            xMotion += motion.getX() / 2;
                                            zMotion += motion.getZ() / 2;
                                    }
                                    if(message.backwards){
                                        //entity.addVelocity(-motion.getX() * 0.6, entity.getMotion().getY(), -motion.getZ() * 0.6);
                                        xMotion += motion.getX() / -2;
                                        zMotion += motion.getZ() / -2;
                                    }
                                    if(message.right){
                                        //entity.addVelocity(-motion.getZ() * 0.5, entity.getMotion().getY(), motion.getX() * 0.5);
                                        xMotion += motion.getZ() / -2;
                                        zMotion += motion.getX() / 2;
                                    }
                                    if(message.left){
                                        //entity.addVelocity(motion.getZ() * 0.5, entity.getMotion().getY(), -motion.getX() * 0.5);
                                        xMotion += motion.getZ() / 2;
                                        zMotion += motion.getX() / -2;
                                    }

                                    entity.setMotion(xMotion, entity.getMotion().getY(), zMotion);

                                    if(message.up){
                                        entity.addVelocity(0, 0.5, 0);
                                    }
                                    if(message.down){
                                        entity.addVelocity(0, -0.3, 0);
                                    }
                                });
                    }
            });
        }
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void encode(CAerosmithMovePacket message, PacketBuffer buffer) {
        buffer.writeBoolean(message.forwards);
        buffer.writeBoolean(message.sprint);
        buffer.writeBoolean(message.backwards);
        buffer.writeBoolean(message.left);
        buffer.writeBoolean(message.right);
        buffer.writeBoolean(message.up);
        buffer.writeBoolean(message.down);
    }

    @Override
    public CAerosmithMovePacket decode(PacketBuffer buffer) {
        return new CAerosmithMovePacket(
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public enum Direction {FORWARDS, BACKWARDS, RIGHT, LEFT, UP, DOWN}
}
