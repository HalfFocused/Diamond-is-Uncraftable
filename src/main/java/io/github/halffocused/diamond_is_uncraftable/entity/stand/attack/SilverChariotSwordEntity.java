package io.github.halffocused.diamond_is_uncraftable.entity.stand.attack;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
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
        if(!world.isRemote()) {
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity) {
                Util.dealStandDamage(shootingStand, (LivingEntity) entity, 18, Vector3d.ZERO, false);
            }
            entity.hurtResistantTime = 0;
        }
    }

    @Override
    protected void onBlockHit(BlockRayTraceResult result) {
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
