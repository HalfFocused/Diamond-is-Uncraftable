package io.github.halffocused.diamond_is_uncraftable.event;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandEffects;
import io.github.halffocused.diamond_is_uncraftable.capability.StandPerWorldCapability;
import io.github.halffocused.diamond_is_uncraftable.capability.Timestop;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.SheerHeartAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.StarPlatinumEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandCapabilityPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;

import static io.github.halffocused.diamond_is_uncraftable.util.Util.StandID.*;

/**
 * Syncs the {@link Stand} capability to the client, for use in GUIs, {@link Timestop} is not synced because it's info is disposable and useless to the client.
 */
@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventSyncCapability {
    @SubscribeEvent
    public static void saveStand(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || DiamondIsUncraftableConfig.COMMON.saveStandOnDeath.get()) {
            Stand.getLazyOptional(event.getOriginal()).ifPresent(originalProps -> {
                ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                Stand.getLazyOptional(player).ifPresent(newProps -> newProps.clone(originalProps));
            });
        }

        if(event.isWasDeath() && !DiamondIsUncraftableConfig.COMMON.saveStandOnDeath.get()){
            if(DiamondIsUncraftableConfig.COMMON.uniqueStandMode.get()){
                Objects.requireNonNull(event.getPlayer().world.getServer()).getWorld(DimensionType.OVERWORLD);
                StandPerWorldCapability.getLazyOptional(event.getPlayer().world.getServer().getWorld(DimensionType.OVERWORLD)).ifPresent(uniqueStandHandler -> {
                    ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
                    Stand stand = Stand.getCapabilityFromPlayer(player);
                    List standAssignments = Collections.singletonList(STANDS);
                    if(uniqueStandHandler.getTakenStandIDs().contains(standAssignments.indexOf(stand.getStandID()))){
                        uniqueStandHandler.removeTakenStandId(standAssignments.indexOf(stand.getStandID()));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props)));
    }

    @SubscribeEvent
    public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props)));
    }

    @SubscribeEvent
    public static void playerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props)));
    }

    @SubscribeEvent
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        player.setInvulnerable(false);
        Stand.getLazyOptional(player).ifPresent(props -> { //It's a lot of code to run on logout, but some horrible bugs occur without it.
            if (!player.world.isRemote) {
                player.getServerWorld().getEntities()
                        .filter(entity -> entity instanceof SheerHeartAttackEntity)
                        .filter(entity -> ((SheerHeartAttackEntity) entity).getMaster() == player)
                        .forEach(Entity::remove);
                switch (props.getStandID()) {
                    case THE_WORLD: {
                        player.getServerWorld().getEntities()
                                .forEach(entity -> {
                                    Timestop.getLazyOptional(entity).ifPresent(props2 -> {
                                        if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)) {
                                            entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                            entity.setNoGravity(false);
                                        } else if (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)
                                            entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                        if (entity instanceof MobEntity)
                                            ((MobEntity) entity).setNoAI(false);
                                        entity.velocityChanged = true;
                                        entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                        entity.fallDistance = props2.getFallDistance();
                                        entity.setInvulnerable(false);
                                        if (props2.getDamage().size() > 0)
                                            props2.getDamage().forEach((source, amount) -> {
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
                                        props2.clear();
                                    });
                                    Entity theWorld = player.world.getEntityByID(props.getPlayerStand());
                                    if (theWorld instanceof TheWorldEntity) {
                                        ((TheWorldEntity) theWorld).shouldDamageBeCancelled = false;
                                        TheWorldEntity.getTheWorldList().remove(theWorld);
                                        ((TheWorldEntity) theWorld).getBrokenBlocks().forEach(pos -> {
                                            theWorld.world.getBlockState(pos).getBlock().harvestBlock(theWorld.world, player, pos, theWorld.world.getBlockState(pos), null, player.getActiveItemStack());
                                            theWorld.world.removeBlock(pos, false);
                                        });
                                        ((TheWorldEntity) theWorld).getBrokenBlocks().clear();
                                    }
                                });
                        break;
                    }
                    case STAR_PLATINUM: {
                        player.getServerWorld().getEntities()
                                .forEach(entity -> {
                                    Timestop.getLazyOptional(entity).ifPresent(props2 -> {
                                        if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)) {
                                            entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                            entity.setNoGravity(false);
                                        } else {
                                            if (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)
                                                entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                        }
                                        if (entity instanceof MobEntity)
                                            ((MobEntity) entity).setNoAI(false);
                                        entity.velocityChanged = true;
                                        entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                        entity.fallDistance = props2.getFallDistance();
                                        entity.setInvulnerable(false);
                                        props2.getDamage().forEach((source, amount) -> {
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
                                        });
                                        props2.clear();
                                    });
                                    Entity starPlatinum = player.world.getEntityByID(props.getPlayerStand());
                                    if (starPlatinum instanceof StarPlatinumEntity) {
                                        ((StarPlatinumEntity) starPlatinum).shouldDamageBeCancelled = false;
                                        StarPlatinumEntity.getStarPlatinumList().remove(starPlatinum);
                                        ((StarPlatinumEntity) starPlatinum).getBrokenBlocks().forEach(pos -> {
                                            starPlatinum.world.getBlockState(pos).getBlock().harvestBlock(starPlatinum.world, player, pos, starPlatinum.world.getBlockState(pos), null, player.getActiveItemStack());
                                            starPlatinum.world.removeBlock(pos, false);
                                        });
                                        ((StarPlatinumEntity) starPlatinum).getBrokenBlocks().clear();
                                    }
                                });
                        break;
                    }
                    case THE_GRATEFUL_DEAD: {
                        player.getServerWorld().getEntities()
                                .filter(entity -> !entity.equals(player))
                                .filter(entity -> entity instanceof LivingEntity)
                                .forEach(entity -> StandEffects.getLazyOptional(entity).ifPresent(props2 -> props2.setAging(false)));
                        break;
                    }
                }
                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SSyncStandCapabilityPacket(props));
            }
        });
    }
}
