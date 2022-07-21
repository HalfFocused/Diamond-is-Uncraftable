package io.github.halffocused.diamond_is_uncraftable.util.frame;

public class GrabFrame {

    int tick;
    double hitboxRange;

    public GrabFrame(int tickIn, double hitboxRangeIn) {
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
