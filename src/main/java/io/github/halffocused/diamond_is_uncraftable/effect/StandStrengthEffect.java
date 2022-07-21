package io.github.halffocused.diamond_is_uncraftable.effect;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StandStrengthEffect extends Effect {
    public StandStrengthEffect(EffectType typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
        if(!(entityLivingBaseIn instanceof PlayerEntity)){
            entityLivingBaseIn.removePotionEffect(this);
        }else{
            Stand playerCapability = Stand.getCapabilityFromPlayer((PlayerEntity) entityLivingBaseIn);

            if(playerCapability.getStandID() == 0){
                entityLivingBaseIn.removePotionEffect(this);
            }
        }
    }
}
