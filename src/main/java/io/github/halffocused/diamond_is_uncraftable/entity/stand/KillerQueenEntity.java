package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.*;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.ChargeAttackFormat;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import software.bernie.geckolib3.core.IAnimatable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ConstantConditions")
public class KillerQueenEntity extends AbstractStandEntity implements IAnimatable, IOnMasterAttacked {
    public LivingEntity bombEntity;
    protected int shaCount;
    BlockPos setBlockBombPosition = null;

    /*
    Balancing variables for KQ's item and block bomb explosion. It happens all over the code in many different places, so these variables should allow easy tweaking.
     */

    public static final double maxExplosionDamage = 15;
    public static final double minExplosionDamage = 5;
    public static final double explosionRange = 7;

    MoveEffects punchEffectsHolder = new MoveEffects(3, null, null);

    AttackFramedata leftPunchData = new AttackFramedata()
            .addDamageFrame(13, 8, Vec3d.ZERO, 2.3, 2)
            .setAttackDuration(20);

    AttackFramedata rightPunchData = new AttackFramedata()
            .addDamageFrame(13, 8, Vec3d.ZERO, 2.3, 2)
            .setAttackDuration(20);

    AttackFramedata barrageData = new AttackFramedata()
            .generateInterval(6, 30, 3, 2, Vec3d.ZERO, 2.4, 4)
            .setAttackDuration(32);

    AttackFramedata bombData = new AttackFramedata()
            .addBombFrame(8, 3.2)
            .setAttackDuration(14);

    AttackFramedata detonateData = new AttackFramedata()
            .addMessageFrame(39, 1, null, null)
            .addMessageFrame(29, 5, null, null)
            .setAttackDuration(43);

    AttackFramedata blockBombData = new AttackFramedata()
            .addMessageFrame(20, 2, null, null)
            .setAttackDuration(54);

    AttackFramedata itemBombData = new AttackFramedata()
            .addMessageFrame(12, 3, null, null)
            .setAttackDuration(45);

    AttackFramedata executeData = new AttackFramedata()
            .addMessageFrame(42, 4, null, null)
            .addMenacingFrame(42)
            .setAttackDuration(50);

    ChargeAttackFormat chargeAttackFormat = new ChargeAttackFormat("firstbombcharge", 54, "holding")
            .addChargeNode(54, 4, true);

    HoveringMoveHandler controller = new HoveringMoveHandler(this)
            .addMove("Jab",1, rightPunchData, "rightpunch", 2.0, punchEffectsHolder)
            .addMove("Jab",2, leftPunchData, "leftpunch", 2.0, punchEffectsHolder)
            .addMove("Barrage",3, barrageData, "barrage", HoveringMoveHandler.RepositionConstants.MASTER_POSITION, punchEffectsHolder)
            .addMove("First Bomb (Entity)",4, bombData,"firstbomb", 2.0)
            .addMove("Detonate",5, detonateData, "click", HoveringMoveHandler.RepositionConstants.IDLE_POSITION)
            .addMove("First Bomb (Block)",6, blockBombData, "blockbomb", HoveringMoveHandler.RepositionConstants.DO_NOT_MOVE)
            .addMove("First Bomb (Item)",7, itemBombData, "itembomb", HoveringMoveHandler.RepositionConstants.IDLE_POSITION)
            .addMove("Eliminate Witness", 8, executeData, "execute", HoveringMoveHandler.RepositionConstants.DO_NOT_MOVE)
            .addChargeAttack(chargeAttackFormat);

    public HoveringMoveHandler getController(){
        return controller;
    }

    public KillerQueenEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public void detonate() {
        if (getMaster() == null || world.isRemote()) return;
        if(spendEnergy(65)) { //In conditional statements like this all other checks must precede the spendEnergy() method.
            controller.setMoveActive(5);
        }
    }

    public void toggleSheerHeartAttack() {

        if(!world.isRemote() && this.shaCount == 0 && spendEnergy(50)) {
            SheerHeartAttackEntity sheerHeartAttackEntity = new SheerHeartAttackEntity(world, this, master, 100);
            sheerHeartAttackEntity.randomizePositions();
            sheerHeartAttackEntity.shoot(getMaster(), rotationPitch, rotationYaw, 0.5f, Float.MIN_VALUE);
            world.addEntity(sheerHeartAttackEntity);
            shaCount++;
        }

    }

    public void turnItemOrBlockIntoBomb() {
        if (getMaster() == null || world.isRemote) return;
        Stand.getLazyOptional(master).ifPresent(props -> {
            if (!master.isCrouching() && master.getHeldItemMainhand() != ItemStack.EMPTY) {
                controller.setMoveActive(7);
            } else if (master.isCrouching()) {
                setBlockBombPosition = master.getPosition().add(0,-1,0);
                controller.setMoveActive(6);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(stand -> {
                stand.setAbility(false);
                stand.setPreventUnsummon2(shaCount > 0);
            });
        }

        if(controller.isMoveActive()){
            if(controller.getActiveMove().getId() == 6){
                setPosition(setBlockBombPosition.getX(), setBlockBombPosition.getY() + 1, setBlockBombPosition.getZ());
            }

            if(controller.getActiveMove().getId() == 8){

                if(bombEntity != null) {

                    if(!bombEntity.isAlive()){
                        bombEntity = null;
                    }else{
                        Vec3d executeVec = getPositionVec().add(getLookVec().normalize().scale(1.3));
                        bombEntity.setPositionAndUpdate(executeVec.x, this.getPosY(), executeVec.z);
                        if(bombEntity instanceof PlayerEntity){
                            Util.applyUnactionableTicks((PlayerEntity) bombEntity, 1);
                        }
                    }
                }
            }

            if(controller.getActiveMove().getId() == 5 && controller.getActiveMove().getFramedata().getTicker() <= 39){
                Util.spawnParticle(this, 8, this.getPosX(), this.getPosY(), this.getPosZ(), 1.2, 2.5, 1.2, 1);
            }
        }

    }



    private void beginBitesTheDust(){
        Stand stand = Stand.getCapabilityFromPlayer(master);

        if (master.isCrouching() && stand.getGameTime() == -1) {
            stand.setGameTime(world.getGameTime());
            stand.setDayTime(world.getDayTime());
            world.loadedTileEntityList
                    .forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                        if (tileEntity instanceof ChestTileEntity)
                            for (int i = 0; i < ((ChestTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((ChestTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getChestInventory().set(i, stack.copy());
                            }
                        else if (tileEntity instanceof AbstractFurnaceTileEntity)
                            for (int i = 0; i < ((AbstractFurnaceTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((AbstractFurnaceTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getFurnaceInventory().set(i, stack.copy());
                            }
                        else if (tileEntity instanceof BrewingStandTileEntity)
                            for (int i = 0; i < ((BrewingStandTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((BrewingStandTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getBrewingInventory().set(i, stack.copy());
                            }
                        else if (tileEntity instanceof BarrelTileEntity)
                            for (int i = 0; i < ((BarrelTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((BarrelTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getBarrelInventory().set(i, stack.copy());
                            }
                        else if (tileEntity instanceof DispenserTileEntity)
                            for (int i = 0; i < ((DispenserTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((DispenserTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getDispenserInventory().set(i, stack.copy());
                            }
                        else if (tileEntity instanceof HopperTileEntity)
                            for (int i = 0; i < ((HopperTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((HopperTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getHopperInventory().set(i, stack.copy());
                            }
                        else if (tileEntity instanceof ShulkerBoxTileEntity)
                            for (int i = 0; i < ((ShulkerBoxTileEntity) tileEntity).getSizeInventory(); i++) {
                                ItemStack stack = ((ShulkerBoxTileEntity) tileEntity).getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standTileEntityEffects.getShulkerBoxInventory().set(i, stack.copy());
                            }
                    }));
            getServer().getWorld(dimension).getEntities().forEach(entity -> {
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
                StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                    standEffects.setBitesTheDustPos(entity.getPosition());
                    standEffects.setStandUser(master.getUniqueID());
                });
            });
            master.sendStatusMessage(new StringTextComponent("Set checkpoint for\u00A7e Bites the Dust\u00A7f!"), true);
        } else if (stand.getGameTime() != -1) {
            world.setGameTime(stand.getGameTime());
            world.setDayTime(stand.getDayTime());
            stand.setGameTime(-1);
            stand.setDayTime(-1);
            master.setHealth(master.getMaxHealth());
            world.loadedTileEntityList.stream()
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
            getServer().getWorld(dimension).getEntities().forEach(entity -> {
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
                        for (int i = 0; i < standPlayerEffects.getEnderChestInventory().size(); i++) {
                            ItemStack stack = standPlayerEffects.getEnderChestInventory().get(i);
                            ((PlayerEntity) entity).getInventoryEnderChest().setInventorySlotContents(i, stack);
                            standPlayerEffects.getEnderChestInventory().set(i, ItemStack.EMPTY);
                        }
                    });
                StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                    if (!standEffects.getAlteredTileEntities().isEmpty())
                        standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                blockPosList.forEach(blockPos -> {
                                    if (world.getChunkProvider().isChunkLoaded(pos))
                                        world.getChunkProvider().forceChunk(pos, true);
                                    TileEntity tileEntity = world.getTileEntity(blockPos);
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
                                if (world.getChunkProvider().isChunkLoaded(pos))
                                    world.getChunkProvider().forceChunk(pos, true);
                                world.setBlockState(blockPos, blockState);
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
    }

    private void detonateBTDMob(){

        Stand stand = Stand.getCapabilityFromPlayer(master);


        Explosion explosion = new Explosion(bombEntity.world, master, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 4, true, Explosion.Mode.NONE);
        ((MobEntity) bombEntity).spawnExplosionParticle();
        explosion.doExplosionB(true);
        world.playSound(null, master.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);

        StandEffects.getLazyOptional(bombEntity).ifPresent(standEffects -> standEffects.setTimeOfDeath(world.getGameTime()));
        world.setGameTime(stand.getGameTime());
        world.setDayTime(stand.getDayTime());
        stand.setGameTime(-1);
        stand.setDayTime(-1);
        master.setHealth(master.getMaxHealth());
        world.loadedTileEntityList.stream()
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
        getServer().getWorld(dimension).getEntities().forEach(entity -> {
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
                    for (int i = 0; i < standPlayerEffects.getEnderChestInventory().size(); i++) {
                        ItemStack stack = standPlayerEffects.getEnderChestInventory().get(i);
                        ((PlayerEntity) entity).getInventoryEnderChest().setInventorySlotContents(i, stack);
                        standPlayerEffects.getEnderChestInventory().set(i, ItemStack.EMPTY);
                    }
                });
            StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                if (!standEffects.getAlteredTileEntities().isEmpty())
                    standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                            blockPosList.forEach(blockPos -> {
                                if (world.getChunkProvider().isChunkLoaded(pos))
                                    world.getChunkProvider().forceChunk(pos, true);
                                TileEntity tileEntity = world.getTileEntity(blockPos);
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
                            if (world.getChunkProvider().isChunkLoaded(pos))
                                world.getChunkProvider().forceChunk(pos, true);
                            world.setBlockState(blockPos, blockState);
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


    @Override
    public int getJabMoveId(){
        return getRNG().nextBoolean() ? 1 : 2;
    }

    @Override
    public int getBarrageMoveId(){
        return 3;
    }

    @Override
    public void messageFrame(int message1, Object message2, Object message3) {
        if (message1 == 1) {
            for (ItemStack stack : master.inventory.mainInventory) {
                if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    master.inventory.markDirty();
                    nbt.remove("bomb");
                    nbt.remove("ownerUUID");
                    stack.clearCustomName();
                }
            }
            for (ItemStack stack : master.inventory.offHandInventory) {
                if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    master.inventory.markDirty();
                    nbt.remove("bomb");
                    nbt.remove("ownerUUID");
                    stack.clearCustomName();
                }
            }
            for (ItemStack stack : master.inventory.armorInventory) {
                if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                    CompoundNBT nbt = stack.getOrCreateTag();
                    master.inventory.markDirty();
                    nbt.remove("bomb");
                    nbt.remove("ownerUUID");
                    stack.clearCustomName();
                }
            }

            Stand stand = Stand.getCapabilityFromPlayer(master);
            if (stand.getCooldown() == 0 && master.isCrouching() && !world.isRemote && stand.getAbilitiesUnlocked() > 1 && master.dimension == DimensionType.OVERWORLD) {
                beginBitesTheDust();
                return;
            }
            Stand.getLazyOptional(master).ifPresent(props -> { //Remove bomb block if it was mined.
                if (world.getBlockState(stand.getBlockPos()).isAir(world, stand.getBlockPos())) {
                    removeFirstBombFromAll();
                }

                    for(ServerWorld currentWorld : getServer().getWorlds()){

                        currentWorld.loadedTileEntityList
                                .forEach(tile -> {
                                    if(tile instanceof LockableTileEntity && !((LockableTileEntity) tile).isEmpty()){
                                        int size = ((LockableTileEntity) tile).getSizeInventory();
                                        for(int i = 0; i < size; i++){
                                            if(((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().getBoolean("bomb") && ((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())){
                                                tile.markDirty();
                                                Util.standExplosion(master, this.world, new Vec3d(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ()), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                                ((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().remove("bomb");
                                                ((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().remove("ownerUUID");
                                                ((LockableTileEntity) tile).getStackInSlot(i).setCount(0);
                                            }
                                        }
                                    }
                                });

                        currentWorld.getEntities()
                                .filter(entity -> entity instanceof ItemEntity)
                                .forEach(entity ->
                                        StandEffects.getLazyOptional(entity).ifPresent(effects -> {
                                            if (effects.isBomb()) {
                                                PlayerEntity player = world.getPlayerByUuid(effects.getStandUser());
                                                if (player != null && player.equals(master)) {

                                                    Util.standExplosion(master, this.world, entity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);

                                                    entity.remove();
                                                }
                                            }
                                        })
                                );
                        currentWorld.getEntities()
                                .filter(entity -> entity instanceof LivingEntity)
                                .forEach(entity -> {
                                    if (entity instanceof PlayerEntity) {
                                        PlayerEntity playerEntity = (PlayerEntity) entity;

                                        for(int i = 0; i < playerEntity.getInventoryEnderChest().getSizeInventory(); i++){
                                            if(playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().getBoolean("bomb") && playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())){
                                                playerEntity.inventory.markDirty();
                                                playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().remove("bomb");
                                                playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().remove("ownerUUID");
                                                playerEntity.getInventoryEnderChest().getStackInSlot(i).setCount(0);
                                                Util.standExplosion(master, this.world, playerEntity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                            }
                                        }

                                        for (ItemStack stack : playerEntity.inventory.mainInventory) {
                                            if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                                playerEntity.inventory.markDirty();
                                                Util.standExplosion(master, this.world, entity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                                stack.getOrCreateTag().remove("bomb");
                                                stack.getOrCreateTag().remove("ownerUUID");
                                                stack.setCount(0);
                                            }
                                        }
                                        for (ItemStack stack : playerEntity.inventory.offHandInventory) {
                                            if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                                playerEntity.inventory.markDirty();
                                                Util.standExplosion(master, this.world, entity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                                stack.getOrCreateTag().remove("bomb");
                                                stack.getOrCreateTag().remove("ownerUUID");
                                                stack.setCount(0);
                                            }
                                        }
                                        for (ItemStack stack : playerEntity.inventory.armorInventory) {
                                            if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                                playerEntity.inventory.markDirty();
                                                Util.standExplosion(master, this.world, entity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                                stack.getOrCreateTag().remove("bomb");
                                                stack.getOrCreateTag().remove("ownerUUID");
                                                stack.setCount(0);
                                            }
                                        }
                                    }else{
                                        for (ItemStack stack : entity.getEquipmentAndArmor()){
                                            CompoundNBT nbt = stack.getOrCreateTag();
                                            if(nbt.getBoolean("bomb") && nbt.getUniqueId("ownerUUID").equals(master.getUniqueID())){
                                                Util.standExplosion(master, this.world, entity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                                stack.getOrCreateTag().remove("bomb");
                                                stack.getOrCreateTag().remove("ownerUUID");
                                                stack.setCount(0);
                                            }
                                        }
                                    }
                                });

                        currentWorld.getEntities()
                                .filter(entity -> entity instanceof ItemFrameEntity)
                                .forEach(itemFrame -> {
                                    ItemFrameEntity frame = (ItemFrameEntity) itemFrame;
                                    if(frame.getDisplayedItem().getOrCreateTag().getBoolean("bomb") && frame.getDisplayedItem().getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())){
                                        Util.standExplosion(master, this.world, frame.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                                        frame.setDisplayedItem(ItemStack.EMPTY);
                                    }
                                });
                    }
                    if (stand.getBlockPos() != BlockPos.ZERO) {
                        if (!world.getChunkProvider().isChunkLoaded(world.getChunkAt(stand.getBlockPos()).getPos()))
                            return;
                        Util.standExplosion(master, this.world, new Vec3d(stand.getBlockPos().getX(), stand.getBlockPos().getY(), stand.getBlockPos().getZ()), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
                        StandChunkEffects.getLazyOptional(world.getChunkAt(master.getPosition())).ifPresent(standChunkEffects -> standChunkEffects.removeBombPos(master));
                        removeFirstBombFromAll();
                    }

                    if (bombEntity != null) {
                        if (bombEntity.isAlive()) {
                            if (bombEntity instanceof MobEntity) {
                                if (stand.getGameTime() == -1) {
                                    Util.standExplosionFX(master, this.world, bombEntity.getPositionVec());
                                    bombEntity.remove();
                                    removeFirstBombFromAll();
                                } else {
                                    detonateBTDMob();
                                }
                            } else if (bombEntity instanceof PlayerEntity) {
                                Stand.getLazyOptional((PlayerEntity) bombEntity).ifPresent(bombProps -> {
                                    if (bombProps.getStandID() != Util.StandID.GER) {
                                        Explosion explosion = new Explosion(bombEntity.world, master, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 4, true, Explosion.Mode.NONE);
                                        ((PlayerEntity) bombEntity).spawnSweepParticles();
                                        explosion.doExplosionB(true);
                                        Util.spawnParticle(this, 5, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 1, 1, 1, 1);
                                        Util.spawnParticle(this, 14, bombEntity.getPosX(), bombEntity.getPosY() + 1, bombEntity.getPosZ(), 1, 1, 1, 20);
                                        Util.dealStandDamage(this, bombEntity, 15, Vec3d.ZERO, false, null);
                                        removeFirstBombFromAll();
                                    } else {
                                        Explosion explosion = new Explosion(master.world, master, master.getPosX(), master.getPosY(), master.getPosZ(), 4, true, Explosion.Mode.NONE);
                                        master.world.setEntityState(master, (byte) 20);
                                        explosion.doExplosionB(true);
                                        master.setHealth(0);
                                    }
                                });
                            }
                            if (!master.isCreative() && !master.isSpectator())
                                master.getFoodStats().addStats(-2, 0);
                        }
                    }
            });
        }

        if(message1 == 2){
            removeFirstBombFromAll();
            Stand props = Stand.getCapabilityFromPlayer(master);
            props.setBlockPos(setBlockBombPosition);
            StandChunkEffects.getLazyOptional(world.getChunkAt(master.getPosition())).ifPresent(standChunkEffects -> standChunkEffects.addBombPos(master, setBlockBombPosition));
            props.setAbilityUseCount(1);
        }

        if(message1 == 3){
            removeFirstBombFromAll();
            Stand props = Stand.getCapabilityFromPlayer(master);
            if (master.getHeldItemMainhand().getCount() > 1) {
                if (master.inventory.getStackInSlot(master.inventory.getBestHotbarSlot()).isEmpty()) {
                    ItemStack stack = master.getHeldItemMainhand().copy();
                    stack.shrink(master.getHeldItemMainhand().getCount() - 1);
                    stack.getOrCreateTag().putBoolean("bomb", true);
                    stack.getOrCreateTag().putUniqueId("ownerUUID", master.getUniqueID());
                    master.getHeldItemMainhand().shrink(1);
                    stack.setDisplayName(new StringTextComponent("Bomb"));
                    master.inventory.add(master.inventory.getBestHotbarSlot(), stack);
                }
            } else if (master.getHeldItemMainhand().getCount() == 1) {
                master.getHeldItemMainhand().getOrCreateTag().putBoolean("bomb", true);
                master.getHeldItemMainhand().getOrCreateTag().putUniqueId("ownerUUID", master.getUniqueID());
                master.getHeldItemMainhand().setDisplayName(new StringTextComponent("Bomb"));
            }
        }


        if(message1 == 4) {
            if(!world.isRemote()) {
                if (bombEntity != null) {
                    if (bombEntity instanceof PlayerEntity) {
                        Util.spawnParticle(this, 5, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 1, 1, 1, 1);
                        Util.spawnParticle(this, 14, bombEntity.getPosX(), bombEntity.getPosY() + 1, bombEntity.getPosZ(), 1, 1, 1, 20);
                        world.playSound(null, master.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                    } else {
                        world.playSound(null, master.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                        Util.spawnParticle(this, 5, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 1, 1, 1, 1);
                        Util.spawnParticle(this, 14, bombEntity.getPosX(), bombEntity.getPosY() + 1, bombEntity.getPosZ(), 1, 1, 1, 20);
                        bombEntity.setPosition(bombEntity.getPosX(), -100, bombEntity.getPosZ());
                    }
                    bombEntity.attackEntityFrom(DamageSource.causeExplosionDamage(master), Float.MAX_VALUE);
                    removeFirstBombFromAll();
                }
            }
        }

        if(message1 == 5){
            world.playSound(null, this.getPosition(), SoundInit.DETONATION_CLICK.get(), SoundCategory.NEUTRAL, 2f, 1);
        }

    }

    @Override
    public void onMasterAttacked(Entity damager, float damage) {
        if(controller.isMoveActive()){
            if(controller.getActiveMove().getId() == 5){
                controller.cancelActiveMoves();
            }
        }
    }

    /*
     Removes first bomb status from any entity, block, or item. Items have their names reset.
     */
    public void removeFirstBombFromAll(){
            bombEntity = null;
            Stand stand = Stand.getCapabilityFromPlayer(master);
            stand.setBombEntityId(0);

            for (ServerWorld currentWorld : getServer().getWorlds()) {

                currentWorld.loadedTileEntityList
                        .forEach(tile -> {
                            if (tile instanceof LockableTileEntity && !((LockableTileEntity) tile).isEmpty()) {
                                int size = ((LockableTileEntity) tile).getSizeInventory();
                                for (int i = 0; i < size; i++) {
                                    if (((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().getBoolean("bomb") && ((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        tile.markDirty();
                                        ((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().remove("bomb");
                                        ((LockableTileEntity) tile).getStackInSlot(i).getOrCreateTag().remove("ownerUUID");
                                        ((LockableTileEntity) tile).getStackInSlot(i).clearCustomName();
                                    }
                                }
                            }
                        });

                currentWorld.getEntities()
                        .filter(entity -> entity instanceof ItemEntity)
                        .forEach(entity ->
                                StandEffects.getLazyOptional(entity).ifPresent(effects -> {
                                    effects.setBomb(false);
                                    ItemStack item = ((ItemEntity) entity).getItem();

                                    CompoundNBT nbt = item.getOrCreateTag();
                                    if (nbt.getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        nbt.remove("bomb");
                                        item.clearCustomName();
                                    }
                                }));

                currentWorld.getEntities()
                        .filter(entity -> entity instanceof LivingEntity)
                        .forEach(entity -> {
                            if (entity instanceof PlayerEntity) {
                                PlayerEntity playerEntity = (PlayerEntity) entity;

                                for (int i = 0; i < playerEntity.getInventoryEnderChest().getSizeInventory(); i++) {
                                    if (playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().getBoolean("bomb") && playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        playerEntity.getInventoryEnderChest().markDirty();
                                        playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().remove("bomb");
                                        playerEntity.getInventoryEnderChest().getStackInSlot(i).getOrCreateTag().remove("ownerUUID");
                                        playerEntity.getInventoryEnderChest().getStackInSlot(i).clearCustomName();
                                    }
                                }

                                for (ItemStack stack : playerEntity.inventory.mainInventory) {
                                    if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        CompoundNBT nbt = stack.getOrCreateTag();
                                        playerEntity.inventory.markDirty();
                                        nbt.remove("bomb");
                                        nbt.remove("ownerUUID");
                                        stack.clearCustomName();
                                    }
                                }
                                for (ItemStack stack : playerEntity.inventory.offHandInventory) {
                                    if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        CompoundNBT nbt = stack.getOrCreateTag();
                                        playerEntity.inventory.markDirty();
                                        nbt.remove("bomb");
                                        nbt.remove("ownerUUID");
                                        stack.clearCustomName();
                                    }
                                }
                                for (ItemStack stack : playerEntity.inventory.armorInventory) {
                                    if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        CompoundNBT nbt = stack.getOrCreateTag();
                                        playerEntity.inventory.markDirty();
                                        nbt.remove("bomb");
                                        nbt.remove("ownerUUID");
                                        stack.clearCustomName();
                                    }
                                }
                            } else {
                                for (ItemStack stack : entity.getEquipmentAndArmor()) {
                                    CompoundNBT nbt = stack.getOrCreateTag();
                                    if (nbt.getBoolean("bomb") && nbt.getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        nbt.remove("bomb");
                                        nbt.remove("ownerUUID");
                                        stack.clearCustomName();
                                    }
                                }
                            }
                        });
                currentWorld.getEntities()
                        .filter(entity -> entity instanceof ItemFrameEntity)
                        .forEach(itemFrame -> {
                            ItemFrameEntity frame = (ItemFrameEntity) itemFrame;
                            if(frame.getDisplayedItem().getOrCreateTag().getBoolean("bomb") && frame.getDisplayedItem().getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())){
                                CompoundNBT nbt = frame.getDisplayedItem().getOrCreateTag();
                                nbt.remove("bomb");
                                nbt.remove("ownerUUID");
                                frame.getDisplayedItem().clearCustomName();
                            }
                        });
            }
            stand.setBombEntityId(0);
            stand.setBlockPos(BlockPos.ZERO);
        }
}

