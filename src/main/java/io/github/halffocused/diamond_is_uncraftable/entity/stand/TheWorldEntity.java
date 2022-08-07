package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.ITimestop;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.Timestop;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.NailBulletEntity;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.ChargeAttackFormat;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.MovementAnimationHolder;
import net.minecraft.entity.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
            .addDamageFrame(7, 3, Vec3d.ZERO, 2.1, 2)
            .addDamageFrame(9, 4, Vec3d.ZERO, 2.2, 2)
            .setAttackDuration(19);

    AttackFramedata tauntTp = new AttackFramedata()
            .addMessageFrame(1, 1,null, null)
            .setAttackDuration(1);

    AttackFramedata barrageData = new AttackFramedata()
                        .generateInterval(10, 125, 2, 2, Vec3d.ZERO, 1.7, 6)
                        .setAttackDuration(125);

    AttackFramedata timestop = new AttackFramedata()
            .addMessageFrame(45, 1, null, null)
            .addMenacingFrame(45)
            .setAttackDuration(62);

    MoveEffects punchEffectsHolder = new MoveEffects(3, null, null);

    HoveringMoveHandler controller = new HoveringMoveHandler(this)
            .addMove("Jab",1, punchData, "jab", 1.6, punchEffectsHolder)
            .addMove("Barrage",2, barrageData, "MUDA", HoveringMoveHandler.RepositionConstants.MASTER_POSITION, punchEffectsHolder)
            .addMove("Taunt",3, tauntTp, null, -1, punchEffectsHolder)
            .addMove("Timestop", 4, timestop, "timestop", 2)
            .addChargeAttack(chargeAttack);

    public static ArrayBlockingQueue<TheWorldEntity> getTheWorldList() {
        return theWorldList;
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (theWorldList.size() > 0) {
            theWorldList.forEach(theWorld -> {
                if (theWorld.ability && !theWorld.cooldown) {
                    if (dayTime != -1 && gameTime != -1) {
                        world.setDayTime(dayTime);
                        world.setGameTime(gameTime);
                    } else {
                        dayTime = world.getDayTime();
                        gameTime = world.getGameTime();
                    }
                }
            });
        } else if (theWorldList.size() <= 0 && StarPlatinumEntity.getStarPlatinumList().size() <= 0) {
            if (!world.isRemote) {
                world.getServer().getWorld(world.dimension.getType()).getEntities()
                        .filter(entity -> !(entity instanceof PlayerEntity))
                        .forEach(entity -> Timestop.getLazyOptional(entity).ifPresent(props -> {
                            if (props.isEmpty())
                                return;
                            if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props.getMotionX() != 0 && props.getMotionY() != 0 && props.getMotionZ() != 0)) {
                                entity.setMotion(props.getMotionX(), props.getMotionY(), props.getMotionZ());
                                entity.setNoGravity(false);
                            } else if (props.getMotionX() != 0 && props.getMotionY() != 0 && props.getMotionZ() != 0)
                                entity.setMotion(props.getMotionX(), props.getMotionY(), props.getMotionZ());
                            if (entity instanceof MobEntity)
                                ((MobEntity) entity).setNoAI(false);
                            entity.velocityChanged = true;
                            if (props.getFallDistance() != 0)
                                entity.fallDistance = props.getFallDistance();
                            if (props.getDamage().size() > 0)
                                props.getDamage().forEach((source, amount) -> {
                                    DamageSource damageSource = DamageSource.GENERIC;
                                    String newSource = source.replaceAll("[0123456789]", "");
                                    switch (newSource) {
                                        case "inFire": {
                                            damageSource = DamageSource.IN_FIRE;
                                            break;
                                        }
                                        case "onFire": {
                                            damageSource = DamageSource.ON_FIRE;
                                            break;
                                        }
                                        case "lightningBolt": {
                                            damageSource = DamageSource.LIGHTNING_BOLT;
                                            break;
                                        }
                                        case "lava": {
                                            damageSource = DamageSource.LAVA;
                                            break;
                                        }
                                        case "hotFloor": {
                                            damageSource = DamageSource.HOT_FLOOR;
                                            break;
                                        }
                                        case "inWall": {
                                            damageSource = DamageSource.IN_WALL;
                                            break;
                                        }
                                        case "cramming": {
                                            damageSource = DamageSource.CRAMMING;
                                            break;
                                        }
                                        case "drown": {
                                            damageSource = DamageSource.DROWN;
                                            break;
                                        }
                                        case "starve": {
                                            damageSource = DamageSource.STARVE;
                                            break;
                                        }
                                        case "cactus": {
                                            damageSource = DamageSource.CACTUS;
                                            break;
                                        }
                                        case "fall": {
                                            damageSource = DamageSource.FALL;
                                            break;
                                        }
                                        case "flyIntoWall": {
                                            damageSource = DamageSource.FLY_INTO_WALL;
                                            break;
                                        }
                                        case "outOfWorld": {
                                            damageSource = DamageSource.OUT_OF_WORLD;
                                            break;
                                        }
                                        case "magic": {
                                            damageSource = DamageSource.MAGIC;
                                            break;
                                        }
                                        case "wither": {
                                            damageSource = DamageSource.WITHER;
                                            break;
                                        }
                                        case "anvil": {
                                            damageSource = DamageSource.ANVIL;
                                            break;
                                        }
                                        case "fallingBlock": {
                                            damageSource = DamageSource.FALLING_BLOCK;
                                            break;
                                        }
                                        case "dragonBreath": {
                                            damageSource = DamageSource.DRAGON_BREATH;
                                            break;
                                        }
                                        case "fireworks": {
                                            damageSource = DamageSource.FIREWORKS;
                                            break;
                                        }
                                        case "dryout": {
                                            damageSource = DamageSource.DRYOUT;
                                            break;
                                        }
                                        case "sweetBerryBush": {
                                            damageSource = DamageSource.SWEET_BERRY_BUSH;
                                            break;
                                        }
                                    }
                                    entity.attackEntityFrom(damageSource, amount);
                                    entity.hurtResistantTime = 0;
                                });
                            dayTime = -1;
                            gameTime = -1;
                            props.clear();
                        }));
            }
        }
    }

    @SubscribeEvent
    public static void fluidEvent(BlockEvent.FluidPlaceBlockEvent event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    event.setCanceled(true);
            });

    }

    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getPlayer().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void blockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown) {
                    if (event.getEntity() == null)
                        event.setCanceled(true);
                    else {
                        if (event.getEntity().getUniqueID() != theWorldEntity.master.getUniqueID())
                            event.setCanceled(true);
                    }
                }
            });
    }

    @SubscribeEvent
    public static void pistonEvent(PistonEvent.Pre event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void playerInteract1(PlayerInteractEvent.EntityInteractSpecific event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getPlayer().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void playerInteract2(PlayerInteractEvent.EntityInteract event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getPlayer().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void playerInteract3(PlayerInteractEvent.RightClickBlock event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getPlayer().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void playerInteract4(PlayerInteractEvent.RightClickItem event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getPlayer().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void playerInteract5(PlayerInteractEvent.LeftClickBlock event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getPlayer().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void enderTeleport(EnderTeleportEvent event) {
        if (theWorldList.size() > 0)
            theWorldList.forEach(theWorldEntity -> {
                if (theWorldEntity.ability && !theWorldEntity.cooldown)
                    if (event.getEntity().getUniqueID() != theWorldEntity.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    public void timeStop() {
        if (getMaster() == null) return;
        if (attacking) return;

        if(timeStopped){
            world.playSound(null, getPosition(), SoundInit.THE_WORLD_TIME_RESUME.get(), SoundCategory.NEUTRAL, 1, 1);
            timeStopped = false;
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


                if(controller.isMoveActive()){
                    if(controller.getActiveMove().getId() == 2 && controller.getActiveMove().getFramedata().getTicker() > 10){
                        props2.setMomentum(Math.max(0, props2.getMomentum() - 1));

                        if(props2.getMomentum() == 0){
                            controller.cancelActiveMoves();
                        }

                    }else if (this.ticksExisted % 12 == 0){
                        props2.setMomentum(Math.max(0, props2.getMomentum() - 1));
                    }
                }else if (this.ticksExisted % 12 == 0){
                    props2.setMomentum(Math.max(0, props2.getMomentum() - 1));
                }

                ability = timeStopped;
                props2.setAbilityActive(ability);
                props2.setPreventUnsummon2(props2.getAbilityActive());

                if (props2.getAbilityActive()) {
                    Timestop.getLazyOptional(master).ifPresent(ITimestop::clear);
                    timestopTick++;
                    shouldDamageBeCancelled = true;
                    master.setInvulnerable(true);
                    if (timestopTick == 1 && props2.getCooldown() <= 0) {
                        //world.playSound(null, getPosition(), SoundInit.STOP_TIME.get(), getSoundCategory(), 5, 1);
                    }
                    if (!theWorldList.contains(this))
                        theWorldList.add(this);

                    if (!world.isRemote) {
                        if (timestopTick == 1 || dayTime == -1 || gameTime == -1) {
                            dayTime = world.getDayTime();
                            gameTime = world.getGameTime();
                        }

                        world.getServer().getWorld(dimension).getEntities()
                                .filter(entity -> entity instanceof PlayerEntity)
                                .filter(entity -> DiamondIsUncraftableConfig.COMMON.timeStopRange.get() == -1 || entity.getDistance(this) < DiamondIsUncraftableConfig.COMMON.timeStopRange.get())
                                .forEach(entity -> {
                                    Stand props = Stand.getCapabilityFromPlayer((PlayerEntity) entity);
                                    props.setExperiencingTimeStop(true);
                                    timestoppedPlayers.add((PlayerEntity) entity);
                                });

                        world.getServer().getWorld(dimension).getEntities()
                                .filter(entity -> entity != this)
                                .filter(entity -> entity != master)
                                .filter(entity -> !(entity instanceof GoldExperienceRequiemEntity))
                                .filter(entity -> !(entity instanceof TheWorldEntity))
                                .filter(entity -> !(entity instanceof StarPlatinumEntity))
                                .filter(entity -> DiamondIsUncraftableConfig.COMMON.timeStopRange.get() == -1 || entity.getDistance(this) < DiamondIsUncraftableConfig.COMMON.timeStopRange.get())
                                .forEach(entity -> {
                                    if (entity instanceof PlayerEntity) {
                                        Stand props = Stand.getCapabilityFromPlayer((PlayerEntity) entity);
                                        if (props.getStandID() == Util.StandID.GER)
                                            return;
                                        if (props.getStandID() == Util.StandID.THE_WORLD)
                                            return;
                                        if (props.getStandID() == Util.StandID.STAR_PLATINUM)
                                            return;
                                    }
                                    if(entity instanceof NailBulletEntity){
                                        if(((NailBulletEntity) entity).isInfinite){
                                            return;
                                        }
                                    }
                                    if (entity instanceof MobEntity) {
                                        if (((MobEntity) entity).getAttackTarget() == master || ((MobEntity) entity).getRevengeTarget() == master) {
                                            ((MobEntity) entity).setAttackTarget(null);
                                            ((MobEntity) entity).setRevengeTarget(null);
                                        }
                                        ((MobEntity) entity).setNoAI(true);
                                    }
                                    if (timestopTick == 1) {
                                        Timestop.getLazyOptional(entity).ifPresent(props -> {
                                            props.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
                                            props.setMotion(entity.getMotion().getX(), entity.getMotion().getY(), entity.getMotion().getZ());
                                            props.setRotation(entity.rotationYaw, entity.rotationPitch, entity.getRotationYawHead());
                                            props.setFallDistance(entity.fallDistance);
                                            props.setFire(entity.getFireTimer());
                                            if (entity instanceof TNTEntity)
                                                props.setFuse(((TNTEntity) entity).getFuse());
                                            if (entity instanceof TNTMinecartEntity)
                                                props.setFuse(((TNTMinecartEntity) entity).minecartTNTFuse);
                                            if (entity instanceof ItemEntity)
                                                props.setAge(((ItemEntity) entity).age);
                                        });
                                    } else {
                                        Timestop.getLazyOptional(entity).ifPresent(props -> {
                                            if (props.getPosX() != 0 && props.getPosY() != 0 && props.getPosZ() != 0) {
                                                entity.setPosition(props.getPosX(), props.getPosY(), props.getPosZ());
                                                if ((entity instanceof IProjectile) || (entity instanceof ItemEntity) || (entity instanceof DamagingProjectileEntity))
                                                    entity.setNoGravity(true);
                                                else {
                                                    entity.rotationYaw = props.getRotationYaw();
                                                    entity.rotationPitch = props.getRotationPitch();
                                                    entity.setRotationYawHead(props.getRotationYawHead());
                                                }
                                                if (entity instanceof PlayerEntity)
                                                    ((PlayerEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 50, 255, false, false));
                                                entity.setMotion(0, 0, 0);
                                                entity.fallDistance = props.getFallDistance();
                                                entity.setFireTimer(props.getFire());
                                                if (entity instanceof TNTEntity)
                                                    ((TNTEntity) entity).setFuse(props.getFuse());
                                                if (entity instanceof TNTMinecartEntity)
                                                    ((TNTMinecartEntity) entity).minecartTNTFuse = props.getFuse();
                                                if (entity instanceof ItemEntity)
                                                    ((ItemEntity) entity).age = props.getAge();
                                                entity.velocityChanged = true;
                                            } else {
                                                props.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
                                                props.setMotion(entity.getMotion().getX(), entity.getMotion().getY(), entity.getMotion().getZ());
                                                props.setRotation(entity.rotationYaw, entity.rotationPitch, entity.getRotationYawHead());
                                                props.setFallDistance(entity.fallDistance);
                                                props.setFire(entity.getFireTimer());
                                                if (entity instanceof TNTEntity)
                                                    props.setFuse(((TNTEntity) entity).getFuse());
                                                if (entity instanceof TNTMinecartEntity)
                                                    props.setFuse(((TNTMinecartEntity) entity).minecartTNTFuse);
                                                if (entity instanceof ItemEntity)
                                                    props.setAge(((ItemEntity) entity).age);
                                            }
                                        });
                                    }
                                });
                    }
                } else {
                    shouldDamageBeCancelled = false;
                    timestopTick = 0;
                    master.setInvulnerable(false);
                    theWorldList.remove(this);
                    brokenBlocks.forEach(pos -> {
                        world.getBlockState(pos).getBlock().harvestBlock(world, master, pos, world.getBlockState(pos), null, master.getActiveItemStack());
                        world.removeBlock(pos, false);
                    });
                    brokenBlocks.clear();

                    for(PlayerEntity playerEntity : timestoppedPlayers){
                        Stand props = Stand.getCapabilityFromPlayer(playerEntity);
                        props.setExperiencingTimeStop(false);
                    }
                    timestoppedPlayers.clear();

                    if (!world.isRemote) {
                        world.getServer().getWorld(dimension).getEntities()
                                .filter(entity -> entity != this)
                                .filter(entity -> entity != master)
                                .forEach(entity -> Timestop.getLazyOptional(entity).ifPresent(props -> {
                                    if (props.isEmpty())
                                        return;
                                    if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props.getMotionX() != 0 && props.getMotionY() != 0 && props.getMotionZ() != 0)) {
                                        entity.setMotion(props.getMotionX(), props.getMotionY(), props.getMotionZ());
                                        entity.setNoGravity(false);
                                    } else if (props.getMotionX() != 0 && props.getMotionY() != 0 && props.getMotionZ() != 0)
                                        entity.setMotion(props.getMotionX(), props.getMotionY(), props.getMotionZ());
                                    if (entity instanceof PlayerEntity) {
                                        ((PlayerEntity) entity).removePotionEffect(Effects.SLOWNESS);
                                        Stand stand = Stand.getCapabilityFromPlayer(((PlayerEntity) entity));
                                        stand.setExperiencingTimeStop(false);
                                    }
                                    if (entity instanceof MobEntity)
                                        ((MobEntity) entity).setNoAI(false);
                                    entity.velocityChanged = true;
                                    if (props.getFallDistance() != 0)
                                        entity.fallDistance = props.getFallDistance();
                                    if (props.getDamage().size() > 0)
                                        props.getDamage().forEach((source, amount) -> {
                                            DamageSource damageSource = DamageSource.GENERIC;
                                            String newSource = source.replaceAll("[0123456789]", "");
                                            switch (newSource) {
                                                case "inFire": {
                                                    damageSource = DamageSource.IN_FIRE;
                                                    break;
                                                }
                                                case "onFire": {
                                                    damageSource = DamageSource.ON_FIRE;
                                                    break;
                                                }
                                                case "lightningBolt": {
                                                    damageSource = DamageSource.LIGHTNING_BOLT;
                                                    break;
                                                }
                                                case "lava": {
                                                    damageSource = DamageSource.LAVA;
                                                    break;
                                                }
                                                case "hotFloor": {
                                                    damageSource = DamageSource.HOT_FLOOR;
                                                    break;
                                                }
                                                case "inWall": {
                                                    damageSource = DamageSource.IN_WALL;
                                                    break;
                                                }
                                                case "cramming": {
                                                    damageSource = DamageSource.CRAMMING;
                                                    break;
                                                }
                                                case "drown": {
                                                    damageSource = DamageSource.DROWN;
                                                    break;
                                                }
                                                case "starve": {
                                                    damageSource = DamageSource.STARVE;
                                                    break;
                                                }
                                                case "cactus": {
                                                    damageSource = DamageSource.CACTUS;
                                                    break;
                                                }
                                                case "fall": {
                                                    damageSource = DamageSource.FALL;
                                                    break;
                                                }
                                                case "flyIntoWall": {
                                                    damageSource = DamageSource.FLY_INTO_WALL;
                                                    break;
                                                }
                                                case "outOfWorld": {
                                                    damageSource = DamageSource.OUT_OF_WORLD;
                                                    break;
                                                }
                                                case "magic": {
                                                    damageSource = DamageSource.MAGIC;
                                                    break;
                                                }
                                                case "wither": {
                                                    damageSource = DamageSource.WITHER;
                                                    break;
                                                }
                                                case "anvil": {
                                                    damageSource = DamageSource.ANVIL;
                                                    break;
                                                }
                                                case "fallingBlock": {
                                                    damageSource = DamageSource.FALLING_BLOCK;
                                                    break;
                                                }
                                                case "dragonBreath": {
                                                    damageSource = DamageSource.DRAGON_BREATH;
                                                    break;
                                                }
                                                case "fireworks": {
                                                    damageSource = DamageSource.FIREWORKS;
                                                    break;
                                                }
                                                case "dryout": {
                                                    damageSource = DamageSource.DRYOUT;
                                                    break;
                                                }
                                                case "sweetBerryBush": {
                                                    damageSource = DamageSource.SWEET_BERRY_BUSH;
                                                    break;
                                                }
                                            }
                                            entity.attackEntityFrom(damageSource, amount);
                                            entity.hurtResistantTime = 0;
                                        });
                                    dayTime = -1;
                                    gameTime = -1;
                                    props.clear();
                                }));
                    }
                }

                if (!ability || !spendEnergy(11 / 20.0, true)) {

                    if(timestopTick > 0){
                        world.playSound(null, getPosition(), SoundInit.THE_WORLD_TIME_RESUME.get(), SoundCategory.NEUTRAL, 1, 1);
                    }

                    timestopTick = 0;
                    master.setInvulnerable(false);
                    timeStopped = false;
                }
            });

        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        ability = false;
        master.setInvulnerable(false);
        shouldDamageBeCancelled = false;
        theWorldList.remove(this);
        brokenBlocks.forEach(pos -> {
            world.getBlockState(pos).getBlock().harvestBlock(world, master, pos, world.getBlockState(pos), null, master.getActiveItemStack());
            world.removeBlock(pos, false);
        });
        brokenBlocks.clear();
        dayTime = -1;
        gameTime = -1;
        if (!world.isRemote)
            world.getServer().getWorld(dimension).getEntities()
                    .filter(entity -> entity != this)
                    .forEach(entity ->
                            Timestop.getLazyOptional(entity).ifPresent(props2 -> {
                                if (props2.isEmpty())
                                    return;
                                if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)) {
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                    entity.setNoGravity(false);
                                } else if (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                if (entity instanceof PlayerEntity) {
                                    ((PlayerEntity) entity).removePotionEffect(Effects.SLOWNESS);
                                    Stand stand = Stand.getCapabilityFromPlayer(((PlayerEntity) entity));
                                    stand.setExperiencingTimeStop(false);
                                }
                                if (entity instanceof MobEntity)
                                    ((MobEntity) entity).setNoAI(false);
                                entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                entity.velocityChanged = true;
                                entity.fallDistance = props2.getFallDistance();
                                entity.setInvulnerable(false);
                                if (props2.getDamage().size() > 0)
                                    props2.getDamage().forEach((source, amount) -> {
                                        DamageSource damageSource = DamageSource.GENERIC;
                                        switch (source) {
                                            case "inFire": {
                                                damageSource = DamageSource.IN_FIRE;
                                                break;
                                            }
                                            case "onFire": {
                                                damageSource = DamageSource.ON_FIRE;
                                                break;
                                            }
                                            case "lightningBolt": {
                                                damageSource = DamageSource.LIGHTNING_BOLT;
                                                break;
                                            }
                                            case "lava": {
                                                damageSource = DamageSource.LAVA;
                                                break;
                                            }
                                            case "hotFloor": {
                                                damageSource = DamageSource.HOT_FLOOR;
                                                break;
                                            }
                                            case "inWall": {
                                                damageSource = DamageSource.IN_WALL;
                                                break;
                                            }
                                            case "cramming": {
                                                damageSource = DamageSource.CRAMMING;
                                                break;
                                            }
                                            case "drown": {
                                                damageSource = DamageSource.DROWN;
                                                break;
                                            }
                                            case "starve": {
                                                damageSource = DamageSource.STARVE;
                                                break;
                                            }
                                            case "cactus": {
                                                damageSource = DamageSource.CACTUS;
                                                break;
                                            }
                                            case "fall": {
                                                damageSource = DamageSource.FALL;
                                                break;
                                            }
                                            case "flyIntoWall": {
                                                damageSource = DamageSource.FLY_INTO_WALL;
                                                break;
                                            }
                                            case "outOfWorld": {
                                                damageSource = DamageSource.OUT_OF_WORLD;
                                                break;
                                            }
                                            case "magic": {
                                                damageSource = DamageSource.MAGIC;
                                                break;
                                            }
                                            case "wither": {
                                                damageSource = DamageSource.WITHER;
                                                break;
                                            }
                                            case "anvil": {
                                                damageSource = DamageSource.ANVIL;
                                                break;
                                            }
                                            case "fallingBlock": {
                                                damageSource = DamageSource.FALLING_BLOCK;
                                                break;
                                            }
                                            case "dragonBreath": {
                                                damageSource = DamageSource.DRAGON_BREATH;
                                                break;
                                            }
                                            case "fireworks": {
                                                damageSource = DamageSource.FIREWORKS;
                                                break;
                                            }
                                            case "dryout": {
                                                damageSource = DamageSource.DRYOUT;
                                                break;
                                            }
                                            case "sweetBerryBush": {
                                                damageSource = DamageSource.SWEET_BERRY_BUSH;
                                                break;
                                            }
                                        }
                                        entity.attackEntityFrom(damageSource, amount);
                                        entity.hurtResistantTime = 0;
                                    });
                                props2.clear();
                            }));
    }

    @Override
    public int addMomentumAmount() {

        if(controller.isMoveActive()){
            if(controller.getActiveMove().getId() == 2 || ability){
                return 0;
            }
        }

        return 7;
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
        Vec3d modifiedPositionVec = new Vec3d(master.getPosX(), entityIn.getPosY(), master.getPosZ());
        Vec3d vec = entityIn.getPositionVec().subtract(modifiedPositionVec).normalize();
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
        if(!timeStopped) {

            world.playSound(null, getPosition(), SoundInit.THE_WORLD_TELEPORT.get(), SoundCategory.NEUTRAL, 1, 1);
            if (getTauntDistance(ticksCharged) > 0) {

                Stand.getLazyOptional(master).ifPresent(effect -> {
                    effect.setInstantTimeStopFrame(QUICK_STOP_DURATION);
                });

                getServer().getWorld(dimension).getEntities()
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

        Style distanceStyle = new Style().setColor(TextFormatting.GRAY).setBold(true);

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
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (master == null || damageSource.getTrueSource() == master || damageSource == DamageSource.CACTUS || damageSource == DamageSource.FALL || Util.isTimeStoppedForEntity(master))
            return false; //Prevents Stands from taking damage they shouldn't, fall damage, cactus damage, etc.

        master.attackEntityFrom(damageSource, damage * 0.65f);
        return false;
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
}
