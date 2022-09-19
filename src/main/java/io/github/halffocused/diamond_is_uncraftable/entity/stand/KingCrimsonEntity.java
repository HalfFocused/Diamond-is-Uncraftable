package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.EffectInit;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.ChargeAttackFormat;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.MovementAnimationHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.ArrayList;

@SuppressWarnings("ConstantConditions")
public class KingCrimsonEntity extends AbstractStandEntity implements IAnimatable, IOnMasterAttacked, IOnHit {

    ArrayList<EpitaphPairing> pairList = new ArrayList<>();

    int opportunityTicks = 0;
    static boolean activeTimeErase = false;
    int epitaphTicks = -1;
    boolean epitaphFlag = false;
    int epitaphEndingOpportunityTicks = 0;
    public boolean timeEraseActive;
    int timeEraseDuration = 0;
    boolean clipToPosition = false;
    LivingEntity executionTarget = null;

    AttackFramedata mainPunchData = new AttackFramedata()
            .addDamageFrame(12, 10, Vector3d.ZERO, 2, 1)
            .addMessageFrame(15, 1, 9, null)
            .setAttackDuration(25);

    AttackFramedata barrageData = new AttackFramedata()
            .generateInterval(8, 45, 3, 2, Vector3d.ZERO, 2.7, 4)
            .addMessageFrame(43, 1, 9, null)
            .setAttackDuration(53);

    AttackFramedata lightChargeAttack = new AttackFramedata()
            .addDamageFrame(8, 16, Vector3d.ZERO, 2.0, 1)
            .addMessageFrame(12, 1, 9, null)
            .setAttackDuration(22);

    AttackFramedata mediumChargeAttack = new AttackFramedata()
            .addDamageFrame(10, 21, Vector3d.ZERO, 2.0, 1)
            .addMessageFrame(14, 1, 9, null)
            .setAttackDuration(24);

    AttackFramedata chopAttack = new AttackFramedata()
            .addDamageFrame(11, 4, Vector3d.ZERO, 2.0, 3, false)
            .setAttackDuration(25);

    AttackFramedata epitaphChopAttack = new AttackFramedata()
            .addDamageFrame(11, 17, Vector3d.ZERO, 2.0, 3)
            .setAttackDuration(30);

    AttackFramedata timeErase = new AttackFramedata()
            .addMessageFrame(30, 4, null, null)
            .setAttackDuration(32);

    AttackFramedata execution = new AttackFramedata()
            .addDamageFrame(10, 1, Vector3d.ZERO, 4, 1)
            .addMessageFrame(14, 2, null, null)
            .addMenacingFrame(15)
            .addMessageFrame(88, 3, null, null)
            .addMenacingFrame(88)
            .setAttackDuration(95);

    AttackFramedata recovery = new AttackFramedata().setAttackDuration(90);

    AttackFramedata epitaph = new AttackFramedata()
            .addMessageFrame(11, 5, null, null)
            .setAttackDuration(30);

    ChargeAttackFormat chargeAttackFormat = new ChargeAttackFormat("charging")
            .addChargeNode(60, 3, true)
            .addChargeNode(140, 4, true)
            .addChargeNode(200, 6, true)
            .addChargeNode(300, 8, false);


    HoveringMoveHandler controller = new HoveringMoveHandler(this)
            .addMove("Punch", 1, mainPunchData, "crimsonpunch", 1.65)
            .addMove("Barrage",2, barrageData, "crimsonbarrage", HoveringMoveHandler.RepositionConstants.MASTER_POSITION)
            .addMove("Medium Punch",3, lightChargeAttack, "weakcharge", 2.0)
            .addMove("Strong Punch",4, mediumChargeAttack, "midcharge", 2.0)
            .addMove("Time Erase Follow Up",5, chopAttack, "chop", HoveringMoveHandler.RepositionConstants.DO_NOT_MOVE)
            .addMove("Donut Punch",6, execution, "execution", HoveringMoveHandler.RepositionConstants.DO_NOT_MOVE)
            .addMove("Time Erase",7, timeErase, "erase", 1.35)
            .addMove("Recovery",8, recovery, "recovery", HoveringMoveHandler.RepositionConstants.IDLE_POSITION)
            .addMove("Epitaph Prediction",9, epitaph, "epitaph", HoveringMoveHandler.RepositionConstants.IDLE_POSITION)
            .addMove("Epitaph Counter-Attack",10, epitaphChopAttack, "chop", 1.85)
            .addChargeAttack(chargeAttackFormat);

    public HoveringMoveHandler getController(){
        return controller;
    }


    public KingCrimsonEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public void epitaph() {

        if(spendEnergy(90)) {
            controller.setMoveActive(9);
        }

    }

    public void teleport(double multiplier) {
        if (world.isRemote || master == null || epitaphTicks > 0) return;

        Stand.getLazyOptional(master).ifPresent(stand -> {


            if (timeEraseActive) {
                endTimeSkip();
            } else if (!timeEraseActive) {
                if (opportunityTicks > 0) {
                    if (getMostRecentlyDamagedEntity() != null && spendEnergy(20, true)) { //
                        stand.setTimeSkipEffectTicker(30);
                        world.playSound(null, getPosition(), SoundInit.KING_CRIMSON_COMBO.get(), SoundCategory.NEUTRAL, 0.65f, 1.2f);
                        if(getMostRecentlyDamagedEntity() instanceof PlayerEntity){
                            Stand.getLazyOptional(((PlayerEntity) getMostRecentlyDamagedEntity())).ifPresent(props -> {
                                props.setTimeSkipEffectTicker(30);
                            });
                        }

                        opportunityTicks = 0;
                        controller.cancelActiveMoves();
                        controller.setMoveActive(5);
                    }
                } else {
                    if(opportunityTicks == -10 && !activeTimeErase) {
                        if(!controller.isMoveActive() && getEnergyPercentage() == 1 && spendEnergy(20)) {
                            controller.setMoveActive(7);
                        }
                    }
                }
            }
        });
    }




    @Override
    public void tick() {
        super.tick();
        if (getMaster() == null) return;

        Stand.getLazyOptional(master).ifPresent(stand -> {
            ability = stand.getAbility();
            if (opportunityTicks > 0 && getMostRecentlyDamagedEntity() != null && energyAtThreshold(15)) {
                Util.spawnParticle(this, 6, this.getPosX(), this.getPosY() + this.getEyeHeight() - 0.5, this.getPosZ(), 1.2, 2.5, 1.2, 1);
            }
        });


        opportunityTicks = Math.max(opportunityTicks - 1, -10);

        if (controller.isMoveActive()) {
            if (controller.getActiveMove().getId() == 5 && getMostRecentlyDamagedEntity() != null) {
                Vector3d position = getMostRecentlyDamagedEntity().getPositionVec().add(getMostRecentlyDamagedEntity().getLookVec().normalize().inverse().mul(1.5, 1.5, 1.5));
                setPosition(position.x, position.y, position.z);
                faceEntity(getMostRecentlyDamagedEntity(), Float.MAX_VALUE, Float.MAX_VALUE);
                getMostRecentlyDamagedEntity().addPotionEffect(new EffectInstance(Effects.BLINDNESS, 10, 1));
            }

            if (executionTarget != null) {
                if (controller.getActiveMove().getId() == 6) {
                    if (clipToPosition) {
                        Vector3d position = this.getPositionVec().add(this.getLookVec().normalize().mul(3.2, 3.2, 3.2));
                        executionTarget.setPositionAndUpdate(position.x, position.y + controller.getActiveMove().getFramedata().getTicker() / 80.0, position.z);
                        if(executionTarget instanceof PlayerEntity){
                            Util.applyUnactionableTicks((PlayerEntity) executionTarget, 1);
                        }
                    }
                    if (!executionTarget.isAlive()) {
                        executionTarget = null;
                        controller.cancelActiveMoves();
                    }
                }
            }
        }
        if (!world.isRemote()) {
            if (timeEraseActive) {

                Util.spawnParticle(this, 6, master.getPosX(), master.getPosY(), master.getPosZ(), 30, 12, 30, 125);
                Util.spawnParticle(this, 8, master.getPosX(), master.getPosY(), master.getPosZ(), 30, 12, 30, 125);

                if (timeEraseDuration == 0 || timeEraseDuration % 20 == 0) {
                    //master.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 21, 1));
                }
                for (EpitaphPairing pair : pairList) {
                    if (pair.getPrediction() instanceof MonsterEntity) {
                        ((MonsterEntity) pair.getPrediction()).setAttackTarget(null);
                    }
                    pair.getPrediction().setRevengeTarget(null);
                    pair.getPredicted().addPotionEffect(new EffectInstance(Effects.BLINDNESS, 25, 1));
                    Util.spawnParticle(this, 7, pair.getPrediction().getPosX(), pair.getPrediction().getPosY() + 0.75, pair.getPrediction().getPosZ(), 1.3, 1.45, 1.3, 1);
                }
                master.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 25, 1));
                master.setInvulnerable(true);

                timeEraseDuration++;


                if(epitaphFlag){
                    if(timeEraseDuration >= 100){
                        epitaphFlag = false;
                        endTimeSkip();
                        epitaphEndingOpportunityTicks = 40;
                    }

                    Stand.getLazyOptional(master).ifPresent(stand -> {
                        stand.setCounterBuffer(false);
                    });
                }else{
                    if(!spendEnergy(0.45, true)){ //Channeled energy spending ignores actionability
                        endTimeSkip();
                    }
                }

            } else {
                timeEraseDuration = 0;
                this.setInvisible(false);
                master.setInvulnerable(false);
            }

            if(epitaphTicks > 0){
                Util.spawnParticle(this, 6, master.getPosX(), master.getPosY() + 0.75, master.getPosZ(), 1.3, 1.45, 1.3, 1);
                Util.spawnParticle(this, 7, master.getPosX(), master.getPosY() + 0.75, master.getPosZ(), 1.3, 1.45, 1.3, 1);
            }

            epitaphTicks = Math.max(epitaphTicks - 1, -1);
            epitaphEndingOpportunityTicks = Math.max(epitaphEndingOpportunityTicks - 1, 0);
        }
    }

    private void teleportNearby(LivingEntity entityLiving){
        //right from the chorus fruit

        for(int i = 0; i < 16; ++i) {
            double d3 = entityLiving.getPosX() + (entityLiving.getRNG().nextDouble() - 0.5D) * 4.0D;
            double d4 = MathHelper.clamp(entityLiving.getPosY() + (double)(entityLiving.getRNG().nextInt(2) - 1), 0.0D, (entityLiving.world.getHeight() - 1));
            double d5 = entityLiving.getPosZ() + (entityLiving.getRNG().nextDouble() - 0.5D) * 4.0D;
            if (entityLiving.isPassenger()) {
                entityLiving.stopRiding();
            }

            entityLiving.attemptTeleport(d3, d4, d5, true);
        }
    }

    @Override
    public MovementAnimationHolder getMovementAnimations(){

        return new MovementAnimationHolder().create("kingcrimson", "kingcrimsonforward", "crimsonright", "crimsonleft", "kingcrimsonbackwards");
        //Yes, joejan called the left animation "right" and right animation "left". Make fun of him for me if you see this.
    }

    @Override
    public void messageFrame(int message1, Object message2, Object message3) {
        if(message1 == 1){
            opportunityTicks = (int) message2;
        }
        if(message1 == 2){
            if(getMostRecentlyDamagedEntity() == null){
                controller.cancelActiveMoves();
                controller.setMoveActive(8);
            }else{
                executionTarget = getMostRecentlyDamagedEntity();
                clipToPosition = true;
            }
        }

        if(message1 == 3){
            if(executionTarget != null){
                Util.dealStandDamage(this, executionTarget, 50f, Vector3d.ZERO, false);
                clipToPosition = false;
            }
        }

        if(message1 == 4){
            startTimeSkip();
        }

        if(message1 == 5){
            epitaphTicks = 30;
            Stand.getLazyOptional(master).ifPresent(stand -> {
                stand.setCounterBuffer(true);
            });

            master.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 30, 1));
        }
    }

    private void startTimeSkip(){
        Stand.getLazyOptional(master).ifPresent(stand -> {
            stand.setPreventUnsummon2(true);
            stand.setExperiencingTimeSkip(true);
        });

        world.playSound(null, getPosition(), SoundInit.TIME_SKIP_BEGIN.get(), SoundCategory.NEUTRAL, 1, 1);

        timeEraseActive = true;
        activeTimeErase = true;
        getServer().getWorld(this.world.getDimensionKey()).getEntities()
                .filter(entity -> !entity.equals(master) && !entity.equals(this))
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> verify((LivingEntity) entity))
                .filter(entity -> entity.getDistance(this) < 50)
                .forEach(entity -> createPairing((LivingEntity) entity));
    }

    public void endTimeSkip(){

        timeEraseActive = false;

        world.playSound(null, getPosition(), SoundInit.TIME_SKIP_END.get(), SoundCategory.NEUTRAL, 1, 1);


        Stand.getLazyOptional(master).ifPresent(stand -> {
            stand.setTimeSkipEffectTicker(30);
            stand.setExperiencingTimeSkip(false);
        });

        pairList.forEach(epitaphPairing -> {
            LivingEntity predicted = epitaphPairing.getPredicted();
            LivingEntity prediction = epitaphPairing.getPrediction();

            predicted.copyLocationAndAnglesFrom(prediction);
            predicted.setPositionAndUpdate(prediction.getPosX(), prediction.getPosY(), prediction.getPosZ());
            predicted.setRotation(prediction.rotationYaw, prediction.rotationPitch);
            predicted.setMotion(prediction.getMotion());
            predicted.fallDistance = prediction.fallDistance;
            predicted.setInvulnerable(false);

            prediction.setGlowing(false);
            prediction.setInvulnerable(false);
            prediction.setPosition(prediction.getPosX(), -999, prediction.getPosZ());
            prediction.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);

            if (predicted instanceof PlayerEntity) {
                Stand.getLazyOptional(((PlayerEntity) predicted)).ifPresent(props -> {
                    props.setTimeSkipEffectTicker(30);
                    props.setExperiencingTimeSkip(false);
                });
            }
        });
        pairList.clear();

        Stand.getLazyOptional(master).ifPresent(stand -> {
            stand.setPreventUnsummon2(false);
        });

        activeTimeErase = false;
    }

    private void createPairing(LivingEntity entityIn){
        LivingEntity predictionEntity = null;

        if(entityIn instanceof PlayerEntity){
            predictionEntity = new VillagerEntity(EntityType.VILLAGER, entityIn.world);
        }else if(entityIn instanceof ZombifiedPiglinEntity){
            predictionEntity = new ZombifiedPiglinEntity(EntityType.ZOMBIFIED_PIGLIN, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
        }else if(entityIn instanceof BlazeEntity){
            predictionEntity = new BlazeEntity(EntityType.BLAZE, entityIn.world);
        }else if(entityIn instanceof DrownedEntity){
            predictionEntity = new DrownedEntity(EntityType.DROWNED, entityIn.world);
        }else if(entityIn instanceof ElderGuardianEntity){
            predictionEntity = new ElderGuardianEntity(EntityType.ELDER_GUARDIAN, entityIn.world);
        }else if(entityIn instanceof EndermiteEntity){
            predictionEntity = new EndermiteEntity(EntityType.ENDERMITE, entityIn.world);
        }else if(entityIn instanceof EvokerEntity){
            predictionEntity = new EvokerEntity(EntityType.EVOKER, entityIn.world);
        }else if(entityIn instanceof GhastEntity){
            predictionEntity = new GhastEntity(EntityType.GHAST, entityIn.world);
        }else if(entityIn instanceof GuardianEntity){
            predictionEntity = new GuardianEntity(EntityType.GUARDIAN, entityIn.world);
        }else if(entityIn instanceof MagmaCubeEntity){
            predictionEntity = new MagmaCubeEntity(EntityType.MAGMA_CUBE, entityIn.world);
        }else if(entityIn instanceof PhantomEntity){
            predictionEntity = new PhantomEntity(EntityType.PHANTOM, entityIn.world);
        }else if(entityIn instanceof PillagerEntity){
            predictionEntity = new PillagerEntity(EntityType.PILLAGER, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.CROSSBOW));
        }else if(entityIn instanceof RavagerEntity){
            predictionEntity = new RavagerEntity(EntityType.RAVAGER, entityIn.world);
        }else if(entityIn instanceof SilverfishEntity){
            predictionEntity = new SilverfishEntity(EntityType.SILVERFISH, entityIn.world);
        }else if(entityIn instanceof SlimeEntity){
            predictionEntity = new SlimeEntity(EntityType.SLIME, entityIn.world);
        }else if(entityIn instanceof StrayEntity){
            predictionEntity = new StrayEntity(EntityType.STRAY, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BOW));
        }else if(entityIn instanceof VexEntity){
            predictionEntity = new VexEntity(EntityType.VEX, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        }else if(entityIn instanceof VindicatorEntity){
            predictionEntity = new VindicatorEntity(EntityType.VINDICATOR, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        }else if(entityIn instanceof WitchEntity){
            predictionEntity = new WitchEntity(EntityType.WITCH, entityIn.world);
        }else if(entityIn instanceof WitherSkeletonEntity){
            predictionEntity = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, entityIn.world);
        }else if(entityIn instanceof ZombieVillagerEntity){
            predictionEntity = new ZombieVillagerEntity(EntityType.ZOMBIE_VILLAGER, entityIn.world);
        }else if(entityIn instanceof ZombieEntity || entityIn instanceof HuskEntity){
            predictionEntity = new HuskEntity(EntityType.HUSK, entityIn.world);
        }else if(entityIn instanceof SkeletonEntity){
            predictionEntity = new SkeletonEntity(EntityType.SKELETON, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BOW));
        }else if(entityIn instanceof SpiderEntity){
            predictionEntity = new SpiderEntity(EntityType.SPIDER, entityIn.world);
        }else if(entityIn instanceof CreeperEntity){
            predictionEntity = new CreeperEntity(EntityType.CREEPER, entityIn.world);
        }else if(entityIn instanceof StrayEntity){
            predictionEntity = new StrayEntity(EntityType.STRAY, entityIn.world);
            predictionEntity.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BOW));
        }else if(entityIn instanceof IronGolemEntity){
            predictionEntity = new IronGolemEntity(EntityType.IRON_GOLEM, entityIn.world);
        }else if(entityIn instanceof EndermanEntity){
            predictionEntity = new EndermanEntity(EntityType.ENDERMAN, entityIn.world);
        }else if(entityIn instanceof CaveSpiderEntity){
            predictionEntity = new CaveSpiderEntity(EntityType.CAVE_SPIDER, entityIn.world);
        }

        if(predictionEntity != null){
            predictionEntity.copyLocationAndAnglesFrom(entityIn);
            predictionEntity.setGlowing(true);
            predictionEntity.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 42069420, 1));
            //predictionEntity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 42069420, 0)); //hehe big number
            predictionEntity.setInvulnerable(true);
            predictionEntity.setSilent(true);
            if(predictionEntity instanceof MonsterEntity){
                    ((MonsterEntity) predictionEntity).setAttackTarget(null);
            }
            predictionEntity.setRevengeTarget(null);

            entityIn.setInvulnerable(true);
            teleportNearby(predictionEntity);
            world.addEntity(predictionEntity);

            if(entityIn instanceof PlayerEntity){
                Stand.getLazyOptional(((PlayerEntity) entityIn)).ifPresent(stand -> {
                    stand.setExperiencingTimeSkip(true);
                });
            }

            pairList.add(new EpitaphPairing(entityIn, predictionEntity));
        }
    }

    @Override
    public void onMasterAttacked(Entity damager, float damage) {
        if(!world.isRemote()) {
            if (epitaphTicks > 0) {
                epitaphTicks = -1;
                timeEraseDuration = 0;
                controller.cancelActiveMoves();
                epitaphFlag = true;
                startTimeSkip();
            }
        }
    }

    @Override
    public void onHit(LivingEntity entity, float damage) {
        if(this.getController().isMoveActive()){
            if(getController().getActiveMove().getId() == 5){
                EffectInstance debuffEffect = new EffectInstance(EffectInit.STAND_WEAKNESS.get(), 80, 1);
                entity.addPotionEffect(debuffEffect);
            }
        }
    }

    class EpitaphPairing{
        private final LivingEntity predicted;
        private final LivingEntity prediction;

        public EpitaphPairing(LivingEntity predictedIn, LivingEntity predictionIn){
            predicted = predictedIn;
            prediction = predictionIn;
        }

        public LivingEntity getPredicted(){
            return predicted;
        }

        public LivingEntity getPrediction(){
            return prediction;
        }
    }

    public boolean verify(LivingEntity livingEntityIn){
        for(EpitaphPairing pair : pairList){
            if(pair.getPredicted().equals(livingEntityIn) || pair.getPrediction().equals(livingEntityIn)){
                return false;
            }
        }
        return true;
    }

    @Override
    public int getJabMoveId(){
        if(epitaphEndingOpportunityTicks > 0){
            return 10;
        }
        return 1;
    }


}