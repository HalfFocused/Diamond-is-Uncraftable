package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;

public abstract class AbstractFrame {

    private int tick;

    public AbstractFrame(int tickIn){
        tick = tickIn;
    }

    public int getTick(){
        return tick;
    }

    public abstract void doThing(AbstractStandEntity standEntity, Move assignedMove);
}
