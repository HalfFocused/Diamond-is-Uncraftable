package io.github.halffocused.diamond_is_uncraftable.util;

import net.minecraft.entity.Entity;

public interface IOnMasterAttacked {
    void onMasterAttacked(Entity damager, float damage);

    //boolean shouldCancelNextDamage();

    //void setCancelNextDamage(boolean shouldCancelNextDamage);
}
