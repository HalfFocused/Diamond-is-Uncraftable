package io.github.halffocused.diamond_is_uncraftable.util.timestop;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.ITimestop;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.Timestop;
import io.github.halffocused.diamond_is_uncraftable.capability.WorldTimestopCapability;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID)
public class TimestopHelper {

    public static boolean isTimeStopped(World worldIn, ChunkPos chunkPosIn) {
        WorldTimestopCapability timestoppedChunks = WorldTimestopCapability.getCapabilityFromWorld(worldIn);
        return timestoppedChunks.getTimestoppedChunkPosList().stream().anyMatch(timestoppedChunk -> timestoppedChunk.getChunkPos().equals(chunkPosIn));
    }

    public static boolean isTimeStopped(World worldIn, BlockPos blockPosIn) {
        return isTimeStopped(worldIn, worldIn.getChunkAt(blockPosIn).getPos());
    }

    public static boolean isTimeStopped(World worldIn, PlayerEntity playerIn) {
        Stand stand = Stand.getCapabilityFromPlayer(playerIn);
        return isTimeStopped(worldIn, playerIn.getPosition()) && !Util.canStandMoveInStoppedTime(stand.getStandID());
    }

    public static boolean isTimeStopped(World worldIn, Entity entityIn) {
        if(entityIn instanceof PlayerEntity){
            return isTimeStopped(worldIn, (PlayerEntity) entityIn);
        }else {
            return isTimeStopped(worldIn, entityIn.getPosition());
        }
    }


    /**
     * Called every tick when a player is stopping time. It does, in order:
     * 1) Remove any timestopped chunks by the player if the player is no longer in range of them.
     * 2) Add all chunks within timestop range of the user to the WorldTimestopCapability
     * @param timeStopper The player who is stopping time.
     */
    public static void timeStopTick(PlayerEntity timeStopper){
        World world = timeStopper.getEntityWorld();
        UUID uuid = timeStopper.getUniqueID();
        int timeStopRange = DiamondIsUncraftableConfig.COMMON.timeStopRange.get();
        WorldTimestopCapability timestoppedChunks = WorldTimestopCapability.getCapabilityFromWorld(world);

        ChunkPos masterChunkPos = world.getChunkAt(timeStopper.getPosition()).getPos();
        int masterChunkX = masterChunkPos.x;
        int masterChunkZ = masterChunkPos.z;

        ArrayList<WorldTimestopCapability.TimestoppedChunk> outOfRangeChunks = new ArrayList<>();
        for(WorldTimestopCapability.TimestoppedChunk timestoppedChunk : timestoppedChunks.getTimestoppedChunkPosList()){
            if(!chunkWithinTimestopRange(masterChunkPos, timestoppedChunk.getChunkPos()) && timeStopper.getUniqueID().equals(timestoppedChunk.getUUID())){
                outOfRangeChunks.add(timestoppedChunk);
            }
        }
        for(WorldTimestopCapability.TimestoppedChunk removeChunk : outOfRangeChunks){
            timestoppedChunks.removeTimestoppedChunk(removeChunk.getChunkPos(), removeChunk.getUUID());
        }


        //There is surely a better way to do this
        for(int i = masterChunkX - timeStopRange; i <= masterChunkX + timeStopRange; i++){
            for(int j = masterChunkZ - timeStopRange; j <= masterChunkZ + timeStopRange; j++){
                if(chunkWithinTimestopRange(masterChunkPos, new ChunkPos(i,j))){

                    WorldTimestopCapability.TimestoppedChunk chunk = new WorldTimestopCapability.TimestoppedChunk(new ChunkPos(i,j), uuid);

                    timestoppedChunks.addTimestoppedChunk(chunk.getChunkPos(), chunk.getUUID());
                }
            }
        }
    }

    /**
     * Remove all timestopped chunks that were stopped by the supplied player.
     * @param timeStopper The player who is no longer stopping time.
     */
    public static void endTimeStop(PlayerEntity timeStopper) {
        World world = timeStopper.getEntityWorld();
        WorldTimestopCapability timestoppedChunks = WorldTimestopCapability.getCapabilityFromWorld(world);

        ArrayList<WorldTimestopCapability.TimestoppedChunk> chunksToRemove = new ArrayList<>();
        for (WorldTimestopCapability.TimestoppedChunk timestoppedChunk : timestoppedChunks.getTimestoppedChunkPosList()) {
            if (timestoppedChunk.getUUID().equals(timeStopper.getUniqueID())) {
                chunksToRemove.add(timestoppedChunk);
            }
        }
        for(WorldTimestopCapability.TimestoppedChunk removeChunk : chunksToRemove){
            timestoppedChunks.removeTimestoppedChunk(removeChunk.getChunkPos(), removeChunk.getUUID());
        }

    }

    public static boolean chunkWithinTimestopRange(ChunkPos timestopPos, ChunkPos testPos) {
        int timeStopRange = DiamondIsUncraftableConfig.COMMON.timeStopRange.get();
        return Math.abs(timestopPos.x - testPos.x) < timeStopRange && Math.abs(timestopPos.z - testPos.z) < timeStopRange;
    }

    @SubscribeEvent
    public static void fluidEvent(BlockEvent.FluidPlaceBlockEvent event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void fluidEvent(BlockEvent.CreateFluidSourceEvent event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() == null) {
            event.setCanceled(true);
        } else {
            World world = event.getWorld().getWorld();
            if (isTimeStopped(world, event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }



    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPlayer())) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void pistonEvent(PistonEvent.Pre event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerInteract1(PlayerInteractEvent.EntityInteractSpecific event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerInteract2(PlayerInteractEvent.EntityInteract event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerInteract3(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerInteract4(PlayerInteractEvent.RightClickItem event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerInteract5(PlayerInteractEvent.LeftClickBlock event) {
        World world = event.getWorld().getWorld();
        if (isTimeStopped(world, event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void enderTeleport(EnderTeleportEvent event) {
        World world = event.getEntity().world;
        if (isTimeStopped(world, event.getEntity().getPosition())) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        ServerWorld world = (ServerWorld) event.world;

        if (!world.isRemote) {
            world.getEntities()
                    .filter(entity -> !(entity instanceof AbstractStandEntity))
                    .forEach(entity -> {

                        if(entity instanceof PlayerEntity){
                            Stand.getLazyOptional(((PlayerEntity) entity)).ifPresent(stand -> stand.setExperiencingTimeStop(isTimeStopped(world, entity.getPosition())));
                        }

                        if (isTimeStopped(world, entity)) {
                            Timestop.getLazyOptional(entity).ifPresent(timestop -> {
                                if (timestop.getPosX() == 0 && timestop.getPosY() == 0 && timestop.getPosZ() == 0) {
                                    timestop.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
                                    timestop.setMotion(entity.getMotion().getX(), entity.getMotion().getY(), entity.getMotion().getZ());
                                    timestop.setRotation(entity.rotationYaw, entity.rotationPitch, entity.getRotationYawHead());
                                    timestop.setFallDistance(entity.fallDistance);
                                    timestop.setFire(entity.getFireTimer());
                                    if (entity instanceof TNTEntity)
                                        timestop.setFuse(((TNTEntity) entity).getFuse());
                                    if (entity instanceof TNTMinecartEntity)
                                        timestop.setFuse(((TNTMinecartEntity) entity).minecartTNTFuse);
                                    if (entity instanceof ItemEntity)
                                        timestop.setAge(((ItemEntity) entity).age);
                                } else { //Load information from capability

                                    entity.setPosition(timestop.getPosX(), timestop.getPosY(), timestop.getPosZ());

                                    if ((entity instanceof IProjectile) || (entity instanceof ItemEntity) || (entity instanceof DamagingProjectileEntity))
                                        entity.setNoGravity(true);
                                    else {
                                        entity.rotationYaw = timestop.getRotationYaw();
                                        entity.rotationPitch = timestop.getRotationPitch();
                                        entity.setRotationYawHead(timestop.getRotationYawHead());
                                    }

                                    if (entity instanceof MobEntity) {
                                        ((MobEntity) entity).setNoAI(true);
                                    }
                                    entity.setMotion(0, 0, 0);
                                    entity.fallDistance = timestop.getFallDistance();
                                    entity.setFireTimer(timestop.getFire());

                                    if (entity instanceof TNTEntity)
                                        ((TNTEntity) entity).setFuse(timestop.getFuse());
                                    if (entity instanceof TNTMinecartEntity)
                                        ((TNTMinecartEntity) entity).minecartTNTFuse = timestop.getFuse();
                                    if (entity instanceof ItemEntity)
                                        ((ItemEntity) entity).age = timestop.getAge();

                                    if(entity instanceof PlayerEntity){
                                        Stand.getLazyOptional(((PlayerEntity) entity)).ifPresent(stand -> {
                                            Util.applyUnactionableTicks(((PlayerEntity) entity), 1);
                                        });
                                        ((PlayerEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 50, 255, false, false));
                                    }
                                    entity.velocityChanged = true;
                                }
                            });
                        } else {
                            Timestop.getLazyOptional(entity).ifPresent(timestop -> {
                                if (!timestop.isEmpty()) {
                                    if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (timestop.getMotionX() != 0 && timestop.getMotionY() != 0 && timestop.getMotionZ() != 0)) {
                                        entity.setMotion(timestop.getMotionX(), timestop.getMotionY(), timestop.getMotionZ());
                                        entity.setNoGravity(false);
                                    } else if (timestop.getMotionX() != 0 && timestop.getMotionY() != 0 && timestop.getMotionZ() != 0) {
                                        entity.setMotion(timestop.getMotionX(), timestop.getMotionY(), timestop.getMotionZ());
                                    }
                                    entity.velocityChanged = true;

                                    if (entity instanceof MobEntity) {
                                        ((MobEntity) entity).setNoAI(false);
                                    }
                                    if (timestop.getFallDistance() != 0) {
                                        entity.fallDistance = timestop.getFallDistance();
                                    }
                                    if(entity instanceof PlayerEntity){
                                        ((PlayerEntity) entity).removePotionEffect(Effects.SLOWNESS);
                                    }

                                    if (timestop.getDamage().size() > 0)
                                        timestop.getDamage().forEach((source, amount) -> {
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
                                    timestop.clear();
                                }
                            });
                        }
                    });
        }
    }

    @SubscribeEvent
    public static void cancelDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if(isTimeStopped(entity.getEntityWorld(), entity)) {
            Timestop.getLazyOptional(entity).ifPresent(props -> {
                if (!props.getDamage().containsKey(event.getSource().getDamageType()))
                    props.getDamage().put(event.getSource().getDamageType(), event.getAmount());
                else
                    for (int i = 0; i < 1000; i++) {
                        if (!props.getDamage().containsKey(event.getSource().getDamageType() + i)) {
                            props.getDamage().put(event.getSource().getDamageType() + i, event.getAmount());
                            break;
                        }
                    }
            });
            event.setCanceled(true);
        }
    }

}