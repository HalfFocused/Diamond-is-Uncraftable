package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.ITimestop;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.Timestop;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.StarPlatinumPunchEntity;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import io.github.halffocused.diamond_is_uncraftable.util.timestop.TimestopHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ArrayBlockingQueue;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StarPlatinumEntity extends AbstractStandEntity {
    public static long dayTime = -1, gameTime = -1;
    /**
     * A list of every {@link StarPlatinumEntity} in the {@link World}, used to cancel events and unfreeze entities on logout.
     */
    private static ArrayBlockingQueue<StarPlatinumEntity> starPlatinumList = new ArrayBlockingQueue<>(1000000);
    public int timestopTick;
    public boolean shouldDamageBeCancelled;
    public boolean cooldown;
    private ArrayBlockingQueue<BlockPos> brokenBlocks = new ArrayBlockingQueue<>(100000);

    public StarPlatinumEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public static ArrayBlockingQueue<StarPlatinumEntity> getStarPlatinumList() {
        return starPlatinumList;
    }

    public HoveringMoveHandler getController(){
        return null;
    }


    @SubscribeEvent
    public static void fluidEvent(BlockEvent.FluidPlaceBlockEvent event) {
        if (starPlatinumList.size() > 0)
            starPlatinumList.forEach(starPlatinum -> {
                if (starPlatinum.ability && !starPlatinum.cooldown)
                    event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event) {
        if (starPlatinumList.size() > 0)
            starPlatinumList.forEach(starPlatinum -> {
                if (starPlatinum.ability && !starPlatinum.cooldown)
                    if (event.getPlayer().getUniqueID() != starPlatinum.master.getUniqueID())
                        event.setCanceled(true);
            });
    }

    @SubscribeEvent
    public static void blockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if (starPlatinumList.size() > 0)
            starPlatinumList.forEach(starPlatinum -> {
                if (starPlatinum.ability && !starPlatinum.cooldown) {
                    if (event.getEntity() == null)
                        event.setCanceled(true);
                    else {
                        if (event.getEntity().getUniqueID() != starPlatinum.master.getUniqueID())
                            event.setCanceled(true);
                    }
                }
            });
    }

    public ArrayBlockingQueue<BlockPos> getBrokenBlocks() {
        return brokenBlocks;
    }

    public void addBrokenBlocks(BlockPos pos) {
        brokenBlocks.add(pos);
    }

    @Override
    public void playSpawnSound() {
        Stand.getLazyOptional(getMaster()).ifPresent(props -> {
            if (!props.getAbility())
                world.playSound(null, getMaster().getPosition(), SoundInit.SPAWN_STAR_PLATINUM.get(), SoundCategory.NEUTRAL, 5, 1);
        });
    }

    public void teleport() {
        if (getMaster() == null) return;
        Stand.getLazyOptional(master).ifPresent(props -> {
            if (props.getCooldown() == 0) {
                Vec3d position = master.getLookVec().mul(28.06, 1, 28.06).add(master.getPositionVec());
                for (double i = position.getY() - 0.5; world.getBlockState(new BlockPos(position.getX(), i, position.getZ())).isSolid(); i++)
                    position = position.add(0, 0.5, 0);
                if (world.getBlockState(new BlockPos(position)).isSolid())
                    return;
                master.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
                world.playSound(null, master.getPosition(), SoundInit.THE_WORLD_TELEPORT.get(), SoundCategory.HOSTILE, 1, 1);
                props.setCooldown(200);
            }
        });
    }

    public void dodgeAttacks() {
        if (getMaster() == null) return;
        Stand.getLazyOptional(master).ifPresent(stand -> {
            if (stand.getCooldown() == 0 && stand.getInvulnerableTicks() == 0)
                stand.setInvulnerableTicks(100);
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props2 -> {
                ability = props2.getAbility();

                if(!world.isRemote()) {
                    if (ability) {
                        TimestopHelper.timeStopTick(master);
                    }else{
                        TimestopHelper.endTimeStop(master);
                    }
                }
            });

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        StarPlatinumPunchEntity starPlatinum1 = new StarPlatinumPunchEntity(world, this, master);
                        starPlatinum1.randomizePositions();
                        starPlatinum1.shoot(master, master.rotationPitch, master.rotationYaw, 2.4f, 0.17f);
                        world.addEntity(starPlatinum1);
                        StarPlatinumPunchEntity starPlatinum2 = new StarPlatinumPunchEntity(world, this, master);
                        starPlatinum2.randomizePositions();
                        starPlatinum2.shoot(master, master.rotationPitch, master.rotationYaw, 2.4f, 0.17f);
                        world.addEntity(starPlatinum2);
                    }
                if (attackTicker >= 160) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        ability = false;
        master.setInvulnerable(false);
        shouldDamageBeCancelled = false;
        brokenBlocks.forEach(pos -> {
            world.getBlockState(pos).getBlock().harvestBlock(world, master, pos, world.getBlockState(pos), null, master.getActiveItemStack());
            world.removeBlock(pos, false);
        });
        brokenBlocks.clear();
        starPlatinumList.remove(this);
        dayTime = -1;
        gameTime = -1;
        if (world.isRemote) return;
        getServer().getWorld(dimension).getEntities()
                .filter(entity -> entity != this)
                .forEach(entity ->
                        Timestop.getLazyOptional(entity).ifPresent(props2 -> {
                            if (props2.isEmpty())
                                return;
                            if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)) {
                                entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                entity.setNoGravity(false);
                            } else {
                                if (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                            }
                            if (entity instanceof PlayerEntity)
                                ((PlayerEntity) entity).removePotionEffect(Effects.SLOWNESS);
                            if (entity instanceof MobEntity)
                                ((MobEntity) entity).setNoAI(false);
                            entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                            entity.velocityChanged = true;
                            entity.fallDistance = props2.getFallDistance();
                            entity.setInvulnerable(false);
                            System.out.println("3");
                            props2.clear();
                        }));
    }
}
