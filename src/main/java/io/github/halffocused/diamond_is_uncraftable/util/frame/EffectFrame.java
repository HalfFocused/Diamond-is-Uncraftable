package io.github.halffocused.diamond_is_uncraftable.util.frame;

import net.minecraft.potion.EffectInstance;

public class EffectFrame {

    int tick;
    EffectInstance effect;
    double hitboxRange;
    int duration;
    int amplifier;

    public EffectFrame(int tickIn, EffectInstance effectIn, int durationIn, int amplifierIn, double hitboxRangeIn) {
        tick = tickIn;
        effect = effectIn;
        duration = durationIn;
        amplifier = amplifierIn;
        hitboxRange = hitboxRangeIn;
    }

    public int getTick(){
        return tick;
    }

    public int getDuration(){
        return duration;
    }

    public int getAmplifier(){
        return amplifier;
    }

    public double getHitboxRange(){
        return hitboxRange;
    }

    public EffectInstance getEffectInstance(){
        return effect;
    }

}
