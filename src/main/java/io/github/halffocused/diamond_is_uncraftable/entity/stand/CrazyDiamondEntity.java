package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.CrazyDiamondPunchEntity;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.HoveringMoveHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ConstantConditions")
public class CrazyDiamondEntity extends AbstractStandEntity {
    public CrazyDiamondEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    public HoveringMoveHandler getController(){
        return null;
    }

    public void repair() {
        if (getMaster() == null) return;
        Stand.getLazyOptional(getMaster()).ifPresent(stand -> {
            if (stand.getCooldown() > 0)
                return;
            if (!stand.getCrazyDiamondBlocks().isEmpty()) {
                Map<BlockPos, BlockState> removalMap = new ConcurrentHashMap<>();
                stand.getCrazyDiamondBlocks().forEach((pos, list) -> {
                    list.forEach((blockPos, blockState) -> {
                        if (world.getChunkProvider().isChunkLoaded(pos))
                            world.getChunkProvider().forceChunk(pos, true);
                        world.setBlockState(blockPos, blockState);
                        removalMap.put(blockPos, blockState);
                    });
                    if (!removalMap.isEmpty())
                        removalMap.forEach((list::remove));
                });
                world.playSound(null, new BlockPos(getPosX(), getPosY(), getPosZ()), SoundInit.SPAWN_CRAZY_DIAMOND.get(), getSoundCategory(), 1.0f, 1.0f);
                stand.setCooldown(100);
            }
        });
    }

    @Override
    public void playSpawnSound() {
        world.playSound(null, getMaster().getPosition(), getSpawnSound(), SoundCategory.NEUTRAL, 2, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();
                if (props.getCooldown() > 0 && ability)
                    props.setCooldown(props.getCooldown() - 1);
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
                        CrazyDiamondPunchEntity crazyDiamond1 = new CrazyDiamondPunchEntity(world, this, master);
                        crazyDiamond1.randomizePositions();
                        crazyDiamond1.shoot(master, master.rotationPitch, master.rotationYaw, 2.5f, 0.1f);
                        world.addEntity(crazyDiamond1);
                        CrazyDiamondPunchEntity crazyDiamond2 = new CrazyDiamondPunchEntity(world, this, master);
                        crazyDiamond2.randomizePositions();
                        crazyDiamond2.shoot(master, master.rotationPitch, master.rotationYaw, 2.5f, 0.1f);
                        world.addEntity(crazyDiamond2);
                    }
                if (attackTicker >= 100) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
