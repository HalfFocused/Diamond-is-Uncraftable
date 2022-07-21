package io.github.halffocused.diamond_is_uncraftable.util;

/**
 * Applied to classes being used as moves to provide some standardization and instanceof functionality.
 */
public interface IMove {
    AttackFramedata getFramedata();

    int getId();

}
