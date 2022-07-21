package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.ChargeAttackFormat;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class TuskAct1Entity extends AbstractStandEntity {
    private int bulletChargeTicks;
    private int prevBulletChargeTicks;

    public TuskAct1Entity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    ChargeAttackFormat nailChargeAttack = new ChargeAttackFormat("null")
            .addChargeNode(200, -1, false);

    HoveringMoveHandler controller = new HoveringMoveHandler(this)
            .addChargeAttack(nailChargeAttack);

    public HoveringMoveHandler getController(){
        return controller;
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();

                if (props.getAct() == 0 && props.getStandOn()) {
                    switch (props.getStandID()) {
                        case Util.StandID.TUSK_ACT_2: {
                            remove();
                            TuskAct2Entity tuskAct2Entity = new TuskAct2Entity(EntityInit.TUSK_ACT_2.get(), world);
                            Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                            tuskAct2Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                            tuskAct2Entity.setMaster(master);
                            tuskAct2Entity.setMasterUUID(master.getUniqueID());
                            world.addEntity(tuskAct2Entity);
                            break;
                        }
                        case Util.StandID.TUSK_ACT_3: {
                            remove();
                            TuskAct3Entity tuskAct3Entity = new TuskAct3Entity(EntityInit.TUSK_ACT_3.get(), world);
                            Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                            tuskAct3Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                            tuskAct3Entity.setMaster(master);
                            tuskAct3Entity.setMasterUUID(master.getUniqueID());
                            world.addEntity(tuskAct3Entity);
                            break;
                        }
                        case Util.StandID.TUSK_ACT_4: {
                            remove();
                            TuskAct4Entity tuskAct4Entity = new TuskAct4Entity(EntityInit.TUSK_ACT_4.get(), world);
                            Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                            tuskAct4Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                            tuskAct4Entity.setMaster(master);
                            tuskAct4Entity.setMasterUUID(master.getUniqueID());
                            world.addEntity(tuskAct4Entity);
                            break;
                        }
                        default:
                            break;
                    }
                }

                if (props.getAbilityUseCount() == 10 && props.getCooldown() == 0)
                    props.setCooldown(100);

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
