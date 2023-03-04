package io.github.halffocused.diamond_is_uncraftable.util;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.*;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.frame.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AttackFramedata {

    /**
     * I will use this class to store the ticks and attack damage of animated moves. It uses the AttackFrame class for efficient storage.
     */

    boolean isActive = false;
    int attackDuration = 0;
    Move assignedMove;
    LivingEntity attackedEntity = null;
    private boolean containsMenacingFrame = false;
    private List<AbstractFrame> frameList =new ArrayList<>();
    int ticker = 0;

    public AttackFramedata addDamageFrame(int tickIn, float damageIn, Vector3d motionIn, double hitBoxRange, int pierce){
        DamageFrame newFrame = new DamageFrame(tickIn, damageIn, motionIn, hitBoxRange, pierce, true);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addDamageFrame(int tickIn, float damageIn, Vector3d motionIn, double hitBoxRange, int pierce, boolean blockable){
        DamageFrame newFrame = new DamageFrame(tickIn, damageIn, motionIn, hitBoxRange, pierce, blockable);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addRadialDamageFrame(int tickIn, float damageIn, Vector3d motionIn, double range){
        RadialDamageFrame newFrame = new RadialDamageFrame(tickIn, damageIn, motionIn, range, true);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addRadialDamageFrame(int tickIn, float damageIn, Vector3d motionIn, double range, boolean blockable){
        RadialDamageFrame newFrame = new RadialDamageFrame(tickIn, damageIn, motionIn, range, blockable);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addBombFrame(int tickIn, double hitBoxRange){
        SetBombFrame newFrame = new SetBombFrame(tickIn, hitBoxRange);
        frameList.add(newFrame);

        return this;
    }
    public AttackFramedata addGrabFrame(int tickIn, double hitBoxRange){
        GrabFrame newFrame = new GrabFrame(tickIn, hitBoxRange);
        frameList.add(newFrame);

        return this;
    }
    public AttackFramedata addEffectFrame(int tickIn, EffectInstance effectIn, double hitBoxRange){
        EffectFrame newFrame = new EffectFrame(tickIn, effectIn, hitBoxRange);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addMessageFrame(int tickIn, int message1, Object message2, Object message3){
        StandMessageFrame newFrame = new StandMessageFrame(tickIn, message1, message2, message3);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addMenacingFrame(int tickIn){
        MenacingFrame newFrame = new MenacingFrame(tickIn);
        frameList.add(newFrame);
        containsMenacingFrame = true;

        return this;
    }

    /**
     * Called when an attack is about to begin.
     */

    public void initAttack(){
        ticker = 0;
        setActive(true);
    }

    /**
     * Called every tick where an attack is active
     */

    public void attackTick(AbstractStandEntity standIn){

        for(AbstractFrame frame : frameList){
            if(frame.getTick() == ticker){
                frame.doThing(standIn, assignedMove);
            }
        }

        if(ticker >= attackDuration){
            setActive(false);
        }

        ticker++;

    }

    public void setActive(boolean active){
        isActive = active;
    }

    public boolean isActive(){
        return isActive;
    }

    public AttackFramedata setAttackDuration(int ticks){
        attackDuration = ticks;
        return this;
    }

    public AttackFramedata generateInterval(int intervalStart, int intervalEnd, int intervalSpacing, float damage, Vector3d motionIn, double hitBoxRange, int pierce){

        int intervalTicker = intervalStart;
        while (intervalTicker <= intervalEnd){
            intervalTicker++;
            if(intervalTicker % intervalSpacing == 0){
                addDamageFrame(intervalTicker, damage, motionIn, hitBoxRange, pierce);
            }
        }

        return this;
    }

    public AttackFramedata generateRadialInterval(int intervalStart, int intervalEnd, int intervalSpacing, float damage, Vector3d motionIn, double hitBoxRange){

        int intervalTicker = intervalStart;
        while (intervalTicker <= intervalEnd){
            intervalTicker++;
            if(intervalTicker % intervalSpacing == 0){
                addRadialDamageFrame(intervalTicker, damage, motionIn, hitBoxRange);
            }
        }

        return this;
    }

    public int getAttackDuration(){
        return attackDuration;
    }

    public void assignMove(Move moveIn){
        assignedMove = moveIn;
    }

    public int getTicker(){
        return ticker;
    }

    public boolean hasMenacingFrame(){
        return this.containsMenacingFrame;
    }
}