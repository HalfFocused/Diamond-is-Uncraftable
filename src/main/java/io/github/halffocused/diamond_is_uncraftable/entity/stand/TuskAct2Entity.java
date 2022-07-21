package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class TuskAct2Entity extends AbstractStandEntity {
    private int bulletChargeTicks;
    private int prevBulletChargeTicks;

    public TuskAct2Entity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    @Override
    public void chargeAttack(boolean isCharging, boolean isBarrage) {
        if (getMaster() == null) return;

    }


    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();

                if (props.getAct() == props.getMaxAct() - 1 && props.getStandOn()) {
                    remove();
                    TuskAct1Entity tuskAct1Entity = new TuskAct1Entity(EntityInit.TUSK_ACT_1.get(), world);
                    Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                    tuskAct1Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                    tuskAct1Entity.setMaster(master);
                    tuskAct1Entity.setMasterUUID(master.getUniqueID());
                    world.addEntity(tuskAct1Entity);
                }


                if (props.getAbilityUseCount() == 10 && props.getCooldown() == 0)
                    props.setCooldown(300);

                if (props.getCooldown() == 1)
                    props.setAbilityUseCount(0);
            });

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
        }
    }
}
