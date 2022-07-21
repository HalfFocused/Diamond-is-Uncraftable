package io.github.halffocused.diamond_is_uncraftable.entity.stand.attack;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SilverChariotSwordEntity extends AbstractStandAttackEntity {
    public SilverChariotSwordEntity(EntityType<? extends AbstractStandAttackEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public SilverChariotSwordEntity(World worldIn, AbstractStandEntity shooter, PlayerEntity player) {
        super(EntityInit.SILVER_CHARIOT_SWORD.get(), worldIn, shooter, player);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        Entity entity = result.getEntity();
        if(entity instanceof LivingEntity){
            Util.dealStandDamage(shootingStand, (LivingEntity) entity, 18, Vec3d.ZERO, false, null);
        }
        entity.hurtResistantTime = 0;
    }

    @Override
    protected void onBlockHit(BlockRayTraceResult result) {
        BlockPos pos = result.getPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlockHardness(world, pos) != -1 && state.getBlockHardness(world, pos) < 3) {
            world.removeBlock(pos, false);
            if (world.rand.nextBoolean())
                state.getBlock().harvestBlock(world, standMaster, pos, state, null, standMaster.getActiveItemStack());
        }
    }

    @Override
    public ResourceLocation getEntityTexture() {
        return Util.ResourceLocations.SILVER_CHARIOT_SWORD;
    }

    @Override
    protected int getRange() {
        return 100;
    }
}
