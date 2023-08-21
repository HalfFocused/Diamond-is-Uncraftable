package io.github.halffocused.diamond_is_uncraftable.util.globalabilities;

import io.github.halffocused.diamond_is_uncraftable.capability.*;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenBitesTheDustEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitesTheDustHelper {
    public static final int BITES_THE_DUST_DURATION = 24000;
    public static boolean bitesTheDustActive = false;
    public static int bitesTheDustTicks = -1;
    public static long activatedDayTime = -1;
    public static ServerWorld bitesTheDustWorld = null;
    public static PlayerEntity bitesTheDustPlayer = null;
    public static PlayerEntity previousBitesTheDustPlayer = null;
    public static ArrayList<LivingEntity> killedEntities = new ArrayList<>();
    public static ArrayList<ItemEntity> pickedUpItems = new ArrayList<>();
    public static ArrayList<FatedDeath> fatedDeaths = new ArrayList<>();


    public static void startBitesTheDust(KillerQueenBitesTheDustEntity standEntity){
        bitesTheDustPlayer = standEntity.getMaster();
        bitesTheDustTicks = BITES_THE_DUST_DURATION;
        bitesTheDustActive = true;
        bitesTheDustWorld = (ServerWorld) standEntity.world;
        activatedDayTime = bitesTheDustWorld.getDayTime();


        /*
         * Save the current position, health, age, and movement of all entities.
         * Players also have their inventories and Ender Chests saved.
         */

        bitesTheDustWorld.getEntities()
                .filter(entity -> !(entity instanceof AbstractStandEntity))
                .forEach(entity -> BitesTheDustCapability.getLazyOptional(entity).ifPresent(bitesTheDustCapability -> {
                    if(bitesTheDustCapability.isEmpty()){
                        bitesTheDustCapability.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
                        bitesTheDustCapability.setMotion(entity.getMotion().getX(), entity.getMotion().getY(), entity.getMotion().getZ());
                        bitesTheDustCapability.setRotation(entity.rotationYaw, entity.rotationPitch, entity.getRotationYawHead());
                        bitesTheDustCapability.setFallDistance(entity.fallDistance);
                        bitesTheDustCapability.setFire(entity.getFireTimer());
                        if(entity instanceof LivingEntity) {
                            bitesTheDustCapability.setHealth(((LivingEntity) entity).getHealth());
                            if(entity instanceof PlayerEntity){
                                bitesTheDustCapability.setIsMaster(entity.equals(bitesTheDustPlayer));
                                bitesTheDustCapability.setHunger(((PlayerEntity) entity).getFoodStats().getFoodLevel());
                            }
                        }
                        if (entity instanceof TNTEntity)
                            bitesTheDustCapability.setFuse(((TNTEntity) entity).getFuse());
                        if (entity instanceof TNTMinecartEntity)
                            bitesTheDustCapability.setFuse(((TNTMinecartEntity) entity).minecartTNTFuse);
                        if (entity instanceof ItemEntity)
                            bitesTheDustCapability.setAge(((ItemEntity) entity).age);
                    }

                    if (entity instanceof PlayerEntity)
                        StandPlayerEffects.getLazyOptional((PlayerEntity) entity).ifPresent(standPlayerEffects -> {
                            for (int i = 0; i < ((PlayerEntity) entity).inventory.mainInventory.size(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).inventory.mainInventory.get(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getMainInventory().set(i, stack.copy());
                            }
                            for (int i = 0; i < ((PlayerEntity) entity).inventory.armorInventory.size(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).inventory.armorInventory.get(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getArmorInventory().set(i, stack.copy());
                            }
                            for (int i = 0; i < ((PlayerEntity) entity).inventory.offHandInventory.size(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).inventory.offHandInventory.get(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getOffHandInventory().set(i, stack.copy());
                            }
                            for (int i = 0; i < ((PlayerEntity) entity).getInventoryEnderChest().getSizeInventory(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).getInventoryEnderChest().getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getEnderChestInventory().set(i, stack.copy());
                            }
                        });
                }));

        /*
         * Save the contents of all loaded LockableTileEntities.
         */

        bitesTheDustWorld.loadedTileEntityList.forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                    if (tileEntity instanceof LockableTileEntity)
                        for (int i = 0; i < ((LockableTileEntity) tileEntity).getSizeInventory(); i++) {
                            ItemStack stack = ((LockableTileEntity) tileEntity).getStackInSlot(i);
                            if (!stack.isEmpty())
                                standTileEntityEffects.getChestInventory().set(i, stack.copy());
                        }
                }));
    }

    public static void rewindBitesTheDust(){
        bitesTheDustTicks = -1;
        bitesTheDustActive = false;
        bitesTheDustWorld.setDayTime(activatedDayTime);

        for(LivingEntity entity : killedEntities){
            entity.setHealth(entity.getMaxHealth());
            Vector3d position = null;
            for(FatedDeath fatedDeath : fatedDeaths){
                if(fatedDeath.entity.equals(entity)){
                    position = fatedDeath.position;
                }
            }
            if(position != null){
                entity.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
            }
            entity.removed = false;
            bitesTheDustWorld.addEntity(entity);
        }

        for(ItemEntity itemEntity : pickedUpItems){
            itemEntity.removed = false;
            bitesTheDustWorld.addEntity(itemEntity);
        }
        pickedUpItems.clear();



        killedEntities.clear();

        bitesTheDustWorld.getEntities()
                .filter(entity -> !(entity instanceof AbstractStandEntity))
                .forEach(entity -> BitesTheDustCapability.getLazyOptional(entity).ifPresent(bitesTheDustCapability -> {

                    if(bitesTheDustCapability.isEmpty() && !(entity instanceof PlayerEntity)){ //remove all non-player entities that weren't there when BTD started.
                        entity.remove();
                    }else {
                        entity.setPositionAndUpdate(bitesTheDustCapability.getPosX(), bitesTheDustCapability.getPosY(), bitesTheDustCapability.getPosZ());

                        entity.rotationYaw = bitesTheDustCapability.getRotationYaw();
                        entity.rotationPitch = bitesTheDustCapability.getRotationPitch();
                        entity.setRotationYawHead(bitesTheDustCapability.getRotationYawHead());

                        entity.setMotion(0, 0, 0);
                        entity.fallDistance = bitesTheDustCapability.getFallDistance();
                        entity.setFire(bitesTheDustCapability.getFire());

                        if(bitesTheDustCapability.getFateTicks() != 0){
                            StandEffects.getCapabilityFromEntity(entity).setRemainingTicks(bitesTheDustCapability.getFateTicks());
                        }

                        StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                            if (!standEffects.getAlteredTileEntities().isEmpty())
                                standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                        blockPosList.forEach(blockPos -> {
                                            if (bitesTheDustWorld.getChunkProvider().isChunkLoaded(pos))
                                                bitesTheDustWorld.getChunkProvider().forceChunk(pos, true);
                                            TileEntity tileEntity = bitesTheDustWorld.getTileEntity(blockPos);
                                            if (!(tileEntity instanceof LockableTileEntity)) return;
                                            StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                                                ((LockableTileEntity) tileEntity).clear();
                                                for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getChestInventory().get(i);
                                                    ((LockableTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                                                }
                                                tileEntity.markDirty();
                                            });
                                        }));
                            if (!standEffects.getDestroyedBlocks().isEmpty()) {
                                Map<BlockPos, BlockState> removalMap = new ConcurrentHashMap<>();
                                standEffects.getDestroyedBlocks().forEach((pos, list) -> {
                                    list.forEach((blockPos, blockState) -> {
                                        if (bitesTheDustWorld.getChunkProvider().isChunkLoaded(pos))
                                            bitesTheDustWorld.getChunkProvider().forceChunk(pos, true);
                                        bitesTheDustWorld.setBlockState(blockPos, blockState);
                                        removalMap.put(blockPos, blockState);
                                    });
                                    if (!removalMap.isEmpty())
                                        removalMap.forEach(list::remove);
                                });
                            }
                            if (standEffects.getBitesTheDustPos() != BlockPos.ZERO) {
                                entity.setPositionAndUpdate(standEffects.getBitesTheDustPos().getX(), standEffects.getBitesTheDustPos().getY(), standEffects.getBitesTheDustPos().getZ());
                                standEffects.setBitesTheDustPos(BlockPos.ZERO);
                            }
                        });

                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).setHealth(bitesTheDustCapability.getHealth());

                            if (entity instanceof PlayerEntity) {
                                Stand.getLazyOptional(((PlayerEntity) entity)).ifPresent(stand -> {
                                    stand.setBitesTheDustEffectTicker(45);
                                });
                                ((PlayerEntity) entity).getFoodStats().setFoodLevel(bitesTheDustCapability.getHunger());
                                StandPlayerEffects.getLazyOptional((PlayerEntity) entity).ifPresent(standPlayerEffects -> {
                                    ((PlayerEntity) entity).inventory.clear();
                                    for (int i = 0; i < standPlayerEffects.getMainInventory().size(); i++) {
                                        ItemStack stack = standPlayerEffects.getMainInventory().get(i);
                                        ((PlayerEntity) entity).inventory.setInventorySlotContents(i, stack);
                                        standPlayerEffects.getMainInventory().set(i, ItemStack.EMPTY);
                                    }
                                    for (int i = 0; i < standPlayerEffects.getArmorInventory().size(); i++) {
                                        ItemStack stack = standPlayerEffects.getArmorInventory().get(i);
                                        ((PlayerEntity) entity).inventory.setInventorySlotContents(i + 36, stack);
                                        standPlayerEffects.getArmorInventory().set(i, ItemStack.EMPTY);
                                    }
                                    for (int i = 0; i < standPlayerEffects.getOffHandInventory().size(); i++) {
                                        ItemStack stack = standPlayerEffects.getOffHandInventory().get(i);
                                        ((PlayerEntity) entity).inventory.setInventorySlotContents(i + 40, stack);
                                        standPlayerEffects.getOffHandInventory().set(i, ItemStack.EMPTY);
                                    }
                                    for (int i = 0; i < standPlayerEffects.getEnderChestInventory().size(); i++) {
                                        ItemStack stack = standPlayerEffects.getEnderChestInventory().get(i);
                                        ((PlayerEntity) entity).getInventoryEnderChest().setInventorySlotContents(i, stack);
                                        standPlayerEffects.getEnderChestInventory().set(i, ItemStack.EMPTY);
                                    }
                                });
                            }
                        }
                        if (entity instanceof TNTEntity)
                            ((TNTEntity) entity).setFuse(bitesTheDustCapability.getFuse());
                        if (entity instanceof TNTMinecartEntity)
                            ((TNTMinecartEntity) entity).minecartTNTFuse = bitesTheDustCapability.getFuse();
                        if (entity instanceof ItemEntity)
                            ((ItemEntity) entity).age = bitesTheDustCapability.getAge();
                        entity.velocityChanged = true;

                        bitesTheDustCapability.clear();
                    }
                }));

        bitesTheDustWorld.loadedTileEntityList.forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
            if (tileEntity instanceof LockableTileEntity)
                for (int i = 0; i < ((LockableTileEntity) tileEntity).getSizeInventory(); i++) {
                    ItemStack stack = standTileEntityEffects.getChestInventory().get(i);
                    ((LockableTileEntity) tileEntity).setInventorySlotContents(i, stack);
                    standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                }
                tileEntity.markDirty();
        }));

        previousBitesTheDustPlayer = bitesTheDustPlayer;
        bitesTheDustPlayer = null;

    }

    public static void cancelBitesTheDust(){

        pickedUpItems.clear();
        killedEntities.clear();
        activatedDayTime = -1;
        bitesTheDustTicks = -1;
        bitesTheDustActive = false;

        bitesTheDustWorld.getEntities()
                .filter(entity -> !(entity instanceof AbstractStandEntity))
                .forEach(entity -> BitesTheDustCapability.getLazyOptional(entity).ifPresent(bitesTheDustCapability -> {

                    StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                        if (!standEffects.getAlteredTileEntities().isEmpty())
                            standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                    blockPosList.forEach(blockPos -> {
                                        if (bitesTheDustWorld.getChunkProvider().isChunkLoaded(pos))
                                            bitesTheDustWorld.getChunkProvider().forceChunk(pos, true);
                                        TileEntity tileEntity = bitesTheDustWorld.getTileEntity(blockPos);
                                        if (!(tileEntity instanceof LockableTileEntity)) return;
                                        StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                                            ((LockableTileEntity) tileEntity).clear();
                                            for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                                                standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                                            }
                                        });
                                    }));
                        if (!standEffects.getDestroyedBlocks().isEmpty()) {
                            Map<BlockPos, BlockState> removalMap = new ConcurrentHashMap<>();
                            standEffects.getDestroyedBlocks().forEach((pos, list) -> {
                                list.forEach((blockPos, blockState) -> {
                                    if (bitesTheDustWorld.getChunkProvider().isChunkLoaded(pos))
                                        bitesTheDustWorld.getChunkProvider().forceChunk(pos, true);
                                    removalMap.put(blockPos, blockState);
                                });
                                if (!removalMap.isEmpty())
                                    removalMap.forEach(list::remove);
                            });
                        }
                        if (standEffects.getBitesTheDustPos() != BlockPos.ZERO) {
                            standEffects.setBitesTheDustPos(BlockPos.ZERO);
                        }
                    });

                    bitesTheDustCapability.clear();
                }));

        bitesTheDustWorld.loadedTileEntityList.forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
            if (tileEntity instanceof LockableTileEntity)
                for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                    standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                }
        }));

        previousBitesTheDustPlayer = bitesTheDustPlayer;
        bitesTheDustPlayer = null;

    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        ServerWorld world = (ServerWorld) event.world;
        if(event.phase == TickEvent.Phase.END && world.getDimensionKey() == World.OVERWORLD) {
            if (!world.isRemote && bitesTheDustActive && bitesTheDustPlayer != null) {
                bitesTheDustTicks--;
                BitesTheDustCapability.getLazyOptional(bitesTheDustPlayer).ifPresent(bitesTheDustCapability -> {
                    bitesTheDustCapability.setAge(bitesTheDustTicks);
                });
                if(bitesTheDustTicks <= 0){
                    rewindBitesTheDust();
                }
            }
            if(!world.isRemote() && !bitesTheDustActive){
                Iterator<FatedDeath> iterator = fatedDeaths.iterator();
                while (iterator.hasNext()){
                    FatedDeath fatedDeath = iterator.next();
                    fatedDeath.ticksRemaining--;
                    if(fatedDeath.ticksRemaining <= 0){
                        LivingEntity entity = fatedDeath.entity;
                        if (entity instanceof MobEntity) {
                            Explosion explosion = new Explosion(entity.world, previousBitesTheDustPlayer, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 4, true, Explosion.Mode.NONE);
                            ((MobEntity) entity).spawnExplosionParticle();
                            explosion.doExplosionB(true);
                            entity.world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                            entity.remove();
                        } else if (entity instanceof PlayerEntity) {
                            Stand.getLazyOptional((PlayerEntity) entity).ifPresent(bombProps -> {
                                Explosion explosion = new Explosion(entity.world, null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 4, true, Explosion.Mode.NONE);
                                ((PlayerEntity) entity).spawnSweepParticles();
                                explosion.doExplosionB(true);
                                entity.attackEntityFrom(DamageSource.causeExplosionDamage(explosion), Float.MAX_VALUE);
                            });
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

     private static class FatedDeath{
        LivingEntity entity;
        int ticksRemaining;
        Vector3d position;
        public FatedDeath(LivingEntity entityIn, int ticksRemainingIn, Vector3d positionIn){
            entity = entityIn;
            ticksRemaining = ticksRemainingIn;
            position = positionIn;
        }
     }

     public static void addFatedDeath(LivingEntity entity, Vector3d position){
        fatedDeaths.add(new FatedDeath(entity, (BITES_THE_DUST_DURATION - bitesTheDustTicks), position));
     }

     @SubscribeEvent
     public static void itemPickup(PlayerEvent.ItemPickupEvent event){
        if(bitesTheDustActive){
            pickedUpItems.add(event.getOriginalEntity());
            event.getOriginalEntity().removed = false;
        }
     }
}
