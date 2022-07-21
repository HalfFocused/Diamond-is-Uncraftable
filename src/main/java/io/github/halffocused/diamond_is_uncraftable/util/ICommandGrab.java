package io.github.halffocused.diamond_is_uncraftable.util;

import net.minecraft.entity.LivingEntity;

/**
 * Applied to an {@link io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity}, indicates that it uses a 'command grab' of some kind (King Crimson execute, Purple Haze throw, etc).
 */
public interface ICommandGrab {
    void onCommandGrabEnd(LivingEntity entity);

    void whileCommandGrabbing(LivingEntity entity);

    double commandGrabDistance();

    int commandGrabDuration();

    String holdingAnimation();
}
