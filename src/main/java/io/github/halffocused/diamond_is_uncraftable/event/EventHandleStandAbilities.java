package io.github.halffocused.diamond_is_uncraftable.event;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.*;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.*;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.SheerHeartAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.event.custom.StandAttackEvent;
import io.github.halffocused.diamond_is_uncraftable.event.custom.StandEvent;
import io.github.halffocused.diamond_is_uncraftable.init.EffectInit;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.init.ItemInit;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SSyncStandMasterPacket;
import io.github.halffocused.diamond_is_uncraftable.util.IOnMasterAttacked;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.globalabilities.BitesTheDustHelper;
import io.github.halffocused.diamond_is_uncraftable.util.globalabilities.TimestopHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.github.halffocused.diamond_is_uncraftable.util.globalabilities.BitesTheDustHelper.*;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = DiamondIsUncraftable.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandleStandAbilities {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            PlayerEntity player = event.player;
            Stand.getLazyOptional(player).ifPresent(stand -> {

                if(stand.getInstantTimeStopFrame() > 0){
                    stand.setInstantTimeStopFrame(stand.getInstantTimeStopFrame() - 1);
                }
                if(stand.getTimeSkipEffectTicker() > 0){
                    stand.setTimeSkipEffectTicker(stand.getTimeSkipEffectTicker() - 1);
                }
                if(stand.getBitesTheDustEffectTicker() > 0){
                    stand.setBitesTheDustEffectTicker(stand.getBitesTheDustEffectTicker() - 1);
                }

                Random rand = player.world.rand;
                int standID = stand.getStandID();
                boolean standOn = stand.getStandOn();
                double cooldown = stand.getCooldown();
                double timeLeft = stand.getTimeLeft();
                double invulnerableTicks = stand.getInvulnerableTicks();

                if (!player.world.isRemote && stand.getGameTime() != -1 && stand.getGameTime() < player.world.getGameTime() - 24000) {
                    stand.setGameTime(-1);
                    stand.setDayTime(-1);
                    player.setHealth(player.getMaxHealth());
                    player.world.loadedTileEntityList.stream()
                            .filter(tileEntity -> tileEntity instanceof LockableTileEntity && !tileEntity.getWorld().isRemote)
                            .forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                                ((LockableTileEntity) tileEntity).clear();
                                if (tileEntity instanceof ChestTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getChestInventory().get(i);
                                        ((ChestTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                                    }
                                else if (tileEntity instanceof AbstractFurnaceTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getFurnaceInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getFurnaceInventory().get(i);
                                        ((AbstractFurnaceTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getFurnaceInventory().set(i, ItemStack.EMPTY);
                                    }
                                else if (tileEntity instanceof BrewingStandTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getBrewingInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getBrewingInventory().get(i);
                                        ((BrewingStandTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getBrewingInventory().set(i, ItemStack.EMPTY);
                                    }
                                else if (tileEntity instanceof BarrelTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getBarrelInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getBarrelInventory().get(i);
                                        ((BarrelTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getBarrelInventory().set(i, ItemStack.EMPTY);
                                    }
                                else if (tileEntity instanceof DispenserTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getDispenserInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getDispenserInventory().get(i);
                                        ((DispenserTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getDispenserInventory().set(i, ItemStack.EMPTY);
                                    }
                                else if (tileEntity instanceof HopperTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getHopperInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getHopperInventory().get(i);
                                        ((HopperTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getHopperInventory().set(i, ItemStack.EMPTY);
                                    }
                                else if (tileEntity instanceof ShulkerBoxTileEntity)
                                    for (int i = 0; i < standTileEntityEffects.getShulkerBoxInventory().size(); i++) {
                                        ItemStack stack = standTileEntityEffects.getShulkerBoxInventory().get(i);
                                        ((ShulkerBoxTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                        standTileEntityEffects.getShulkerBoxInventory().set(i, ItemStack.EMPTY);
                                    }
                                tileEntity.markDirty();
                            }));
                    player.getServer().getWorld(player.world.getDimensionKey()).getEntities().forEach(entity -> {
                        if (entity instanceof PlayerEntity && !entity.world.isRemote)
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
                            });
                        StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                            if (!standEffects.getAlteredTileEntities().isEmpty())
                                standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                        blockPosList.forEach(blockPos -> {
                                            if (entity.world.getChunkProvider().isChunkLoaded(pos))
                                                entity.world.getChunkProvider().forceChunk(pos, true);
                                            TileEntity tileEntity = entity.world.getTileEntity(blockPos);
                                            if (!(tileEntity instanceof LockableTileEntity)) return;
                                            StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                                                ((LockableTileEntity) tileEntity).clear();
                                                if (tileEntity instanceof ChestTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getChestInventory().get(i);
                                                        ((ChestTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                else if (tileEntity instanceof AbstractFurnaceTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getFurnaceInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getFurnaceInventory().get(i);
                                                        ((AbstractFurnaceTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getFurnaceInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                else if (tileEntity instanceof BrewingStandTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getBrewingInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getBrewingInventory().get(i);
                                                        ((BrewingStandTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getBrewingInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                else if (tileEntity instanceof BarrelTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getBarrelInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getBarrelInventory().get(i);
                                                        ((BarrelTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getBarrelInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                else if (tileEntity instanceof DispenserTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getDispenserInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getDispenserInventory().get(i);
                                                        ((DispenserTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getDispenserInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                else if (tileEntity instanceof HopperTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getHopperInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getHopperInventory().get(i);
                                                        ((HopperTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getHopperInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                else if (tileEntity instanceof ShulkerBoxTileEntity)
                                                    for (int i = 0; i < standTileEntityEffects.getShulkerBoxInventory().size(); i++) {
                                                        ItemStack stack = standTileEntityEffects.getShulkerBoxInventory().get(i);
                                                        ((ShulkerBoxTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                        standTileEntityEffects.getShulkerBoxInventory().set(i, ItemStack.EMPTY);
                                                    }
                                                tileEntity.markDirty();
                                            });
                                        }));
                            if (standEffects.isShouldBeRemoved())
                                entity.remove();
                            if (entity instanceof ItemEntity && standEffects.getBitesTheDustPos() == BlockPos.ZERO)
                                entity.remove();
                            if (!standEffects.getDestroyedBlocks().isEmpty()) {
                                Map<BlockPos, BlockState> removalMap = new ConcurrentHashMap<>();
                                standEffects.getDestroyedBlocks().forEach((pos, list) -> {
                                    list.forEach((blockPos, blockState) -> {
                                        if (player.world.getChunkProvider().isChunkLoaded(pos))
                                            player.world.getChunkProvider().forceChunk(pos, true);
                                        player.world.setBlockState(blockPos, blockState);
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
                    });
                    stand.setCooldown(36000);
                }

                if (invulnerableTicks > 0) {
                    stand.setInvulnerableTicks(stand.getInvulnerableTicks() - 0.5);
                    for (int i = 0; i < 10; i++)
                        player.world.addOptionalParticle(
                                ParticleTypes.DRAGON_BREATH,
                                player.getPosX() + (player.world.rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()),
                                player.getPosY() + player.world.rand.nextDouble(),
                                player.getPosZ() + (player.world.rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()),
                                0, 0.3 + (rand.nextBoolean() ? 0.1 : -0.1), 0);
                    if (invulnerableTicks == 0.5)
                        stand.setCooldown(140);
                }

                if (stand.getEnergyCooldown() == 0) {
                    stand.setCurrentStandEnergy(Math.min(stand.getCurrentStandEnergy() + (stand.getEnergyRegenerationRate() / 20.0f), stand.getMaxStandEnergy()));
                } else {
                    stand.setEnergyCooldown(Math.max(0, stand.getEnergyCooldown() - 1));
                }

                if (standOn) {
                    stand.setStandUnsummonedTime(0);

                } else {
                    stand.setStandUnsummonedTime(Math.min(stand.getStandUnsummonedTime() + 1, (20 * 60 * 5))); //Shouldn't need to know more than like 5 minutes.
                }
                stand.setReflexCooldown(Math.max(0, stand.getReflexCooldown() - 1));
                stand.setInvincibleTicks(Math.max(0, stand.getInvincibleTicks() - 1));
                if(!player.world.isRemote){
                    if(stand.getStandID() == Util.StandID.KILLER_QUEEN) {
                    BlockPos.getAllInBox(player.getPosition().add(15, 15, 15), player.getPosition().add(-15, -15, -15))
                            .forEach(blockPos -> {
                                if (player.world.getTileEntity(blockPos) != null) {
                                    if (player.world.getTileEntity(blockPos) instanceof LockableTileEntity) {
                                        LockableTileEntity tile = (LockableTileEntity) player.world.getTileEntity(blockPos);
                                        for (int i = 0; i < tile.getSizeInventory(); i++) {
                                            CompoundNBT nbt = tile.getStackInSlot(i).getOrCreateTag();
                                            if (nbt.getBoolean("bomb") && nbt.getUniqueId("ownerUUID").equals(player.getUniqueID())) {
                                                Util.spawnClientParticle((ServerPlayerEntity) player, 17, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 1, 2, 1, 1);
                                            }
                                        }
                                    }
                                }
                            });

                        BlockPos.getAllInBox(player.getPosition().add(10, 10, 10), player.getPosition().add(-10, -10, -10))
                                .filter(blockPos -> blockPos.equals(stand.getBlockPos()))
                                .filter(blockPos -> !blockPos.equals(BlockPos.ZERO))
                                .forEach(blockPos -> {
                                    Util.spawnClientParticle((ServerPlayerEntity) player, 8, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ()+ 0.5, 1, 2, 1, 1);
                                });


                        player.world.getServer().getWorld(player.world.getDimensionKey()).getEntities()
                            .filter(entity -> entity instanceof LivingEntity)
                            .filter(entity -> !entity.equals(player))
                            .filter(entity -> entity.getDistance(player) < 20)
                            .forEach(entity -> {

                                if(entity.getEntityId() == stand.getBombEntityId()){
                                    Util.spawnClientParticle((ServerPlayerEntity) player, 8, entity.getPosX(), entity.getPosY() + 0.5, entity.getPosZ(), 1, 2, 1, 1);
                                }

                                if(entity instanceof PlayerEntity){
                                    boolean hasItemBomb = false;
                                    for(ItemStack stack : ((PlayerEntity) entity).inventory.mainInventory){
                                        if(stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(player.getUniqueID())){
                                            hasItemBomb = true;
                                        }
                                    }
                                    for(ItemStack stack : ((PlayerEntity) entity).inventory.offHandInventory){
                                        if(stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(player.getUniqueID())){
                                            hasItemBomb = true;
                                        }
                                    }
                                    for(ItemStack stack : ((PlayerEntity) entity).inventory.armorInventory){
                                        if(stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(player.getUniqueID())){
                                            hasItemBomb = true;
                                        }
                                    }
                                    if(hasItemBomb){
                                        Util.spawnClientParticle((ServerPlayerEntity) player, 17, entity.getPosX(), entity.getPosY() + 0.5, entity.getPosZ(), 1, 2, 1, 1);
                                    }
                                    PlayerEntity playerEntity = (PlayerEntity) entity;

                                    for (int i = 0; i < playerEntity.getInventoryEnderChest().getSizeInventory(); i++) {
                                        if (playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().getBoolean("bomb") && playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().getUniqueId("ownerUUID").equals(player.getUniqueID())) {
                                            Util.spawnClientParticle((ServerPlayerEntity) player, 18, entity.getPosX(), entity.getPosY() + 0.5, entity.getPosZ(), 1, 2, 1, 1);
                                        }
                                    }
                                }else {
                                    LivingEntity livingEntity = (LivingEntity) entity;
                                    for (ItemStack stack : livingEntity.getEquipmentAndArmor()){
                                        CompoundNBT nbt = stack.getOrCreateTag();
                                        if(nbt.getBoolean("bomb") && nbt.getUniqueId("ownerUUID").equals(player.getUniqueID())){
                                            Util.spawnClientParticle((ServerPlayerEntity) player, 17, livingEntity.getPosX(), livingEntity.getPosY() + 0.5, livingEntity.getPosZ(), 1, 2, 1, 1);
                                        }
                                    }
                                }
                            });
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void blockDestroyed(BlockEvent.BreakEvent event) {
        LivingEntity livingEntity = event.getPlayer();
        if (livingEntity == null) return;
        Chunk chunk = livingEntity.world.getChunkAt(livingEntity.getPosition());
        StandEffects.getLazyOptional(livingEntity).ifPresent(standEffects -> {
            if (standEffects.getDestroyedBlocks().containsKey(chunk.getPos()) && standEffects.getDestroyedBlocks().get(chunk.getPos()).containsKey(event.getPos()))
                return;
            if (BitesTheDustHelper.bitesTheDustActive)
                standEffects.putDestroyedBlock(chunk.getPos(), event.getPos(), event.getState());
        });
    }

    @SubscribeEvent
    public static void blockPlaced(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;
        Chunk chunk = entity.world.getChunkAt(entity.getPosition());
        StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
            if (standEffects.getDestroyedBlocks().containsKey(chunk.getPos()) && standEffects.getDestroyedBlocks().get(chunk.getPos()).containsKey(event.getPos()))
                return;
            if (BitesTheDustHelper.bitesTheDustActive)
                standEffects.putDestroyedBlock(chunk.getPos(), event.getPos(), Blocks.AIR.getDefaultState());

        });
    }

    @SubscribeEvent
    public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if (player == null) return;
        Chunk chunk = player.world.getChunkAt(player.getPosition());
        if (event.getUseBlock() == Event.Result.ALLOW && player.world.getTileEntity(event.getPos()) != null)
            StandEffects.getLazyOptional(player).ifPresent(standEffects -> {
                if (BitesTheDustHelper.bitesTheDustActive)
                    standEffects.putAlteredTileEntity(chunk.getPos(), event.getPos());
            });
    }

    @SubscribeEvent
    public static void livingDeath(LivingDeathEvent event) {

        if(!event.getEntityLiving().getEntityWorld().isRemote() && event.getEntityLiving() instanceof PlayerEntity){
            PlayerEntity player = ((PlayerEntity) event.getEntityLiving());
            if(bitesTheDustPlayer != null) {
                if (bitesTheDustPlayer.equals(player)) {
                    if (bitesTheDustActive){
                        event.setCanceled(true);
                        player.setHealth(4f);
                        rewindBitesTheDust();
                    }
                }else if(previousBitesTheDustPlayer != null && previousBitesTheDustPlayer.equals(player)){
                    if(!bitesTheDustActive){
                        fatedDeaths.clear();
                    }
                }
            }
        }
        
        if(!event.getEntity().world.isRemote() && event.getEntity() instanceof LivingEntity){
            if(BitesTheDustHelper.bitesTheDustActive && !killedEntities.contains(((LivingEntity) event.getEntity()))){
                BitesTheDustCapability bitesTheDustCapability = BitesTheDustCapability.getCapabilityFromEntity(event.getEntity());
                Vector3d originPos = new Vector3d(bitesTheDustCapability.getPosX(), bitesTheDustCapability.getPosY(), bitesTheDustCapability.getPosZ());
                BitesTheDustHelper.addFatedDeath(((LivingEntity) event.getEntity()), originPos);

                if(!(event.getEntity() instanceof PlayerEntity)){
                    killedEntities.add((LivingEntity) event.getEntity());
                    event.getEntity().remove();
                    event.getEntity().removed = false;
                }
            }
        }

        if(!event.getEntity().world.isRemote() && event.getEntity() instanceof PlayerEntity){
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) event.getEntity();
            Stand.getLazyOptional(playerEntity).ifPresent(props -> {


                //This is completely safe to call on players who cannot stop time. All it does is remove any timestopped chunks matching the player's UUID.
                TimestopHelper.endTimeStop(playerEntity);

                playerEntity.getServerWorld().getEntities()
                        .filter(entity -> entity instanceof SheerHeartAttackEntity)
                        .filter(entity -> ((SheerHeartAttackEntity) entity).standMaster != null)
                        .filter(entity -> ((SheerHeartAttackEntity) entity).standMaster.equals(playerEntity))
                        .forEach(Entity::remove);

                playerEntity.getServerWorld().getEntities()
                        .filter(entity -> entity instanceof AbstractStandEntity)
                        .filter(entity -> ((AbstractStandEntity) entity).getMaster().equals(playerEntity))
                        .forEach(entity -> {
                            if (entity instanceof KingCrimsonEntity) {
                                if (((KingCrimsonEntity) entity).timeEraseActive) {
                                    ((KingCrimsonEntity) entity).endTimeSkip();
                                }
                            }
                            entity.remove();
                        });
            });
        }

        if(event.getEntity() instanceof PlayerEntity && event.getSource() != null &&event.getSource().getTrueSource() != null){

            PlayerEntity player = (PlayerEntity) event.getEntity();

            Stand stand = Stand.getCapabilityFromPlayer(player);

            if(stand.getStandID() == Util.StandID.THE_WORLD && !stand.getStandOn() && stand.getReflexCooldown() == 0 && stand.getStandUnsummonedTime() > (20 * 60)){
                player.setHealth(4f);
                event.setCanceled(true);
                stand.setReflexCooldown(20 * 60 * 10);
                stand.setInvincibleTicks(40);
                stand.setCurrentStandEnergy(50);
                stand.setEnergyCooldown(40);
                stand.setStandUnsummonedTime(0);
                stand.setInstantTimeStopFrame(TheWorldEntity.QUICK_STOP_DURATION);

                stand.setStandOn(true);

                AbstractStandEntity standEntity = Util.StandID.getStandByID(stand.getStandID(), player.world);
                Vector3d position = player.getLookVec().mul(0.5, 1, 0.5).add(player.getPositionVec()).add(0, 0.5, 0);
                standEntity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), player.rotationYaw, player.rotationPitch);
                standEntity.setMaster(player);
                standEntity.setMasterUUID(player.getUniqueID());
                standEntity.followMaster();
                if (DiamondIsUncraftableConfig.CLIENT.playStandSpawnSounds.get())
                    standEntity.playSpawnSound();
                DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new SSyncStandMasterPacket(standEntity.getEntityId(), player.getEntityId()));
                player.world.addEntity(standEntity);

                player.getServer().getWorld(player.world.getDimensionKey()).getEntities()
                        .filter(taunted -> !taunted.equals(player))
                        .filter(taunted -> taunted instanceof LivingEntity)
                        .filter(taunted -> taunted.getDistance(player) <= 7)
                        .forEach(taunted -> {
                            Vector3d modifiedPositionVec = new Vector3d(player.getPosX(), taunted.getPosY(), player.getPosZ());
                            Vector3d vec = taunted.getPositionVec().subtract(modifiedPositionVec).normalize();
                            Util.teleportUntilWall((LivingEntity) taunted, vec, 7);

                            if(taunted instanceof PlayerEntity){
                                Util.applyUnactionableTicks(((PlayerEntity) taunted), 20);

                                Stand.getLazyOptional(((PlayerEntity) taunted)).ifPresent(effect -> {
                                    effect.setInstantTimeStopFrame(TheWorldEntity.QUICK_STOP_DURATION);
                                });
                            }
                        });

            }else {
                stand.setMomentum(0);
                stand.setRage(0);
                stand.setPreventUnsummon(false);
                stand.setPreventUnsummon2(false);
                stand.setStandOn(false);
            }
        }

        if(event.isCanceled()){
            if(event.getEntity() instanceof ServerPlayerEntity && !event.getEntity().world.isRemote()){
                ServerPlayerEntity playerEntity = (ServerPlayerEntity) event.getEntity();
                Util.giveAdvancement(playerEntity, "secondchance");
            }
        }
    }

    @SubscribeEvent
    public static void effectRemovedEvent(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() == Effects.GLOWING) event.getEntityLiving().setGlowing(false);
    }

    @SubscribeEvent
    public static void standUnsummoned(StandEvent.StandUnsummonedEvent event) {
        PlayerEntity player = event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> {
            player.setInvulnerable(false);
            player.setNoGravity(false);
            if (!player.isCreative() && !player.isSpectator())
                player.setGameType(GameType.SURVIVAL);
        });
    }

    @SubscribeEvent
    public static void standPunchEntityEvent(StandAttackEvent.EntityHit event) {

    }

    @SubscribeEvent
    public static void standPunchBlockEvent(StandAttackEvent.BlockHit event) {

    }

    @SubscribeEvent
    public static void cancelDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof PlayerEntity) {
            Stand.getLazyOptional((PlayerEntity) entity).ifPresent(stand -> {

                if(stand.getExperiencingTimeSkip()){
                    event.setCanceled(true);
                    return;
                }

                if (!entity.world.isRemote() && stand.getStandOn()) {
                    boolean shouldCancelDamage = false;
                    AtomicReference<AbstractStandEntity> standEntity = new AtomicReference<>();

                    entity.getServer().getWorld(entity.world.getDimensionKey()).getEntities()
                            .filter(entity1 -> entity1 instanceof AbstractStandEntity)
                            .filter(entity1 -> ((AbstractStandEntity) entity1).getMaster() != null)
                            .filter(entity1 -> ((AbstractStandEntity) entity1).getMaster().equals(entity))
                            .forEach(entity1 -> standEntity.set((AbstractStandEntity) entity1));

                    if (standEntity.get() instanceof IOnMasterAttacked) {
                        Entity source = event.getSource().getTrueSource();
                        if (source != null) {

                            IOnMasterAttacked counterStand = ((IOnMasterAttacked) standEntity.get());
                            counterStand.onMasterAttacked(source, event.getAmount());
                            shouldCancelDamage = stand.getCounterBuffer();
                            stand.setCounterBuffer(false);

                            if(shouldCancelDamage && source instanceof LivingEntity){
                                ((LivingEntity) source).addPotionEffect(new EffectInstance(Effects.BLINDNESS, 20, 1));
                                entity.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 20, 1));
                            }
                        }
                    }
                    event.setCanceled(shouldCancelDamage);

                    if(!entity.world.isRemote() && stand.getInvincibleTicks() > 0){
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void itemPickup(EntityItemPickupEvent event) {
        ItemEntity entity = event.getItem();
        if (entity == null) return;
        if (entity.world.isRemote) return;
        StandEffects.getLazyOptional(entity).ifPresent(props -> {
            if (props.isBomb()) {
                PlayerEntity player = entity.world.getPlayerByUuid(props.getStandUser());
                if (player != null) {
                    Util.standExplosion(player, player.world, entity.getPositionVec(), KillerQueenEntity.explosionRange, 3, KillerQueenEntity.maxExplosionDamage, KillerQueenEntity.minExplosionDamage);
                }
                entity.remove();
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void itemExpire(ItemExpireEvent event) {
        ItemEntity entity = event.getEntityItem();
        if (entity == null) return;
        if (entity.getItem().getOrCreateTag().getBoolean("bomb"))
            StandEffects.getLazyOptional(entity).ifPresent(props -> {
                PlayerEntity player = entity.world.getPlayerByUuid(props.getStandUser());
                Stand.getLazyOptional(player).ifPresent(stand -> stand.setAbilityUseCount(0));
            });
    }

    @SubscribeEvent
    public static void noClip(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity)
            Stand.getLazyOptional((PlayerEntity) event.getEntityLiving()).ifPresent(props -> {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                if (!player.isSpectator())
                    player.noClip = props.getNoClip();
                else
                    player.noClip = true;
            });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof World)) return;
        World world = (World) event.getWorld();
        if (world.isRemote) return;
        Chunk chunk = world.getChunkAt(event.getPos());
        StandChunkEffects.getLazyOptional(chunk).ifPresent(props -> {
            props.getSoundEffects().forEach((uuid, list) -> {
                if (list.size() > 0) {
                    List<BlockPos> removalList = new ArrayList<>();
                    list.forEach(blockPos -> {
                        if (blockPos.equals(event.getPos())) {
                            PlayerEntity player = world.getPlayerByUuid(uuid);
                            Util.activateEchoesAbility(world, event.getPlayer(), event.getPos(), props, player, removalList);
                        }
                    });
                    list.removeAll(removalList);
                }
            });
        });
    }

    @SubscribeEvent
    public static void explosionEvent(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        if (world == null || world.isRemote) return;
        event.getAffectedBlocks().forEach(blockPos ->
                StandChunkEffects.getLazyOptional(world.getChunkAt(blockPos)).ifPresent(props -> {
                    props.getBombs().forEach((uuid, pos) -> {
                        if (pos.equals(blockPos)) {
                            PlayerEntity player = world.getPlayerByUuid(uuid);
                            Stand.getLazyOptional(player).ifPresent(stand -> {
                                stand.setBlockPos(BlockPos.ZERO);
                                stand.setAbilityUseCount(0);
                            });
                            props.removeBombPos(player);
                        }
                    });
                    props.getSoundEffects().forEach((uuid, list) -> {
                        if (list.size() > 0) {
                            List<BlockPos> removalList = new ArrayList<>();
                            list.forEach(pos -> {
                                if (pos.equals(blockPos)) {
                                    PlayerEntity player = world.getPlayerByUuid(uuid);
                                    Stand.getLazyOptional(player).ifPresent(stand -> stand.setAbilityUseCount(stand.getAbilityUseCount() - 1));
                                    removalList.add(pos);
                                }
                            });
                            list.removeAll(removalList);
                        }
                    });
                }));
    }

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof AbstractStandEntity) return;
        StandEffects.getLazyOptional(entity).ifPresent(props -> {

            if (props.getSoundEffect() != 0) {
                if (entity.world.rand.nextInt(40) == 1) {
                    SoundEvent soundEvent = SoundEvents.ENTITY_GENERIC_EXPLODE;
                    switch (props.getSoundEffect()) {
                        case 1: {
                            soundEvent = SoundEvents.BLOCK_ANVIL_BREAK;
                            break;
                        }
                        case 2: {
                            soundEvent = SoundEvents.BLOCK_BELL_USE;
                            break;
                        }
                        case 3: {
                            soundEvent = SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST;
                            break;
                        }
                        case 4: {
                            soundEvent = SoundEvents.ENTITY_GENERIC_BURN;
                            break;
                        }
                        case 6: {
                            soundEvent = SoundEvents.BLOCK_GLASS_BREAK;
                            break;
                        }
                        case 7: {
                            soundEvent = SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON;
                            break;
                        }
                    }
                    if (props.getSoundEffect() != 2 && props.getSoundEffect() % 2 == 0) {
                        for (int i = 0; i < 2; i++) {
                            entity.world.playSound(null, entity.getPosition(), soundEvent, SoundCategory.BLOCKS, 1, 1);
                            entity.attackEntityFrom(DamageSource.DROWN, 2);
                        }
                    } else {
                        entity.world.playSound(null, entity.getPosition(), soundEvent, SoundCategory.BLOCKS, 1, 1);
                        entity.attackEntityFrom(DamageSource.DROWN, 2);
                    }
                }
            }


            if (props.isRotating()) {
                entity.rotationYaw += 30;
                if (entity.rotationYaw > 180)
                    entity.rotationYaw = -180;
                entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, 2);
            }
            if (props.isThreeFreeze()) {
                PlayerEntity playerEntity = entity.world.getPlayerByUuid(props.getStandUser());
                if (playerEntity == null || !playerEntity.isAlive()) return;
                float distance = entity.getDistance(playerEntity);
                if (distance < 2)
                    entity.setMotion(0, -1000, 0);
                else if (distance < 3)
                    entity.setMotion(0, -100, 0);
                else if (distance < 6)
                    entity.setMotion(0, -50, 0);
                else if (distance < 10)
                    entity.setMotion(0, -10, 0);
                else if (distance < 15)
                    entity.setMotion(0, -5, 0);
                else
                    return;
                entity.velocityChanged = true;
            }
            if (props.getTimeNearFlames() > 0) {
                entity.setFire((int) (props.getTimeNearFlames() * 2));
                props.setTimeNearFlames(props.getTimeNearFlames() - 0.25);
                if (entity.world.rand.nextInt(6) == 1)
                    entity.attackEntityFrom(DamageSource.IN_FIRE, 2);
            }
        });

        StandChunkEffects.getLazyOptional(entity.world.getChunkAt(entity.getPosition())).ifPresent(props ->
                props.getSoundEffects().forEach((uuid, list) -> {
                    if (list.size() > 0) {
                        List<BlockPos> removalList = new ArrayList<>();
                        list.forEach(blockPos -> {
                            if (blockPos.equals(entity.getPosition().add(0, -1, 0)))
                                Util.activateEchoesAbility(entity.world, entity, blockPos, props, entity.world.getPlayerByUuid(uuid), removalList);
                        });
                        list.removeAll(removalList);
                    }
                }));
    }



}