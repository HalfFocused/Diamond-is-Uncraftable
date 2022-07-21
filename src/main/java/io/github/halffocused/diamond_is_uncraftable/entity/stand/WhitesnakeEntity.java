package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.WhitesnakePunchEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class WhitesnakeEntity extends AbstractStandEntity {
    public WhitesnakeEntity(EntityType<? extends AbstractStandEntity> type, World world) {
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
                ability = props.getAbility();
                if (props.getStandID() == Util.StandID.MADE_IN_HEAVEN && props.getAct() == 0 && props.getStandOn()) {
                    remove();
                    MadeInHeavenEntity madeInHeaven = new MadeInHeavenEntity(EntityInit.MADE_IN_HEAVEN.get(), world);
                    Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                    madeInHeaven.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                    madeInHeaven.setMaster(master);
                    madeInHeaven.setMasterUUID(master.getUniqueID());
                    world.addEntity(madeInHeaven);
                }
            });
            master.setNoGravity(false);

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        WhitesnakePunchEntity whitesnake1 = new WhitesnakePunchEntity(world, this, master);
                        whitesnake1.randomizePositions();
                        whitesnake1.shoot(master, master.rotationPitch, master.rotationYaw, 1, 0.25f);
                        world.addEntity(whitesnake1);
                        WhitesnakePunchEntity whitesnake2 = new WhitesnakePunchEntity(world, this, master);
                        whitesnake2.randomizePositions();
                        whitesnake2.shoot(master, master.rotationPitch, master.rotationYaw, 1, 0.25f);
                        world.addEntity(whitesnake2);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
