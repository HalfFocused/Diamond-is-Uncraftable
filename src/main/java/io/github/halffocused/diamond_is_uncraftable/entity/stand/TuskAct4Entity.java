package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandEffects;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.TuskAct4PunchEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class TuskAct4Entity extends AbstractStandEntity {
    private int bulletChargeTicks;
    private int prevBulletChargeTicks;

    public TuskAct4Entity(EntityType<? extends AbstractStandEntity> type, World world) {
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
            if (master.getRidingEntity() instanceof AbstractHorseEntity)
                StandEffects.getLazyOptional(master).ifPresent(props -> props.setRotating(false));
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();

                if (props.getAct() == props.getMaxAct() - 3 && props.getStandOn()) {
                    remove();
                    TuskAct3Entity tuskAct3Entity = new TuskAct3Entity(EntityInit.TUSK_ACT_3.get(), world);
                    Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                    tuskAct3Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                    tuskAct3Entity.setMaster(master);
                    tuskAct3Entity.setMasterUUID(master.getUniqueID());
                    world.addEntity(tuskAct3Entity);
                }


                if (props.getAbilityUseCount() == 10 && props.getCooldown() == 0)
                    props.setCooldown(600);

                if (props.getCooldown() == 1)
                    props.setAbilityUseCount(0);
            });

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;

            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker > 55)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        TuskAct4PunchEntity tuskAct4PunchEntity = new TuskAct4PunchEntity(world, this, master);
                        tuskAct4PunchEntity.randomizePositions();
                        tuskAct4PunchEntity.shoot(master, master.rotationPitch, master.rotationYaw, 5, 0.001f);
                        world.addEntity(tuskAct4PunchEntity);
                        TuskAct4PunchEntity tuskAct4PunchEntity1 = new TuskAct4PunchEntity(world, this, master);
                        tuskAct4PunchEntity1.randomizePositions();
                        tuskAct4PunchEntity1.shoot(master, master.rotationPitch, master.rotationYaw, 5, 0.001f);
                        world.addEntity(tuskAct4PunchEntity1);
                    }
                if (attackTicker >= 170) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
