package io.github.halffocused.diamond_is_uncraftable.util.frame;

import net.minecraft.util.math.vector.Vector3d;

public class RadialDamageFrame {

    int tick;
    float damage;
    Vector3d motion;
    double hitboxRange;
    boolean blockable;

    public RadialDamageFrame(int tickIn, float damageIn, Vector3d motionIn, double hitboxRangeIn, boolean blockableIn) {
        tick = tickIn;
        damage = damageIn;
        motion = motionIn;
        hitboxRange = hitboxRangeIn;
        blockable = blockableIn;
    }

    public int getTick(){
        return tick;
    }

    public float getDamage(){
        return damage;
    }

    public Vector3d getMotion(){
        return motion;
    }

    public double getHitboxRange(){
        return hitboxRange;
    }

    public boolean getBlockable(){
        return blockable;
    }

}
