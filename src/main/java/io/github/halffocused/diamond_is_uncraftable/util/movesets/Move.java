package io.github.halffocused.diamond_is_uncraftable.util.movesets;

import io.github.halffocused.diamond_is_uncraftable.util.AttackFramedata;
import io.github.halffocused.diamond_is_uncraftable.util.MoveEffects;

public class Move {

    AttackFramedata framedata;
    String animation;
    int id;
    double repositionDistance;
    MoveEffects moveEffects;
    String name;
    boolean canDamageMaster = false;

    public Move(String nameIn, AttackFramedata framedataIn, String animationNameIn, int idIn){
        framedata = framedataIn;
        animation = animationNameIn;
        id = idIn;
        repositionDistance = 2;
        framedata.assignMove(this);
        name = nameIn;
    }

    public Move(String nameIn, AttackFramedata framedataIn, String animationNameIn, int idIn, double repositionDistanceIn){
        framedata = framedataIn;
        animation = animationNameIn;
        id = idIn;
        repositionDistance = repositionDistanceIn;
        framedata.assignMove(this);
        name = nameIn;
    }

    public Move(String nameIn, AttackFramedata framedataIn, String animationNameIn, int idIn, double repositionDistanceIn, MoveEffects moveEffectsIn){
        framedata = framedataIn;
        animation = animationNameIn;
        id = idIn;
        repositionDistance = repositionDistanceIn;
        moveEffects = moveEffectsIn;
        framedata.assignMove(this);
        name = nameIn;
    }

    public Move(String nameIn, AttackFramedata framedataIn, String animationNameIn, int idIn, double repositionDistanceIn, MoveEffects moveEffectsIn, boolean canDamageMasterIn){
        framedata = framedataIn;
        animation = animationNameIn;
        id = idIn;
        repositionDistance = repositionDistanceIn;
        moveEffects = moveEffectsIn;
        framedata.assignMove(this);
        name = nameIn;
        canDamageMaster = canDamageMasterIn;
    }

    public AttackFramedata getFramedata(){
        return framedata;
    }

    public String getAnimation(){
        return animation;
    }

    public int getId(){
        return id;
    }

    public double getRepositionDistance(){
        return repositionDistance;
    }

    public MoveEffects getMoveEffects(){
        return moveEffects;
    }

    public String getName(){return name;}

    public boolean getCanDamageMaster(){return canDamageMaster;}

    /**
     * Returns wheter or not the move has any menacing frames in it. Menacing frames spawn menacing particles when played.
     * Used to render the move's name purple in the Action HUD
     * @return whether or not this move's AttackFramedata contains at least one menacing frame.
     */
    public boolean isMenacing(){
        return this.framedata.hasMenacingFrame();
    }
}
