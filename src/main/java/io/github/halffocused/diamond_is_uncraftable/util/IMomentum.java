package io.github.halffocused.diamond_is_uncraftable.util;

public interface IMomentum {
    double addMomentumAmount();

    /**
     * Returns the amount of momentum that should be lost every second.
     * @return A double representing the amount of momentum that should be lost every second.
     */
    double getMomentumDrainRate();
}
