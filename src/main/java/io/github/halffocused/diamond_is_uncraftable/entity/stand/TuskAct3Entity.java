package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class TuskAct3Entity extends AbstractStandEntity {
    private int bulletChargeTicks;
    private int prevBulletChargeTicks;

    public TuskAct3Entity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public void teleport() {
        if (getMaster() == null) return;
        Stand.getLazyOptional(master).ifPresent(props -> {
            if (props.getCooldown() == 0) {
                Vec3d position = master.getLookVec().mul(7, 1, 7).add(master.getPositionVec());
                master.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
                props.setCooldown(80);
            }
        });
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
                props.setAbilityActive(props.getStandOn() && props.getTimeLeft() > 801 && props.getCooldown() == 0 && props.getAbility());
                ability = props.getAbility() && props.getTimeLeft() > 800;
                if (ability)
                    props.setTimeLeft(props.getTimeLeft() - 1);
                if (props.getTimeLeft() == 801)
                    props.setCooldown(200);
                if (props.getAbilityActive()) {
                    props.setNoClip(true);
                    master.setSwimming(true);
                    fallDistance = 0;
                    if (!world.getBlockState(master.getPosition()).isSolid())
                        master.setMotion(0, -0.5, 0);
                    if (master.getPosition().getY() < 1)
                        master.setMotion(0, 0.5, 0);
                } else {
                    if ((world.getBlockState(master.getPosition()).isSolid() && !world.getBlockState(master.getPosition()).isTransparent()) || master.getPosition().getY() < 1) {
                        master.setMotion(0, 2, 0);
                        fallDistance = 0;
                    } else {
                        if (props.getNoClip()) {
                            master.setMotion(0, 0, 0);
                            fallDistance = 0;
                            props.setNoClip(false);
                        }
                    }
                }

                if (props.getStandOn()) {
                    if (props.getAct() == props.getMaxAct() - 2) {
                        remove();
                        TuskAct2Entity tuskAct2Entity = new TuskAct2Entity(EntityInit.TUSK_ACT_2.get(), world);
                        Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                        tuskAct2Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                        tuskAct2Entity.setMaster(master);
                        tuskAct2Entity.setMasterUUID(master.getUniqueID());
                        world.addEntity(tuskAct2Entity);
                    } else if (props.getAct() == props.getMaxAct() - 1) {
                        remove();
                        TuskAct1Entity tuskAct1Entity = new TuskAct1Entity(EntityInit.TUSK_ACT_1.get(), world);
                        Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                        tuskAct1Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                        tuskAct1Entity.setMaster(master);
                        tuskAct1Entity.setMasterUUID(master.getUniqueID());
                        world.addEntity(tuskAct1Entity);
                    }
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

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (getMaster() == null) return;
        Stand.getLazyOptional(master).ifPresent(props -> props.setNoClip(false));
        master.setInvulnerable(false);
    }
}
