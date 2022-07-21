package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class EchoesAct1Entity extends AbstractStandEntity {
    public EchoesAct1Entity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                if (props.getAct() == 0 && props.getStandOn()) {
                    switch (props.getStandID()) {
                        case Util.StandID.ECHOES_ACT_2: {
                            remove();
                            EchoesAct2Entity echoesAct2Entity = new EchoesAct2Entity(EntityInit.ECHOES_ACT_2.get(), world);
                            Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                            echoesAct2Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                            echoesAct2Entity.setMaster(master);
                            echoesAct2Entity.setMasterUUID(master.getUniqueID());
                            world.addEntity(echoesAct2Entity);
                            break;
                        }
                        case Util.StandID.ECHOES_ACT_3: {
                            remove();
                            EchoesAct3Entity echoesAct3Entity = new EchoesAct3Entity(EntityInit.ECHOES_ACT_3.get(), world);
                            Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                            echoesAct3Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                            echoesAct3Entity.setMaster(master);
                            echoesAct3Entity.setMasterUUID(master.getUniqueID());
                            world.addEntity(echoesAct3Entity);
                            break;
                        }
                        default:
                            break;
                    }
                }
            });

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
        }
    }
}
