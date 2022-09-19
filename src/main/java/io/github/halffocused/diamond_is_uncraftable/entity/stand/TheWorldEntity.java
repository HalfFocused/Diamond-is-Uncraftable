package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.ChargeAttackFormat;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.MovementAnimationHolder;
import io.github.halffocused.diamond_is_uncraftable.util.timestop.TimestopHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TheWorldEntity extends AbstractStandEntity implements IMomentum, IOverrideChargeAttack, IOnMasterAttacked {
    public static long dayTime = -1, gameTime = -1;
    /**
     * A list of every {@link TheWorldEntity} in the {@link World}, used to cancel events and unfreeze entities on logout.
     */
    private static ArrayBlockingQueue<TheWorldEntity> theWorldList = new ArrayBlockingQueue<>(1000000);
    public int timestopTick;
    public boolean shouldDamageBeCancelled;
    public boolean cooldown;
    boolean timeStopped = false;
    public static final int QUICK_STOP_DURATION = 4 + 2;
    int noTeleportPeriod = 0;
    private ArrayList<PlayerEntity> timestoppedPlayers = new ArrayList<>();

    public HoveringMoveHandler getController(){
        return controller;
    }
    
    private ArrayBlockingQueue<BlockPos> brokenBlocks = new ArrayBlockingQueue<>(100000);

    public TheWorldEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    ChargeAttackFormat chargeAttack = new ChargeAttackFormat("taunt")
            .addChargeNode(11, 3, true);

    AttackFramedata punchData = new AttackFramedata()
            .addDamageFrame(7, 3, Vector3d.ZERO, 2.1, 2)
            .addDamageFrame(9, 4, Vector3d.ZERO, 2.2, 2)
            .setAttackDuration(19);

    AttackFramedata tauntTp = new AttackFramedata()
            .addMessageFrame(1, 1,null, null)
            .setAttackDuration(1);

    AttackFramedata barrageData = new AttackFramedata()
                        .generateInterval(10, 125, 2, 2, Vector3d.ZERO, 1.7, 6)
                        .setAttackDuration(125);

    AttackFramedata timestop = new AttackFramedata()
            .addMessageFrame(45, 1, null, null)
            .addMenacingFrame(45)
            .setAttackDuration(62);

    HoveringMoveHandler controller = new HoveringMoveHandler(this)
            .addMove("Jab",1, punchData, "jab", 1.6)
            .addMove("Barrage",2, barrageData, "MUDA", HoveringMoveHandler.RepositionConstants.MASTER_POSITION)
            .addMove("Taunt",3, tauntTp, null, -1)
            .addMove("Timestop", 4, timestop, "timestop", 2)
            .addChargeAttack(chargeAttack);

    public static ArrayBlockingQueue<TheWorldEntity> getTheWorldList() {
        return theWorldList;
    }

    public void timeStop() {
        if (getMaster() == null) return;
        if (attacking) return;

        if(timeStopped){
            timeStopped = false;
            TimestopHelper.endTimeStop(master);
        }else{
            Stand.getLazyOptional(master).ifPresent(props -> {
                if (spendEnergy(10)) {
                    controller.setMoveActive(4);
                }
            });
        }
    }

    public void teleport(){

        if(!world.isRemote){

            if(!timeStopped && noTeleportPeriod == 0 && spendEnergy(40)) {
                world.playSound(null, getPosition(), SoundInit.THE_WORLD_TELEPORT.get(), SoundCategory.NEUTRAL, 1, 1);

                master.fallDistance = 0;
                Util.teleportUntilWall(master, master.getLookVec(), 4);
                Stand.getLazyOptional(master).ifPresent(stand -> {
                    stand.setInstantTimeStopFrame(QUICK_STOP_DURATION);
                });
            }
        }
    }

    public void addBrokenBlocks(BlockPos pos) {
        brokenBlocks.add(pos);
    }

    @Override
    public SoundEvent getSpawnSound() {
        return SoundInit.SUMMON_STAND.get();
    }

    @Override
    public void playSpawnSound() {
        Stand.getLazyOptional(getMaster()).ifPresent(props -> {
            if (!props.getAbility())
                world.playSound(null, getMaster().getPosition(), SoundInit.THE_WORLD_SUMMON.get(), SoundCategory.NEUTRAL, 2, 1);
        });
    }

    public ArrayBlockingQueue<BlockPos> getBrokenBlocks() {
        return brokenBlocks;
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null && !world.isRemote()) {

            noTeleportPeriod = Math.max(0, noTeleportPeriod - 1);

            Stand.getLazyOptional(master).ifPresent(props2 -> {


                if (controller.isMoveActive()) {
                    if (controller.getActiveMove().getId() == 2 && controller.getActiveMove().getFramedata().getTicker() > 10) {

                        if(props2.getMomentum() == 0){
                            Style warningStyle = Style.EMPTY.setFormatting(TextFormatting.RED);
                            StringTextComponent warning = new StringTextComponent("The World needs momentum to perform it's barrage!");
                            warning.setStyle(warningStyle);
                            master.sendStatusMessage(warning, false);
                        }

                        props2.setMomentum(Math.max(0, props2.getMomentum() - 1));

                        if (props2.getMomentum() == 0) {
                            controller.cancelActiveMoves();
                        }

                    }
                }
                if(timeStopped){
                    TimestopHelper.timeStopTick(master);
                    if(!spendEnergy((9.0/20), true)){
                        timeStopped = false;
                        TimestopHelper.endTimeStop(master);
                    }
                }
                props2.setPreventUnsummon2(timeStopped);
            });
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        timeStopped = false;
        TimestopHelper.endTimeStop(master);
    }

    @Override
    public double addMomentumAmount() {

        if(controller.isMoveActive()){
            if(controller.getActiveMove().getId() == 2 || TimestopHelper.isTimeStopped(world, master.getPosition())){
                return 0;
            }
        }

        return 7;
    }

    @Override
    public double getMomentumDrainRate() {
        return 2;
    }


    @Override
    public MovementAnimationHolder getMovementAnimations(){
        return new MovementAnimationHolder().create("idle", "foward", "right2", "right", "backwards");
        //Can this mf ever spell every animation name properly????????????? "foward"
    }

    @Override
    public int getJabMoveId() {
        return 1;
    }

    @Override
    public int getBarrageMoveId() {
        return 2;
    }

    @Override
    public void messageFrame(int message1, Object message2, Object message3) {
        if(message1 == 1){
            if(!world.isRemote()) {
                world.playSound(null, getPosition(), SoundInit.THE_WORLD_TIME_STOP.get(), SoundCategory.NEUTRAL, 1, 1);
                timeStopped = true;
            }
        }
    }

    private void sendToTauntPosition(LivingEntity entityIn, float tauntChargeTicks){
        double tauntDistance = getTauntDistance((int) tauntChargeTicks);
        Vector3d modifiedPositionVec = new Vector3d(master.getPosX(), entityIn.getPosY(), master.getPosZ());
        Vector3d vec = entityIn.getPositionVec().subtract(modifiedPositionVec).normalize();
        Util.teleportUntilWall(entityIn, vec, tauntDistance);

        entityIn.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 5, 1));
        entityIn.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 5, 1));

        if(entityIn instanceof PlayerEntity){
            Util.applyUnactionableTicks(((PlayerEntity) entityIn), 20);

            Stand.getLazyOptional(((PlayerEntity) entityIn)).ifPresent(effect -> {
                effect.setInstantTimeStopFrame(QUICK_STOP_DURATION);
            });
        }
    }

    @Override
    public void chargeAttackRelease(int ticksCharged) {
        if(!TimestopHelper.isTimeStopped(world, master.getPosition())) {

            world.playSound(null, getPosition(), SoundInit.THE_WORLD_TELEPORT.get(), SoundCategory.NEUTRAL, 1, 1);
            if (getTauntDistance(ticksCharged) > 0) {

                Stand.getLazyOptional(master).ifPresent(effect -> {
                    effect.setInstantTimeStopFrame(QUICK_STOP_DURATION);
                });

                getServer().getWorld(world.getDimensionKey()).getEntities()
                        .filter(entity -> !entity.equals(master) && !entity.equals(this))
                        .filter(entity -> entity instanceof LivingEntity)
                        .filter(entity -> entity.getDistance(master) <= 7)
                        .forEach(entity -> sendToTauntPosition((LivingEntity) entity, ticksCharged));
            }
        }
    }

    @Override
    public StringTextComponent generateHudText(int ticksCharged) {

        double tauntDistance = getTauntDistance(ticksCharged);

        if(!spendEnergy(0.35) || ticksCharged > 199){
            controller.forceChargeAttackRelease();
        }

        Style distanceStyle = Style.EMPTY.setFormatting(TextFormatting.GRAY).setBold(true);

        DecimalFormat format = new DecimalFormat("0.0");

        StringTextComponent returnValue = ticksCharged < 15 ? new StringTextComponent("Charging...") : new StringTextComponent(format.format(tauntDistance) + " Blocks");
        returnValue.setStyle(distanceStyle);

        return returnValue;
    }

    @Override
    public boolean shouldAllowChargeAttack() {
        return true;
    }

    @Override
    public void onMasterAttacked(Entity damager, float damage) {

        noTeleportPeriod = 40;

        if(controller.isMoveActive()){
            if(controller.getActiveMove().getId() == 4){
                controller.cancelActiveMoves();
                penalizeEnergy(30);
            }
        }
    }

    private double getTauntDistance(int ticks){
        return ticks < 15 ? 0 : Math.min(ticks / 12.0, 8);
    }

    @Override
    public float getDamageSharingPercentage(){
        return 0.65f;
    }
}
