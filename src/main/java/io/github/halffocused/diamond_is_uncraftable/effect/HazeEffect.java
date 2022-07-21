package io.github.halffocused.diamond_is_uncraftable.effect;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HazeEffect extends Effect {
    public HazeEffect(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
            if(!entityLivingBaseIn.world.isRemote()) {
                entityLivingBaseIn.attackEntityFrom(DamageSource.WITHER, amplifier);
                entityLivingBaseIn.hurtResistantTime = 0;
            }

    }
}
