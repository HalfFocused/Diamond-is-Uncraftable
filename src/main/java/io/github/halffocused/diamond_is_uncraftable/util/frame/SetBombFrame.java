package io.github.halffocused.diamond_is_uncraftable.util.frame;

public class SetBombFrame {

    int tick;
    double hitboxRange;

    public SetBombFrame(int tickIn, double hitboxRangeIn) {
        tick = tickIn;
        hitboxRange = hitboxRangeIn;
    }

    public int getTick(){
        return tick;
    }


    public double getHitboxRange(){
        return hitboxRange;
    }

}
