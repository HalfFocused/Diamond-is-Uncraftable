package io.github.halffocused.diamond_is_uncraftable.util;

import net.minecraft.util.text.StringTextComponent;

public interface IOverrideChargeAttack {
    void chargeAttackRelease(int ticksCharged);

    StringTextComponent generateHudText(int ticksCharged);

    boolean shouldAllowChargeAttack();
}
