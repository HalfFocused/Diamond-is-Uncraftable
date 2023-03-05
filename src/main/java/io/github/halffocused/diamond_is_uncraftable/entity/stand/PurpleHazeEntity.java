package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.EffectInit;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Stance;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.WalkingMoveHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;

@SuppressWarnings("ConstantConditions")
public class PurpleHazeEntity extends AbstractStandEntity implements IAnimatable, IWalkingStand, IOnHit, ICommandGrab {

    int viralModifier = 0;

    AttackFramedata normalPunchData = new AttackFramedata(22)
            .addDamageFrame(9, 4, Vector3d.ZERO, 2.0, 3)
            .addDamageFrame(12, 6, Vector3d.ZERO, 2.0, 3);

    AttackFramedata ragePunchData = new AttackFramedata(14)
            .addDamageFrame(9, 10, Vector3d.ZERO, 2.0, 1, false);

    AttackFramedata rageSlam = new AttackFramedata(50)
            .addRadialDamageFrame(31, 16, new Vector3d(0, 0.7, 0), 6, false)
            .addMessageFrame(31, 1, 0,0);

    AttackFramedata barrageData = new AttackFramedata(109)
            .generateInterval(45, 108, 2, 2, Vector3d.ZERO, 3, 4);

    AttackFramedata commandGrabData = new AttackFramedata(80)
            .addGrabFrame(23, 3.0);



    public HoveringMoveHandler getController(){
        return null;
    }

    Stance normalStance = new Stance(1, 0.17, 0.5, 3, 2, 12,"idle", "forward", false)
            .addWalkingMove(1, new Move("Double Punch", normalPunchData, "jab", 1, -3), 1.0, 3, 20);

    Stance enragedStance = new Stance(3, 0.25, 0.5, 10, -1, 15,"rageidle", "ragesprint", true)
            .addWalkingMove(1, new Move("Rage Punch", ragePunchData, "ragepunch", 1, -3, true), 0.7, 3, 15)
            .addWalkingMove(2, new Move("Rage Slam", rageSlam, "rageslam", 2, -3, true), 0.3, 6, 20);

    Stance violentStance = new Stance(2, 0.17, 0.5, 3, 2, 12,"idle", "forward", false)
            .addWalkingMove(1, new Move("Double Punch", normalPunchData, "jab", 1, -3), 0.6, 3, 20)
            .addWalkingMove(2, new Move("Rage Punch", ragePunchData, "ragepunch", 2, -3), 0.4, 3, 20);

    WalkingMoveHandler controller = new WalkingMoveHandler(this, 1)
            .addStance(normalStance)
            .addStance(enragedStance)
            .addStance(violentStance);

    public PurpleHazeEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public void commandGrab() {
        if(controller.getActiveStance().getId() != 3){
            controller.setMoveActive(3);
        }
    }

    @Override
    public void playSpawnSound() {
        world.playSound(null, getMaster().getPosition(), getSpawnSound(), SoundCategory.NEUTRAL, 2, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {

            Stand.getLazyOptional(master).ifPresent(stand ->{

                if(controller.getActiveStanceId() == 3) {
                    if (this.ticksExisted % 40 == 0) {
                        stand.setRage(Math.max(stand.getRage() - 1, 0));
                    }
                }else{
                    if (this.ticksExisted % 25 == 0) {
                        stand.setRage(Math.max(stand.getRage() - 1, 0));
                    }
                }

            });

            if(controller.getActiveStanceId() == 3){
                if(getDistance(master) > 20){
                    this.setPosition(master.getPosX(), master.getPosY(), master.getPosZ());
                }
            }
        }

        Stand.getLazyOptional(master).ifPresent(stand -> {
            stand.setPreventUnsummon2(stand.getRage() >= 25);

            Style warningStyle = Style.EMPTY.setUnderlined(true);
            if((this.ticksExisted + 20) % 20 >= 11){
                warningStyle.setFormatting(TextFormatting.RED);
            }else{
                warningStyle.setFormatting(TextFormatting.GRAY);
            }
            String message = "";
            if(stand.getRage() < 25){
                message = "Purple Haze: Under Control";
                viralModifier = 0;
                controller.setActiveStance(1);
            }else if(stand.getRage() < 50){
                message = "Purple Haze: Mildly Angered";
                viralModifier = 0;
                controller.setActiveStance(1);
            }else if(stand.getRage() < 75){
                message = "Purple Haze: Violent";
                viralModifier = 1;
                controller.setActiveStance(2);
            }else if(stand.getRage() < 100){
                message = "Purple Haze: On The Verge!";
                viralModifier = 1;
                controller.setActiveStance(2);
            }else if(stand.getRage() >= 100){
                message = "Purple Haze: Rampaging!";
                viralModifier = 2;
                controller.setActiveStance(3);
            }

            master.sendStatusMessage(new StringTextComponent(message).setStyle(warningStyle), true);
        });


    }

    @Override
    public void chargeAttack(boolean isCharging, boolean isBarrage) {
        if (getMaster() == null) return;
        controller.tick(isCharging, isBarrage);
    }




    @Override
    public void changeTargetingMode() {
        controller.changeTargetting();
    }

    @Override
    public WalkingMoveHandler getWalkingController() {
        return controller;
    }

    private void addRage(int incrementRage){
        Stand.getLazyOptional(master).ifPresent(props -> {
            int newRage = (Math.max(0, props.getRage() + incrementRage));
            props.setRage(Math.min(200, newRage));
        });
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (master == null || damageSource.getTrueSource() == master || damageSource == DamageSource.CACTUS || damageSource == DamageSource.FALL)
            return false; //Prevents Stands from taking damage they shouldn't, fall damage, cactus damage, etc.
            addRage((int) (damage * 2));

        master.attackEntityFrom(damageSource, damage * 0.25f);

        return false;
    }

    @Override
    public void messageFrame(int message1, Object message2, Object message3){
        if(message1 == 1){
            if(!this.world.isRemote()) {
                Explosion explosion = new Explosion(this.world, master, this.getPosX(), this.getPosY(), this.getPosZ(), 2, true, Explosion.Mode.DESTROY);
                this.spawnExplosionParticle();
                world.playSound(null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                explosion.doExplosionB(true);
                Util.spawnParticle(this, 3, this.getPosX(), this.getPosY(), this.getPosZ(), 16, 2, 16, 25);

            }
        }
    }

    @Override
    public void onHit(LivingEntity entity, float damage) {
        entity.addPotionEffect(new EffectInstance(EffectInit.HAZE.get(), 80, (int) Math.ceil(damage / 8) + viralModifier));
        addRage((int) (damage / 1.2));
    }

    @Override
    public void onCommandGrabEnd(LivingEntity entity){

    }

    @Override
    public void whileCommandGrabbing(LivingEntity entity){
        //Do nothing
    }

    @Override
    public double commandGrabDistance() {
        return 1.75;
    }

    @Override
    public int commandGrabDuration() {
        return 60;
    }

    @Override
    public String holdingAnimation() {
        return "holding";
    }
}
