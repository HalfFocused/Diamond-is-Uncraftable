package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.*;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.SheerHeartAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.*;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.ChargeAttackFormat;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import software.bernie.geckolib3.core.IAnimatable;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ConstantConditions")
public class KillerQueenEntity extends AbstractStandEntity implements IAnimatable, IOnMasterAttacked {
    public LivingEntity bombEntity;
    public int shaCount;
    BlockPos setBlockBombPosition = null;

    /*
    Balancing variables for KQ's item and block bomb explosion. It happens all over the code in many different places, so these variables should allow easy tweaking.
     */

    public static final double maxExplosionDamage = 15;
    public static final double minExplosionDamage = 5;
    public static final double explosionRange = 7;

    AttackFramedata leftPunchData = new AttackFramedata()
            .addDamageFrame(13, 8, Vector3d.ZERO, 2.3, 2)
            .setAttackDuration(20);

    AttackFramedata rightPunchData = new AttackFramedata()
            .addDamageFrame(13, 8, Vector3d.ZERO, 2.3, 2)
            .setAttackDuration(20);

    AttackFramedata barrageData = new AttackFramedata()
            .generateInterval(6, 30, 3, 2, Vector3d.ZERO, 2.4, 4)
            .setAttackDuration(32);

    AttackFramedata bombData = new AttackFramedata()
            .addBombFrame(8, 3.2)
            .setAttackDuration(14);

    AttackFramedata detonateData = new AttackFramedata()
            .addMessageFrame(39, 1, null, null)
            .addMessageFrame(32, 5, null, null)
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
            .addMove("Jab",1, rightPunchData, "rightpunch", 2.0)
            .addMove("Jab",2, leftPunchData, "leftpunch", 2.0)
            .addMove("Barrage",3, barrageData, "barrage", HoveringMoveHandler.RepositionConstants.MASTER_POSITION)
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
            SheerHeartAttackEntity sheerHeartAttackEntity = new SheerHeartAttackEntity(world, this, master);
            sheerHeartAttackEntity.setPosition(master.getPosX(), master.getPosY() + master.getEyeHeight(), master.getPosZ());
            sheerHeartAttackEntity.shoot(getMaster(), master.rotationPitch, master.rotationYaw, 0.35f, Float.MIN_VALUE);
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
                        Vector3d executeVec = getPositionVec().add(getLookVec().normalize().scale(1.3));
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
            Stand stand = Stand.getCapabilityFromPlayer(master);
            Stand.getLazyOptional(master).ifPresent(props -> { //Remove bomb block if it was mined.
                if (world.getBlockState(stand.getBlockPos()).isAir(world, stand.getBlockPos())) {
                    stand.setBlockPos(BlockPos.ZERO);
                }

                    for(ServerWorld currentWorld : getServer().getWorlds()){

                        currentWorld.getEntities()
                            .filter(entity -> entity instanceof ItemEntity)
                            .forEach(entity -> {
                                ItemEntity itemEntity = ((ItemEntity) entity);
                                CompoundNBT nbt = itemEntity.getItem().getOrCreateTag();
                                if (nbt.getBoolean("bomb") && nbt.getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                    Util.standExplosion(master, this.world, entity.getPositionVec(), explosionRange, 3, maxExplosionDamage, minExplosionDamage);

                                    entity.remove();
                                }
                            });
                    }
                    if (stand.getBlockPos() != BlockPos.ZERO) {
                        if (!world.getChunkProvider().isChunkLoaded(world.getChunkAt(stand.getBlockPos()).getPos()))
                            return;
                        Util.standExplosion(master, this.world, new Vector3d(stand.getBlockPos().getX(), stand.getBlockPos().getY(), stand.getBlockPos().getZ()), explosionRange, 3, maxExplosionDamage, minExplosionDamage);
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
                                }
                            } else if (bombEntity instanceof PlayerEntity) {
                                Stand.getLazyOptional((PlayerEntity) bombEntity).ifPresent(bombProps -> {
                                    Explosion explosion = new Explosion(bombEntity.world, master, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 4, true, Explosion.Mode.NONE);
                                    ((PlayerEntity) bombEntity).spawnSweepParticles();
                                    explosion.doExplosionB(true);
                                    Util.spawnParticle(this, 5, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 1, 1, 1, 1);
                                    Util.spawnParticle(this, 14, bombEntity.getPosX(), bombEntity.getPosY() + 1, bombEntity.getPosZ(), 1, 1, 1, 20);
                                    Util.dealStandDamage(this, bombEntity, 15, Vector3d.ZERO, false);
                                    removeFirstBombFromAll();
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
                        bombEntity.attackEntityFrom(DamageSource.causeExplosionDamage(master), Float.MAX_VALUE);
                    } else {
                        world.playSound(null, master.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                        Util.spawnParticle(this, 5, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 1, 1, 1, 1);
                        Util.spawnParticle(this, 14, bombEntity.getPosX(), bombEntity.getPosY() + 1, bombEntity.getPosZ(), 1, 1, 1, 20);
                        bombEntity.remove();
                    }
                }
                bombEntity = null;
                removeFirstBombFromAll();
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
    public void removeFirstBombFromAll() {

        Stand stand = Stand.getCapabilityFromPlayer(master);
        stand.setBlockPos(BlockPos.ZERO);
        stand.setBombEntityId(0);
        bombEntity = null;

        /*

        AtomicInteger processes = new AtomicInteger();

        bombEntity = null;
        Stand stand = Stand.getCapabilityFromPlayer(master);
        stand.setBombEntityId(0);

        for (ServerWorld currentWorld : getServer().getWorlds()) {
            currentWorld.loadedTileEntityList
                    .forEach(tile -> {

                        processes.getAndIncrement();
                        if(processes.get() > 1000){
                            return;
                        }
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
                    .forEach(entity -> {

                                processes.getAndIncrement();
                                if (processes.get() > 1000) {
                                    return;
                                }

                                StandEffects.getLazyOptional(entity).ifPresent(effects -> {
                                    effects.setBomb(false);
                                    ItemStack item = ((ItemEntity) entity).getItem();

                                    CompoundNBT nbt = item.getOrCreateTag();
                                    if (nbt.getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                        nbt.remove("bomb");
                                        nbt.remove("ownerUUID");
                                        item.clearCustomName();
                                    }
                                });
                            });

            currentWorld.getEntities()
                    .filter(entity -> entity instanceof LivingEntity)
                    .forEach(entity -> {

                        processes.getAndIncrement();
                        if (processes.get() > 1000) {
                            return;
                        }

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

                                processes.getAndIncrement();
                                if (processes.get() > 1000) {
                                    return;
                                }

                                if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                    CompoundNBT nbt = stack.getOrCreateTag();
                                    playerEntity.inventory.markDirty();
                                    nbt.remove("bomb");
                                    nbt.remove("ownerUUID");
                                    stack.clearCustomName();
                                }
                            }
                            for (ItemStack stack : playerEntity.inventory.offHandInventory) {

                                processes.getAndIncrement();
                                if (processes.get() > 1000) {
                                    return;
                                }

                                if (stack.getOrCreateTag().getBoolean("bomb") && stack.getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                                    CompoundNBT nbt = stack.getOrCreateTag();
                                    playerEntity.inventory.markDirty();
                                    nbt.remove("bomb");
                                    nbt.remove("ownerUUID");
                                    stack.clearCustomName();
                                }
                            }
                            for (ItemStack stack : playerEntity.inventory.armorInventory) {

                                processes.getAndIncrement();
                                if (processes.get() > 1000) {
                                    return;
                                }

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

                                processes.getAndIncrement();
                                if (processes.get() > 1000) {
                                    return;
                                }

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

                        processes.getAndIncrement();
                        if (processes.get() > 1000) {
                            return;
                        }

                        ItemFrameEntity frame = (ItemFrameEntity) itemFrame;
                        if (frame.getDisplayedItem().getOrCreateTag().getBoolean("bomb") && frame.getDisplayedItem().getOrCreateTag().getUniqueId("ownerUUID").equals(master.getUniqueID())) {
                            CompoundNBT nbt = frame.getDisplayedItem().getOrCreateTag();
                            nbt.remove("bomb");
                            nbt.remove("ownerUUID");
                            frame.getDisplayedItem().clearCustomName();
                        }
                    });
        }
        stand.setBombEntityId(0);
        stand.setBlockPos(BlockPos.ZERO);

         */
    }
}

