package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;

public class GrabFrame extends AbstractFrame{

    private double hitboxRange;

    public GrabFrame(int tickIn, double hitboxRangeIn) {
        super(tickIn);
        hitboxRange = hitboxRangeIn;
    }

    public double getHitboxRange(){
        return hitboxRange;
    }


    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {

    }
}
