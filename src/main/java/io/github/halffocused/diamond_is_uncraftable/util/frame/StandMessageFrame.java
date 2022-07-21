package io.github.halffocused.diamond_is_uncraftable.util.frame;

public class StandMessageFrame {

    int tick;
    int message1;
    Object message2;
    Object message3;

    public StandMessageFrame(int tickIn, int message1In, Object message2In, Object message3In) {
        super();
        tick = tickIn;
        message1 = message1In;
        message2 = message2In;
        message3 = message3In;
    }

    public int getTick(){
        return tick;
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

}
