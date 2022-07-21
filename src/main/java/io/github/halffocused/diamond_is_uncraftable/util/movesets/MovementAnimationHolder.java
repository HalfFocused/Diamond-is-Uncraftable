package io.github.halffocused.diamond_is_uncraftable.util.movesets;

public class MovementAnimationHolder {
    public String idleAnimation;
    public String forwardAnimation;
    public String leftAnimation;
    public String rightAnimation;
    public String backwardsAnimation;

    public MovementAnimationHolder create(String idleAnimationIn, String forwardAnimationIn, String leftAnimationIn, String rightAnimationIn, String backwardAnimationIn){
        idleAnimation = idleAnimationIn;
        forwardAnimation = forwardAnimationIn;
        leftAnimation = leftAnimationIn;
        rightAnimation = rightAnimationIn;
        backwardsAnimation = backwardAnimationIn;

         return this;
    }
}
