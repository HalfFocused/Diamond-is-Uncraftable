package io.github.halffocused.diamond_is_uncraftable.entity.stand;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.AbstractStandAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.init.EntityInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.timestop.TimestopHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class SheerHeartAttackEntity extends AbstractStandAttackEntity {
    private KillerQueenEntity masterStand;
    private PlayerEntity master;
    private int detonationTime = 35;
    boolean detonating = false;
    LivingEntity target = null;
    boolean stalled = false;
    boolean hasStalledOnce = false;
    int masterFuse;

    public SheerHeartAttackEntity(EntityType<? extends AbstractStandAttackEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public SheerHeartAttackEntity(World worldIn, AbstractStandEntity shooter, PlayerEntity player, int fuse) {
        super(EntityInit.SHEER_HEART_ATTACK.get(), worldIn, shooter, player);
        standMaster = player;
        masterStand = (KillerQueenEntity) shooter;
        masterFuse = fuse;
    }



    public PlayerEntity getMaster() {
        return master;
    }


    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        if(result.getEntity() instanceof LivingEntity) {
            LivingEntity hitEntity = (LivingEntity) result.getEntity();
            detonating = true;
        }else{
            remove();
        }
    }

    @Override
    protected void onBlockHit(BlockRayTraceResult result) {

        if(world.getBlockState(result.getPos()).isSolid()) {
            stalled = true;
        }
    }

    @Override
    public ResourceLocation getEntityTexture() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if(!world.isRemote()) {
            if (detonating) {
                setNoGravity(world.getBlockState(new BlockPos(this.getPosX(), this.getPosY() + 1, this.getPosZ())).isSolid());
                if(!TimestopHelper.isTimeStopped(world, this)) {
                    detonationTime = Math.max(0, detonationTime - 1);
                }
                setMotion(0, this.getMotion().getY(), 0);
                Util.spawnParticle(masterStand, 14, this.getPosX(), this.getPosY() + 1, this.getPosZ(), 0.4, 0.4, 0.4, 2);

                double verticalAdjustment = Util.heightAboveGround(world, this.getPositionVec());
                int circleDots = 32;
                int circleRadius = 8;
                for(int i = 0; i < circleDots; i++){
                    double radians = ((Math.PI * 2.0) / circleDots) * i;
                    double xOffset = Math.sin(radians) * circleRadius;
                    double zOffset = Math.cos(radians) * circleRadius;
                    Util.spawnParticle(masterStand, 15, this.getPosX() + xOffset, this.getPosY() - verticalAdjustment, this.getPosZ() + zOffset, 0.1, 0.1, 0.1, 1);
                }
            }else{

                Util.spawnParticle(masterStand, 13, this.getPosX(), this.getPosY() + 1, this.getPosZ(), 0.5, 0.5, 0.5, 1);

                if(stalled){
                    setNoGravity(world.getBlockState(new BlockPos(this.getPosX(), this.getPosY() + 1, this.getPosZ())).isSolid());
                    setMotion(0, this.getMotion().getY(), 0);
                    if(!TimestopHelper.isTimeStopped(world, this)) {
                        masterFuse -= 2; //After hitting the ground, SHA fuses 3x as fast.
                    }
                }
                if(target == null) {
                    getServer().getWorld(dimension).getEntities()
                            .filter(entity -> !entity.equals(standMaster))
                            .filter(entity -> entity instanceof LivingEntity)
                            .filter(entity -> !(entity instanceof AbstractStandEntity))
                            .filter(entity -> entity.getDistance(this) < 7)
                            .filter(Entity::isAlive)
                            .filter(entity -> ((LivingEntity) entity).canEntityBeSeen(this))
                            .forEach(entity -> target = (LivingEntity) entity);
                }

                if(target != null){
                    stalled = false;
                }

                if(target != null && !detonating) {
                    stalled = false;
                    double x = (target.getBoundingBox().minX + (target.getBoundingBox().maxX - target.getBoundingBox().minX) / 2) - getPosX();
                    double y = (target.getBoundingBox().minY + (target.getBoundingBox().maxY - target.getBoundingBox().minY) / 2) - getPosY();
                    double z = (target.getBoundingBox().minZ + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ) / 2) - getPosZ();
                    Vec3d vec3d = (new Vec3d(x, y, z)).normalize().add(rand.nextGaussian() * (double) 0.0075f * (double) 0, rand.nextGaussian() * (double) 0.0075f * 0, rand.nextGaussian() * (double) 0.0075F * 0).scale(1);
                    setMotion(vec3d.scale(0.6));
                    float f = MathHelper.sqrt(horizontalMag(vec3d));
                    rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180 / (float) Math.PI));
                    rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (double) (180 / (float) Math.PI));
                    prevRotationYaw = rotationYaw;
                    prevRotationPitch = rotationPitch;
                }

                if(!TimestopHelper.isTimeStopped(world, this)) {
                    masterFuse--;
                    if (masterFuse <= 0) {
                        detonating = true;
                    }
                }
            }
            if (detonationTime == 0) {
                getServer().getWorld(dimension).getEntities()
                        .filter(entity -> entity instanceof LivingEntity)
                        .filter(entity -> !(entity instanceof AbstractStandEntity))
                        .filter(entity -> entity.getDistance(this) < 8)
                        .filter(Entity::isAlive)
                        .forEach(entity -> {
                                Util.dealStandDamage(shootingStand, (LivingEntity) entity, (9f - entity.getDistance(this)) * 5f, Vec3d.ZERO, entity.getDistance(this) > 3, null);
                        });

                Util.spawnParticle(masterStand, 5, this.getPosX(), this.getPosY(), this.getPosZ(), 0.5, 0.5, 0.5, 1);
                world.playSound(null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0f, 1.0f);

                detonating = false;
                remove();
            }
        }
    }

    @Override
    public void remove(){
            super.remove();
            if (masterStand != null)
                masterStand.shaCount--;
    }

    @Override
    public int getRange(){
        return 200;
    }

    @Override
    protected boolean shouldBeDestroyedByBlocks(BlockState state) {
        return false;
    }

    @Override
    protected boolean shouldRemoveOnHitEntity(){
        return false;
    }
}
