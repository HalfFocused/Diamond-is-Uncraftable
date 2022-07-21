package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class TwentiethCenturyBoyEntity extends AbstractStandEntity {
    public TwentiethCenturyBoyEntity(EntityType<? extends AbstractStandEntity> type, World world) {
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
                props.setAbilityActive(props.getStandOn() && props.getTimeLeft() > 801 && props.getCooldown() <= 0 && props.getAbility());
                ability = props.getTimeLeft() > 801 && props.getCooldown() <= 0 && props.getAbility();
                if (props.getTimeLeft() > 800)
                    props.setTimeLeft(props.getTimeLeft() - 1);
                if (ability)
                    master.setMotion(0, master.onGround && !world.getBlockState(master.getPosition().down()).isAir(world, master.getPosition().down()) ? 0 : master.getMotion().getY(), 0);
            });

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
        }
    }
}
