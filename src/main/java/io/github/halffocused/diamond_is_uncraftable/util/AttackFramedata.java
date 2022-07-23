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
import net.minecraft.util.math.Vec3d;
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
    private List<Object> frameList =new ArrayList<>();
    int ticker = 0;

    public AttackFramedata addDamageFrame(int tickIn, float damageIn, Vec3d motionIn, double hitBoxRange, int pierce){
        DamageFrame newFrame = new DamageFrame(tickIn, damageIn, motionIn, hitBoxRange, pierce, true);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addDamageFrame(int tickIn, float damageIn, Vec3d motionIn, double hitBoxRange, int pierce, boolean blockable){
        DamageFrame newFrame = new DamageFrame(tickIn, damageIn, motionIn, hitBoxRange, pierce, blockable);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addRadialDamageFrame(int tickIn, float damageIn, Vec3d motionIn, double range){
        RadialDamageFrame newFrame = new RadialDamageFrame(tickIn, damageIn, motionIn, range, true);
        frameList.add(newFrame);

        return this;
    }

    public AttackFramedata addRadialDamageFrame(int tickIn, float damageIn, Vec3d motionIn, double range, boolean blockable){
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
    public AttackFramedata addEffectFrame(int tickIn, EffectInstance effectIn, int durationIn, int amplifierIn, double hitBoxRange){
        EffectFrame newFrame = new EffectFrame(tickIn, effectIn, durationIn, amplifierIn, hitBoxRange);
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


        for (Object frame : frameList) {
            if(frame instanceof DamageFrame){
                if (((DamageFrame) frame).getTick() == ticker){
                    dealAttack((DamageFrame) frame, standIn);
                }
            }

            if(frame instanceof SetBombFrame){
                if (((SetBombFrame) frame).getTick() == ticker){
                    setBomb((SetBombFrame) frame, standIn);
                }
            }

            if(frame instanceof EffectFrame){
                if (((EffectFrame) frame).getTick() == ticker){
                    dealEffect((EffectFrame) frame, standIn);
                }
            }

            if(frame instanceof RadialDamageFrame){
                if (((RadialDamageFrame) frame).getTick() == ticker){
                    dealRadialAttack((RadialDamageFrame) frame, standIn);
                }
            }

            if(frame instanceof StandMessageFrame){
                if (((StandMessageFrame) frame).getTick() == ticker){
                    standIn.messageFrame(((StandMessageFrame) frame).getMessage1(), ((StandMessageFrame) frame).getMessage2(), ((StandMessageFrame) frame).getMessage3());
                }
            }

            if(frame instanceof GrabFrame){
                if(((GrabFrame) frame).getTick() == ticker){
                    if(standIn instanceof IWalkingStand){
                        World world = standIn.getServer().getWorld(standIn.dimension);
                        AxisAlignedBB hitbox = Util.getAttackHitbox(standIn, ((GrabFrame) frame).getHitboxRange());
                        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standIn, hitbox);
                        LivingEntity targetEntity = null;
                        for (Entity entity : listOfEntities) {
                            if(entity instanceof LivingEntity){
                                if(entity != standIn.getMaster()){
                                    targetEntity = (LivingEntity) entity;
                                }
                            }
                        }
                        if(targetEntity != null){
                            ((IWalkingStand) standIn).getWalkingController().startCommandGrab(targetEntity);
                        }
                    }
                }
            }

            if(frame instanceof MenacingFrame){
                if (((MenacingFrame) frame).getTick() == ticker){

                    if(!standIn.getMaster().world.isRemote()){
                        ServerPlayerEntity playerEntity = (ServerPlayerEntity) standIn.getMaster();
                        Util.giveAdvancement(playerEntity, "menacing");
                    }

                    Util.spawnParticle(standIn, 1, standIn.getPosX(), standIn.getPosY() + 1.5, standIn.getPosZ(), 2, 2, 2, 4);

                    Util.spawnParticle(standIn, 1, standIn.getMaster().getPosX(), standIn.getMaster().getPosY() + 1.5, standIn.getMaster().getPosZ(), 2, 2, 2, 4);
                }
            }
        }

        if(ticker >= attackDuration){
            setActive(false);
        }

        ticker++;

    }

    private void dealAttack(DamageFrame damageFrame, AbstractStandEntity standEntityIn) {

        World world = standEntityIn.getServer().getWorld(standEntityIn.dimension);

        AxisAlignedBB hitbox = Util.getAttackHitbox(standEntityIn, damageFrame.getHitboxRange());

        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standEntityIn, hitbox);

        Random random = standEntityIn.getRNG();
        boolean soundFlag = false;
        int entitiesHit = 0;

        for (Entity entity : listOfEntities) {
            if (entity instanceof LivingEntity) {
                if (entity != standEntityIn && entitiesHit < damageFrame.getPierce()) {
                    if (!(entity.equals(standEntityIn.getMaster())) || (entity.equals(standEntityIn.getMaster()) && assignedMove.getCanDamageMaster())) {

                        Util.dealStandDamage(standEntityIn, (LivingEntity) entity, damageFrame.getDamage(), damageFrame.getMotion(), damageFrame.getBlockable(), standEntityIn.getController().getActiveMove().getMoveEffects());

                        entitiesHit++;
                    }
                }
            }
        }

        if(entitiesHit > 0){
            world.playSound(null, standEntityIn.getPosition(), Util.getHitSound(standEntityIn), SoundCategory.NEUTRAL, 0.5F, 0.6f / (random.nextFloat() * 0.3f + 1) * 2);
        }else{
            world.playSound(null, standEntityIn.getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 0.25F, 0.6f / (random.nextFloat() * 0.3f + 1) * 2);
            standEntityIn.setMostRecentlyDamagedEntity(null);
        }
    }

    private void dealRadialAttack(RadialDamageFrame damageFrame, AbstractStandEntity standEntityIn) {

        standEntityIn.getServer().getWorld(standEntityIn.dimension).getEntities()
                .filter(entity -> !entity.equals(standEntityIn))
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> entity.getDistance(standEntityIn) < damageFrame.getHitboxRange())
                .filter(standEntityIn::canEntityBeSeen)
                .forEach(entity -> radialDamage((LivingEntity) entity, damageFrame, standEntityIn));

        Util.spawnParticle(standEntityIn, 2, standEntityIn.getPosX(), standEntityIn.getEyeHeight() + standEntityIn.getPosY(), standEntityIn.getPosZ(), damageFrame.getHitboxRange() * 2, 1.5, damageFrame.getHitboxRange() * 2, 2);

    }

    private void setBomb(SetBombFrame bombFrame, AbstractStandEntity standEntityIn){
        World world = standEntityIn.getServer().getWorld(standEntityIn.dimension);

        AxisAlignedBB hitbox = Util.getAttackHitbox(standEntityIn, bombFrame.getHitboxRange(), 0.5);

        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standEntityIn, hitbox);

        boolean blockedFlag = false;

        for(Entity entity : listOfEntities){
            if(entity instanceof LivingEntity && !(entity instanceof AbstractStandEntity) && !(entity instanceof EnderDragonEntity)){
                if(entity != standEntityIn.getMaster() && standEntityIn instanceof KillerQueenEntity){ //There should be no way this frame gets called without the stand being Killer Queen, but I would find a way
                    LivingEntity bombTarget = (LivingEntity) entity;
                    if (entity instanceof PlayerEntity) {
                        if (bombTarget.isActiveItemStackBlocking()) {
                            entity.getHeldEquipment().forEach(itemStack -> {
                                if (itemStack.getItem().equals(Items.SHIELD)) {
                                    itemStack.damageItem(50, ((PlayerEntity) entity), (playerEntity) -> {
                                        playerEntity.sendBreakAnimation(Hand.MAIN_HAND);
                                        playerEntity.sendBreakAnimation(Hand.OFF_HAND);
                                    });
                                }
                            });
                            blockedFlag = true;
                        }
                    }

                    Util.spawnParticle(standEntityIn, 14, entity.getPosX(), entity.getPosY() + 1, entity.getPosZ(), 1, 1, 1, 20);

                    ((KillerQueenEntity) standEntityIn).removeFirstBombFromAll();

                    Stand stand = Stand.getCapabilityFromPlayer(standEntityIn.getMaster());

                    if(bombTarget.getHealth() / bombTarget.getMaxHealth() >= 0.16 && bombTarget.getHealth() >= 3) {
                        ((KillerQueenEntity) standEntityIn).bombEntity = bombTarget;
                        stand.setBombEntityId(bombTarget.getEntityId());
                    }else if (!blockedFlag){
                        ((KillerQueenEntity) standEntityIn).bombEntity = bombTarget;
                        stand.setBombEntityId(bombTarget.getEntityId());
                        standEntityIn.getController().setMoveActive(8);
                    }else{
                        ((KillerQueenEntity) standEntityIn).removeFirstBombFromAll();
                        if(bombTarget.isActiveItemStackBlocking()){
                            ((LivingEntity) entity).getActiveItemStack().getOrCreateTag().putBoolean("bomb", true);
                            ((LivingEntity) entity).getActiveItemStack().getOrCreateTag().putUniqueId("ownerUUID", standEntityIn.getMaster().getUniqueID());
                            ((LivingEntity) entity).getHeldItemMainhand().setDisplayName(new StringTextComponent("Bomb"));
                        }
                    }

                }
            }
        }

    }

    private void dealEffect(EffectFrame effectFrame, AbstractStandEntity standEntityIn) {
        World world = standEntityIn.getServer().getWorld(standEntityIn.dimension);

        AxisAlignedBB hitbox = Util.getAttackHitbox(standEntityIn, effectFrame.getHitboxRange());

        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standEntityIn, hitbox);

        for (Entity parseList : listOfEntities) {
            if (parseList instanceof LivingEntity) {
                ((LivingEntity) parseList).addPotionEffect(effectFrame.getEffectInstance());
            }
        }
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

    public AttackFramedata generateInterval(int intervalStart, int intervalEnd, int intervalSpacing, float damage, Vec3d motionIn, double hitBoxRange, int pierce){

        int intervalTicker = intervalStart;
        while (intervalTicker <= intervalEnd){
            intervalTicker++;
            if(intervalTicker % intervalSpacing == 0){
                addDamageFrame(intervalTicker, damage, motionIn, hitBoxRange, pierce);
            }
        }

        return this;
    }

    public AttackFramedata generateRadialInterval(int intervalStart, int intervalEnd, int intervalSpacing, float damage, Vec3d motionIn, double hitBoxRange){

        int intervalTicker = intervalStart;
        while (intervalTicker <= intervalEnd){
            intervalTicker++;
            if(intervalTicker % intervalSpacing == 0){
                addRadialDamageFrame(intervalTicker, damage, motionIn, hitBoxRange);
            }
        }

        return this;
    }


    private void radialDamage(LivingEntity entity, RadialDamageFrame frame, AbstractStandEntity standIn){
        Util.spawnParticle(standIn, 2, entity.getPosX(), entity.getEyeHeight() + entity.getPosY(), entity.getPosZ(), 1.2, 1.6, 1.2, 2);

        int entitiesHit = 0;
        Random random = entity.getRNG();

        World world = standIn.getServer().getWorld(standIn.dimension);

        if (entity != standIn) {
            if (!(entity.equals(standIn.getMaster())) || (entity.equals(standIn.getMaster()) && assignedMove.getCanDamageMaster())) {
                    Util.dealStandDamage(standIn, entity, frame.getDamage(), frame.getMotion(), frame.getBlockable(), standIn.getController().getActiveMove().getMoveEffects());
                    entitiesHit++;
            }
        }

        if (assignedMove != null) { // This should literally never happen but it's always good to check.
            if (assignedMove.getMoveEffects() != null) {
                if (!(entity.equals(standIn.getMaster())) || (entity.equals(standIn.getMaster()) && assignedMove.getCanDamageMaster())) {
                    Util.spawnParticle(standIn, assignedMove.getMoveEffects().getParticleId(), entity.getPosX(), entity.getEyeHeight() + entity.getPosY(), entity.getPosZ(), 2.4, 1.4, 2.4, 1);
                    Util.spawnParticle(standIn, 4, entity.getPosX() + (random.nextFloat() - 0.5), entity.getEyeHeight() + entity.getPosY() + (random.nextFloat() - 0.5), entity.getPosZ() + (random.nextFloat() - 0.5), 0.7, 0.9, 0.7, (int) (frame.getDamage() * 8.5));
                }
            }
        }

        if(entitiesHit > 0){
            world.playSound(null, standIn.getPosition(), Util.getHitSound(standIn), SoundCategory.NEUTRAL, 0.5F, 0.6f / (random.nextFloat() * 0.6f + 1) * 2);
        }else{
            world.playSound(null, standIn.getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 0.25F, 0.6f / (random.nextFloat() * 0.3f + 1) * 2);
            standIn.setMostRecentlyDamagedEntity(null);
        }
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