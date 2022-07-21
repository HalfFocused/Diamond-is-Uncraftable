package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.GreenDayPunchEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class GreenDayEntity extends AbstractStandEntity {
    public GreenDayEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> ability = props.getAbility());

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (ability && !world.isRemote)
                getServer().getWorld(dimension).getEntities()
                        .filter(entity -> !entity.equals(master) && !entity.equals(this))
                        .filter(entity -> entity instanceof LivingEntity)
                        .filter(entity -> entity.getDistance(this) < master.getHealth()) //I think a variable range seems really cool, maybe bows are effective?
                        .filter(entity -> !entity.areEyesInFluid(FluidTags.WATER)) //If you're in lava, you're even more fucked.
                        .filter(entity -> entity.getMotion().getY() < Util.ENTITY_DEFAULT_Y_MOTION) //-0.0784000015258789 is the actual default Y value, very realistic.
                        .forEach(entity -> ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.WITHER, 40, 2, false, false))); //I think this pretty similar to it's ability in the show.

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        GreenDayPunchEntity greenDay1 = new GreenDayPunchEntity(world, this, master);
                        greenDay1.randomizePositions();
                        greenDay1.shoot(master, master.rotationPitch, master.rotationYaw, 1, 0.25f);
                        world.addEntity(greenDay1);
                        GreenDayPunchEntity greenDay2 = new GreenDayPunchEntity(world, this, master);
                        greenDay2.randomizePositions();
                        greenDay2.shoot(master, master.rotationPitch, master.rotationYaw, 1, 0.25f);
                        world.addEntity(greenDay2);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
