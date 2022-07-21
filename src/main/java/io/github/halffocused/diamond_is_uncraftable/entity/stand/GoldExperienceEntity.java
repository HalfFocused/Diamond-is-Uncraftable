package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.GoldExperiencePunchEntity;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

@SuppressWarnings({"ConstantConditions", "unused"})
public class GoldExperienceEntity extends AbstractStandEntity {
    private boolean transforming;
    private int transformTick;

    public GoldExperienceEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    //4 methods below are currently redundant, will be used in the future.
    public boolean isTransforming() {
        return transforming;
    }

    public void setTransforming(boolean transforming) {
        this.transforming = transforming;
    }

    public int getTransformTick() {
        return transformTick;
    }

    public void setTransformTick(int transformTick) {
        this.transformTick = transformTick;
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
                if (props.getTransformed() > 0)
                    props.setCooldown(props.getCooldown() - 1);
                if (props.getCooldown() <= 0) {
                    props.setTransformed(0);
                    props.setCooldown(80);
                }
            });
            master.addPotionEffect(new EffectInstance(Effects.REGENERATION, 40, 2));

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
                        GoldExperiencePunchEntity goldExperience1 = new GoldExperiencePunchEntity(world, this, master);
                        goldExperience1.randomizePositions();
                        goldExperience1.shoot(master, master.rotationPitch, master.rotationYaw, 2.0F, 0.2F);
                        world.addEntity(goldExperience1);
                        GoldExperiencePunchEntity goldExperience2 = new GoldExperiencePunchEntity(world, this, master);
                        goldExperience2.randomizePositions();
                        goldExperience2.shoot(master, master.rotationPitch, master.rotationYaw, 2.0F, 0.2F);
                        world.addEntity(goldExperience2);
                    }
                if (attackTicker >= 110) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
