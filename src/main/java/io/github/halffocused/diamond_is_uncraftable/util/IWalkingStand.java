package io.github.halffocused.diamond_is_uncraftable.util;

import io.github.halffocused.diamond_is_uncraftable.util.movesets.WalkingMoveHandler;

/**
 * Applied to an {@link io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity}, indicates that it is a walking stand.
 */
public interface IWalkingStand {

    void changeTargetingMode();

    WalkingMoveHandler getWalkingController();

}
