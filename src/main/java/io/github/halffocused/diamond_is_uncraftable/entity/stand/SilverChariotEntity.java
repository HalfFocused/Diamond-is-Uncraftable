package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.SilverChariotSwordEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EffectInit;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncSilverChariotArmorPacket;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.MovementAnimationHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

@SuppressWarnings({"ConstantConditions", "unused"})
public class SilverChariotEntity extends AbstractStandEntity implements IAnimatable, IMomentum, IOnHit, IOnMasterAttacked {
    private boolean hasArmor = false;

    int swingChargeTicks;
    int prevSwingChargeTicks;
    boolean isBarrageAttack;
    int outgoingChargeAttackTicks;
    boolean isOutgoingAttackBarrage;
    boolean hasMoveHitOnce = false;

    int noSwordTicks = 0;

    int swordChargeTicker = 0;

    boolean initialArmorSet = false;

    final int ARMOR_OFF_FRAME = 40;

    AttackFramedata jabData = new AttackFramedata()
            .addDamageFrame(6, 1, Vector3d.ZERO, 2.6, 3)
            .addDamageFrame(8, 1, Vector3d.ZERO, 2.6, 3)
            .addDamageFrame(10, 1, Vector3d.ZERO, 2.6, 3)
            .addDamageFrame(12, 1, Vector3d.ZERO, 2.6, 3)
            .addDamageFrame(14, 2, Vector3d.ZERO, 2.6, 3)
            .setAttackDuration(22);

    AttackFramedata fastJabData = new AttackFramedata()
            .addDamageFrame(6, 1, Vector3d.ZERO, 2.6, 4)
            .addDamageFrame(7, 1, Vector3d.ZERO, 2.6, 4)
            .addDamageFrame(8, 1, Vector3d.ZERO, 2.6, 4)
            .addDamageFrame(9, 1, Vector3d.ZERO, 2.6, 4)
            .addDamageFrame(10, 2, Vector3d.ZERO, 2.6, 4)
            .addDamageFrame(12, 2, Vector3d.ZERO, 2.6, 4)
            .addDamageFrame(14, 2, new Vector3d(0, 0.45, 0), 2.6, 4)
            .setAttackDuration(16);

    AttackFramedata barrageData = new AttackFramedata()
            .addDamageFrame(4, 2, Vector3d.ZERO, 2.2, 2)
            .addDamageFrame(9, 2, Vector3d.ZERO, 2.4, 2)
            .generateInterval(16, 38, 2, 1, Vector3d.ZERO, 2.6, 3)
            .setAttackDuration(47);

    AttackFramedata spinAttack = new AttackFramedata()
            .addRadialDamageFrame(1, 2, new Vector3d(0, 0.4, 0), 4)
            .generateRadialInterval(0, 20, 2, 1, Vector3d.ZERO, 5)
            .setAttackDuration(20);

    AttackFramedata countering = new AttackFramedata().setAttackDuration(15);

    AttackFramedata counterSlash = new AttackFramedata()
            .addRadialDamageFrame(1, 2, new Vector3d(0, 0.4, 0), 5)
            .addMenacingFrame(1)
            .addMessageFrame(1, 2, null, null)
            .generateRadialInterval(18, 30, 2, 4, Vector3d.ZERO, 5)
            .setAttackDuration(60);

    AttackFramedata shedArmor = new AttackFramedata()
            .addRadialDamageFrame(ARMOR_OFF_FRAME, 3, Vector3d.ZERO, 8, false)
            .addMessageFrame(54, 3, null, null)
            .setAttackDuration(55);

    AttackFramedata armorOn = new AttackFramedata()
            .setAttackDuration(45);

    AttackFramedata aimSword = new AttackFramedata()
            .addMessageFrame(1,4, null, null)
            .addMessageFrame(10,4, null, null)
            .addMessageFrame(20,4, null, null)
            .addMessageFrame(30, 0, null, null)
            .setAttackDuration(30);

    AttackFramedata fireSword = new AttackFramedata()
            .addMessageFrame(3, 1, null, null)
            .setAttackDuration(4);

    HoveringMoveHandler controller = new HoveringMoveHandler(this)
            .addMove("Rapid Swings", 1, jabData, "jabs", 2.0)
            .addMove("Light-Speed Swings",2, fastJabData, "offjab", 2.0)
            .addMove("Sword Barrage",3, barrageData, "horahora", 2.0)
            .addMove("Spin Attack",4, spinAttack, "spintowin", -1)
            .addMove("Counter",5, countering, "parry", -1)
            .addMove("Counter-Attack",6, counterSlash, "counter", -3)
            .addMove("Shed Armor",7, shedArmor, "armoroff", -3)
            .addMove("Equip Armor",8, armorOn, "armoron", -3)
            .addMove("Aim Sword",9, aimSword, "aim", -2)
            .addMove("Fire Sword", 10, fireSword, "shoot", -2);



    public SilverChariotEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public boolean hasArmor() {
        return hasArmor;
    }

    public void ability(){

        if(hasArmor()){
            if(spendEnergy(0)) {
                controller.setMoveActive(9);
            }
        }else{
            Stand.getLazyOptional(master).ifPresent(props -> {
                if (props.getCooldown() <= 0 && master.hurtResistantTime == 0 && spendEnergy(40)) {
                    if (controller.isActionable()) {
                        controller.setMoveActive(5);
                    }
                }
            });
        }

    }

    /**
     * Sets whether Silver Chariot has his armor on and passes the information to the client.
     *
     * @param hasArmor Simply says if Silver Chariot should have his armor or not.
     */
    public void setHasArmor(boolean hasArmor) {
        if(!initialArmorSet){
            this.hasArmor = hasArmor;
            if (!world.isRemote) //Packet is necessary because hasArmor can change after the entity has spawned, after IEntityAdditionalSpawnData#writeSpawnData has already fired.
                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SSyncSilverChariotArmorPacket(getEntityId(), hasArmor()));
                initialArmorSet = true;
        }else {

            if (!hasArmor()) {
                if (this.ticksExisted < 10) {
                    if (!world.isRemote) //Packet is necessary because hasArmor can change after the entity has spawned, after IEntityAdditionalSpawnData#writeSpawnData has already fired.
                        DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SSyncSilverChariotArmorPacket(getEntityId(), hasArmor()));
                } else {
                    if (controller.isActionable()) {
                        controller.setMoveActive(8);
                        this.hasArmor = hasArmor;
                        Stand.getLazyOptional(master).ifPresent(props -> {
                            props.setAbility(false);
                        });
                        if (!world.isRemote) //Packet is necessary because hasArmor can change after the entity has spawned, after IEntityAdditionalSpawnData#writeSpawnData has already fired.
                            DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SSyncSilverChariotArmorPacket(getEntityId(), hasArmor()));
                    }
                }
            } else {
                if (controller.isActionable()) {
                    controller.setMoveActive(7);
                    this.hasArmor = hasArmor;
                    Stand.getLazyOptional(master).ifPresent(props -> {
                        props.setAbility(true);
                    });
                    if (!world.isRemote) //Packet is necessary because hasArmor can change after the entity has spawned, after IEntityAdditionalSpawnData#writeSpawnData has already fired.
                        DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SSyncSilverChariotArmorPacket(getEntityId(), hasArmor()));
                }
            }
        }
    }

    public void putHasArmor(boolean hasArmor) {
        this.hasArmor = hasArmor;
    }

    /**
     * Removes the speed {@link net.minecraft.potion.Effect} from the Stand's master when it's unsummoned.
     */
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        Stand.getLazyOptional(master).ifPresent(props -> props.setAbility(false));
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility() && props.getMomentum() > 0;
                    if (ability == hasArmor() && controller.isActionable()) {
                        setHasArmor(!ability);
                        props.setAbilityActive(!hasArmor && props.getMomentum() > 0);
                        if (!hasArmor()) {
                            if (props.getTimeLeft() % 20 == 0 && !master.isCreative())
                                master.getFoodStats().addStats(-1, 0);
                            world.playSound(null, new BlockPos(getPosX(), getPosY(), getPosZ()), SoundEvents.ENTITY_GENERIC_EXPLODE, getSoundCategory(), 2.0f, 1.0f);
                        }
                }
                if(!world.isRemote) {
                    if (!(controller.isMoveActive())) {
                        props.setCounterBuffer(false);
                    }
                    if (!controller.isMoveActive() && hasMoveHitOnce) {
                        hasMoveHitOnce = false;
                    }
                    boolean shouldRemoveStrength = false;
                    for(EffectInstance effect : getMaster().getActivePotionEffects()){
                        if(effect.getPotion().equals(EffectInit.STAND_STRENGTH.get())){
                            if(!spendEnergy((2.5 / 20.0) * (effect.getAmplifier() + 1), true)){
                                shouldRemoveStrength = true;
                            }
                        }
                    }
                    if(shouldRemoveStrength){
                        master.removePotionEffect(EffectInit.STAND_STRENGTH.get());
                        addEnergy(50);
                    }

                }
                if(controller.isMoveActive()){ //Sound effects while SC is removing it's armor
                    if(controller.getActiveMove().getId() == 7 && controller.getActiveMove().getFramedata().getTicker() < ARMOR_OFF_FRAME){

                        SoundEvent soundEvent;

                        if(rand.nextInt(4) == 0){
                            switch (rand.nextInt(3)){
                                case 0: {
                                    soundEvent = SoundInit.ARMOR_OFF_1.get();
                                    break;
                                }
                                case 1: {
                                    soundEvent = SoundInit.ARMOR_OFF_2.get();
                                    break;
                                }
                                default: {
                                    soundEvent = SoundInit.ARMOR_OFF_3.get();
                                    break;
                                }
                            }

                            world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), soundEvent, SoundCategory.HOSTILE, 1.0f, 0.6f / (rand.nextFloat() * 0.3f + 1) * 3.5f);
                        }
                    }

                    if(controller.getActiveMove().getId() == 9 && rand.nextInt(9) == 0){
                        Util.spawnParticle(this, 9, getPosX(), getPosY() + this.getEyeHeight(), getPosZ(), 1.8, 1.8, 1.8, 1);
                    }
                }

            });
        }
    }


    @Override
    public MovementAnimationHolder getMovementAnimations(){
        if(hasArmor()) {
            if(controller.isActionable()) {
                return new MovementAnimationHolder().create("idle", "forward", "left", "right", "backwards");
            }else{
                return new MovementAnimationHolder().create("noswordidel", "noswordforward", "noswordleft", "noswordright", "noswordback");
                //If you're reading this: Tell joejan to spell his damn animation names properly
            }
        }else{
            return new MovementAnimationHolder().create("offidle", "offorward", "offorward", "offorward", "offorward");
        }
    }


    @Override
    public HoveringMoveHandler getController() {
        return controller;
    }

    @Override
    public int getJabMoveId() {
        return hasArmor() ? 1 : 2;
    }

    @Override
    public int getBarrageMoveId() {
        return hasArmor() ? 3 : 4;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 2, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        if(world.isRemote) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(currentAnimation, animationLooping));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void messageFrame(int message1, Object message2, Object message3){
        if(message1 == 0){
            controller.cancelActiveMoves();
            controller.setMoveActive(10);
        }
        if(message1 == 1){

            SilverChariotSwordEntity swordEntity = new SilverChariotSwordEntity(world, this, master);
            swordEntity.setPosition(master.getPosX(), master.getPosY() + master.getEyeHeight(), master.getPosZ());
            swordEntity.shoot(getMaster(), master.rotationPitch, master.rotationYaw, 4f, Float.MIN_VALUE);

            world.addEntity(swordEntity);


            Util.applyUnactionableTicks(master, 120);
        }
        if(message1 == 2){
            Stand.getLazyOptional(master).ifPresent(props -> {
                props.setMomentum(Math.min(100, props.getMomentum() + 20));
            });
        }
        if(message1 == 3){
            addEnergy(100);
        }
    }

    @Override
    public double addMomentumAmount() {
        return hasArmor ? 3 : 0;
    }

    @Override
    public double getMomentumDrainRate() {
        if(controller.isMoveActive() && controller.getActiveMove().getId() == 7){
            return 0;
        }else{
            return hasArmor ? 2.5 : 4;
        }
    }

    @Override
    public void onHit(LivingEntity entity, float damage) {
        if(!world.isRemote()) {
            if (!hasArmor()) {
                addEnergy(1);
            }

            if(!hasMoveHitOnce){
                EffectInstance newEffect = null;
                for(EffectInstance effect : getMaster().getActivePotionEffects()){
                    if(effect.getPotion().equals(EffectInit.STAND_STRENGTH.get())){
                        newEffect = new EffectInstance(EffectInit.STAND_STRENGTH.get(), 100, Math.min(effect.getAmplifier() + 1, 6));
                    }
                }
                if(newEffect == null){
                    newEffect = new EffectInstance(EffectInit.STAND_STRENGTH.get(), 100, 0);
                }
                master.removePotionEffect(EffectInit.STAND_STRENGTH.get());
                master.addPotionEffect(newEffect);
                hasMoveHitOnce = true;
            }
        }
    }

    @Override
    public void onMasterAttacked(Entity attacker, float damage) {
        if (controller.isMoveActive()) {
            if (controller.getActiveMove().getId() == 5) {
                controller.cancelActiveMoves();
                controller.setMoveActive(6);
                Stand.getLazyOptional(master).ifPresent(props -> {
                    props.setCounterBuffer(true);
                });
            }
        }
    }

    @Override
    public float getDamageSharingPercentage(){
        return hasArmor() ? 0.35f : 1.25f;
    }
}
