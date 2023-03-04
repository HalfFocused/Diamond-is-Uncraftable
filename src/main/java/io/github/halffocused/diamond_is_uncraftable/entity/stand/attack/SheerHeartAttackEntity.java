package io.github.halffocused.diamond_is_uncraftable.entity.stand.attack;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenEntity;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SheerHeartAttackEntity extends AbstractStandAttackEntity {

    LivingEntity target = null;
    private int firstFuse = 100;
    private int secondFuse = 40;

    public SheerHeartAttackEntity(EntityType<? extends AbstractStandAttackEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public SheerHeartAttackEntity(World worldIn, KillerQueenEntity shooter, PlayerEntity player) {
        super(EntityInit.SHEER_HEART_ATTACK.get(), worldIn, shooter, player);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        if(!world.isRemote()) {
            firstFuse = 0;
            setVelocity(0,0,0);
            setMotion(0,0,0);
        }
    }

    @Override
    public void tick(){
        super.tick();
        if(!world.isRemote()) {
            firstFuse--;
            Util.spawnParticle(shootingStand, 13, this.getPosX(), this.getPosY() + 1, this.getPosZ(), 0.5, 0.5, 0.5, 1);
            if (firstFuse <= 0) {
                setVelocity(0, 0, 0);
                setMotion(0, 0, 0);
                secondFuse--;
                Util.spawnParticle(shootingStand, 14, this.getPosX(), this.getPosY() + 1, this.getPosZ(), 0.4, 0.4, 0.4, 2);
                if (secondFuse <= 0) {
                    Util.standExplosion(standMaster, standMaster.world, new Vector3d(this.getPositionVec().getX(), this.getPositionVec().getY() + 1, this.getPositionVec().getZ()), 6.5, 3, 5, 23);
                    remove();
                }
            }else{
                if(target == null) {
                    getServer().getWorld(world.getDimensionKey()).getEntities()
                            .filter(entity -> !entity.equals(standMaster))
                            .filter(entity -> entity instanceof LivingEntity)
                            .filter(entity -> !(entity instanceof AbstractStandEntity))
                            .filter(entity -> entity.getDistance(this) < 7)
                            .filter(Entity::isAlive)
                            .filter(entity -> ((LivingEntity) entity).canEntityBeSeen(this))
                            .forEach(entity -> target = (LivingEntity) entity);
                }

                if(target != null) {
                    double x = (target.getBoundingBox().minX + (target.getBoundingBox().maxX - target.getBoundingBox().minX) / 2) - getPosX();
                    double y = (target.getBoundingBox().minY + (target.getBoundingBox().maxY - target.getBoundingBox().minY) / 2) - getPosY();
                    double z = (target.getBoundingBox().minZ + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ) / 2) - getPosZ();
                    Vector3d vec3d = (new Vector3d(x, y, z)).normalize().add(rand.nextGaussian() * (double) 0.0075f * (double) 0, rand.nextGaussian() * (double) 0.0075f * 0, rand.nextGaussian() * (double) 0.0075F * 0).scale(1);
                    setMotion(vec3d.scale(0.2));
                    float f = MathHelper.sqrt(horizontalMag(vec3d));
                    rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180 / (float) Math.PI));
                    rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (double) (180 / (float) Math.PI));
                    prevRotationYaw = rotationYaw;
                    prevRotationPitch = rotationPitch;
                }

            }
        }
    }

    @Override
    protected void onBlockHit(BlockRayTraceResult result) {
        if(!world.isRemote()) {
            firstFuse = 0;
            setVelocity(0,0,0);
            setMotion(0,0,0);
        }
    }

    @Override
    public ResourceLocation getEntityTexture() {
        return Util.ResourceLocations.SHEER_HEART_ATTACK;
    }

    @Override
    protected int getRange() {
        return 10000;
    }

    @Override
    public void remove(){
        if(shootingStand instanceof KillerQueenEntity){
            ((KillerQueenEntity) shootingStand).shaCount--;
        }
        super.remove();
    }

    @Override
    public boolean shouldRemoveOnHitEntity(){
        return false;
    }

    protected boolean shouldBeDestroyedByBlocks(BlockState state) {
        return false;
    }
}
