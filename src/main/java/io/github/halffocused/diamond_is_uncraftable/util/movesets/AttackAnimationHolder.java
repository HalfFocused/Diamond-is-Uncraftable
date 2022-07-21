package io.github.halffocused.diamond_is_uncraftable.util.movesets;

public class AttackAnimationHolder {
    public String attackAnimation;
    public String barrageAnimation;

    public AttackAnimationHolder create(String attackAnimationIn, String barrageAnimationIn){
        attackAnimation = attackAnimationIn;
        barrageAnimation = barrageAnimationIn;

         return this;
    }
}
