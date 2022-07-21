package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.CMoonPunchEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class CMoonEntity extends AbstractStandEntity {
    public CMoonEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }
    ArrayList<Entity> antiGravityEntities = new ArrayList<>();

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();
                if (props.getAct() == props.getMaxAct() - 1) {
                    remove();
                    WhitesnakeEntity whitesnake = new WhitesnakeEntity(EntityInit.WHITESNAKE.get(), world);
                    Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                    whitesnake.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                    whitesnake.setMaster(master);
                    whitesnake.setMasterUUID(master.getUniqueID());
                    world.addEntity(whitesnake);
                }
            });

            if (ability) {
                if(!world.isRemote) {
                    getServer().getWorld(dimension).getEntities()
                            .filter(entity -> !entity.equals(master) && !entity.equals(this))
                            .filter(entity -> entity instanceof Entity)
                            .filter(entity -> entity.getDistance(this) < 100)
                            .forEach(entity -> {
                                if(!antiGravityEntities.contains(entity) && !entity.hasNoGravity()){
                                    entity.setNoGravity(true);
                                    antiGravityEntities.add(entity);
                                }
                                Vec3d motionVector = entity.getPositionVector().subtract(master.getPositionVec()).normalize().scale(0.5);
                                //The closest actually scaled value here is 0.98, which is an entity's falling speed increase per tick. However, 0.5 has a much nicer curve of slow to fast which I prefer
                                entity.addVelocity(motionVector.x, motionVector.y, motionVector.z);
                            });
                }
            }else{
                if(antiGravityEntities.size() > 0){
                    for(Entity entity : antiGravityEntities){
                        entity.setNoGravity(false);
                    }
                }
            }

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
                        CMoonPunchEntity cMoon1 = new CMoonPunchEntity(world, this, master);
                        cMoon1.randomizePositions();
                        cMoon1.shoot(master, master.rotationPitch, master.rotationYaw, 2.15f, 0.2F);
                        world.addEntity(cMoon1);
                        CMoonPunchEntity cMoon2 = new CMoonPunchEntity(world, this, master);
                        cMoon2.randomizePositions();
                        cMoon2.shoot(master, master.rotationPitch, master.rotationYaw, 2.15f, 0.2F);
                        world.addEntity(cMoon2);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (getMaster() == null) return;
        if (master.isPotionActive(Effects.LEVITATION))
            master.removePotionEffect(Effects.LEVITATION);
        master.setNoGravity(false);
    }
}
