package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;

public class StandMessageFrame extends AbstractFrame{

    private int message1;
    private Object message2;
    private Object message3;

    public StandMessageFrame(int tickIn, int message1In, Object message2In, Object message3In) {
        super(tickIn);
        message1 = message1In;
        message2 = message2In;
        message3 = message3In;
    }

    public int getMessage1(){
        return message1;
    }

    public Object getMessage2(){
        return message2;
    }

    public Object getMessage3(){
        return message3;
    }

    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {
        standEntity.messageFrame(getMessage1(), getMessage2(), getMessage3());
    }
}
