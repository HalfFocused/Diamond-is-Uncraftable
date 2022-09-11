package io.github.halffocused.diamond_is_uncraftable.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.capability.StandChunkEffects;
import io.github.halffocused.diamond_is_uncraftable.capability.Timestop;
import io.github.halffocused.diamond_is_uncraftable.config.DiamondIsUncraftableConfig;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.*;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.AbstractStandAttackEntity;
import io.github.halffocused.diamond_is_uncraftable.init.*;
import io.github.halffocused.diamond_is_uncraftable.item.StandArrowItem;
import io.github.halffocused.diamond_is_uncraftable.network.message.server.SParticlePacket;
import io.github.halffocused.diamond_is_uncraftable.network.message.client.CRemoveEntityPacket;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.InputMappings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static net.minecraft.entity.Entity.horizontalMag;

/**
 * Used for various utilities and constants.
 */
@SuppressWarnings("unused")
public class Util {

    static Random rand = new Random();

    /**
     * The true default Y motion for entities.
     */
    public static final double ENTITY_DEFAULT_Y_MOTION = -0.0784000015258789;

    public static int getHighestBlockInXZ(World world, BlockPos pos) {
        for (int height = world.getActualHeight(); height > 0; height--)
            if (world.getBlockState(new BlockPos(pos.getX(), height, pos.getZ())).getMaterial() != Material.AIR)
                return height;
        return -1;
    }

    public static Vec3d rotationVectorIgnoreY(LivingEntity entity) {
        float f = entity.rotationPitch * ((float)Math.PI / 180F);
        float f1 = -entity.rotationYaw * ((float)Math.PI / 180F);
        float f2 = MathHelper.cos(f1);
        float f3 = MathHelper.sin(f1);
        float f4 = MathHelper.cos(f);
        float f5 = MathHelper.sin(f);
        return new Vec3d((double)(f3), 0, (double)(f2));
    }

    /**
     * Returns whether or not the exact point pos is in any block's collision box.
     * world.getBlockState().isSolid() has a lot of issues that necessitated the creation of this method.
     * @param world The world that the collision check is taking place in.
     * @param pos The position vector being checked.
     * @return if the pos exists in any block's collision.
     */
    public static boolean isPointAtVecSolid(World world, Vec3d pos){
        VoxelShape collision = world.getBlockState(new BlockPos(pos)).getCollisionShape(world, new BlockPos(pos));
        for(AxisAlignedBB box : collision.toBoundingBoxList()){
            if(box.offset(new BlockPos(pos)).contains(pos)){
                return true;
            }
        }
        return false;
    }

    public static void teleportUntilWall(LivingEntity entity, Vec3d vec, double maxDistance){

        double incrementSize = 0.01;

        Vec3d currentPosition = new Vec3d(entity.getPosX(), entity.getPosY() + entity.getEyeHeight(), entity.getPosZ());
        Vec3d increment = vec.normalize().scale(incrementSize);
        boolean hitWall = false;
        Vec3d futurePosition = currentPosition.add(increment.scale(1 / incrementSize));

        for(int i = 0; i < maxDistance * (1 / incrementSize) && !hitWall; i++){

            if(Util.isPointAtVecSolid(entity.world, futurePosition)){
                hitWall = true;
            }
            currentPosition = currentPosition.add(increment);
            futurePosition = currentPosition.add(increment.scale(1 / incrementSize));
        }
        if(!hitWall){
            currentPosition = futurePosition;
        }
         entity.setPositionAndUpdate(currentPosition.getX(), currentPosition.getY(), currentPosition.getZ());
    }

    public static double heightAboveGround(World world, Vec3d pos){
        double distanceUntilGround = 0;
        while(!Util.isPointAtVecSolid(world, pos.add(new Vec3d(0, -distanceUntilGround, 0))) && pos.add(new Vec3d(0, -distanceUntilGround, 0)).y - distanceUntilGround >= 0.01){
            distanceUntilGround = distanceUntilGround + 0.01;
        }
        return distanceUntilGround;
    }

    public static BlockPos getNearestBlockEnd(World world, BlockPos pos) {
        for (int height = world.getActualHeight(); height > 0; height--) {
            if (pos.getX() > 0) {
                for (int x = pos.getX(); x > 0; x--)
                    if (world.getBlockState(new BlockPos(x, height, pos.getZ())).getMaterial() != Material.AIR)
                        return new BlockPos(x, height, pos.getZ());
            } else if (pos.getX() < 0) {
                for (int x = pos.getX(); x < 0; x++)
                    if (world.getBlockState(new BlockPos(x, height, pos.getZ())).getMaterial() != Material.AIR)
                        return new BlockPos(x, height, pos.getZ());
            }
        }
        return new BlockPos(0, 65, 0); //The location of the End exit portal.
    }

    public static SoundEvent getHitSound(AbstractStandEntity standIn){

        Random randomNoise = standIn.getRNG();

        switch(randomNoise.nextInt(7)){
            case 1:
                return SoundInit.PUNCH_2.get();
            case 2:
                return SoundInit.PUNCH_3.get();
            case 3:
                return SoundInit.PUNCH_4.get();
            case 4:
                return SoundInit.PUNCH_5.get();
            case 5:
                return SoundInit.PUNCH_6.get();
            case 6:
                return SoundInit.PUNCH_7.get();
            default:
                return SoundInit.PUNCH_1.get();
        }


    }

    public static boolean canStandMoveInStoppedTime(int standId){
        return standId == StandID.THE_WORLD || standId == StandID.STAR_PLATINUM || standId == StandID.TUSK_ACT_4;
    }

    public static boolean isTimeStoppedForEntity(LivingEntity entity){
            if(entity instanceof PlayerEntity){
                Stand props = Stand.getCapabilityFromPlayer((PlayerEntity) entity);
                return props.getExperiencingTimeStop() && !canStandMoveInStoppedTime(props.getStandID());
            }else{
                return !Timestop.getCapabilityFromEntity(entity).isEmpty();
            }
    }

    public static void activateEchoesAbility(World world, @Nonnull LivingEntity entity, BlockPos pos, StandChunkEffects chunkEffects, @Nullable PlayerEntity player, List<BlockPos> list) {
        if (world.isRemote) return;
        switch (new Random().nextInt(4)) {
            case 0: {
                world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 3, Explosion.Mode.DESTROY);
                break;
            }
            case 1: {
                entity.setMotion(entity.getMotion().add(0, 1, 0));
                entity.velocityChanged = true;
                break;
            }
            case 2: {
                entity.attackEntityFrom(DamageSource.HOT_FLOOR, 2);
                entity.setFire(4);
                break;
            }
            case 3: {
                entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, 10);
                break;
            }
            default:
                break;
        }
        if (player != null) {
            list.add(pos);
            Stand.getLazyOptional(player).ifPresent(stand -> stand.setAbilityUseCount(stand.getAbilityUseCount() - 1));
        }
        world.removeBlock(pos, false);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static void setupActSwitch(ServerPlayerEntity standMaster, int standID, int act) {
        switch (standID) {
            default:
                break;
            case StandID.BEACH_BOY: {
                switch (act) {
                    default:
                        break;
                    case 0: {
                        standMaster.sendStatusMessage(new StringTextComponent("Mode: Fishing Rod"), true);
                        break;
                    }
                    case 1: {
                        standMaster.sendStatusMessage(new StringTextComponent("Mode: Damage"), true);
                        break;
                    }
                    case 2: {
                        standMaster.sendStatusMessage(new StringTextComponent("Mode: Homing"), true);
                        break;
                    }
                }
            }
            break;
        }
    }

    public static boolean isClientHoldingShift() {
        return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static EntityRayTraceResult rayTraceEntities(Entity shooter, Vec3d startVec, Vec3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance) {
        World world = shooter.world;
        double d0 = distance;
        Entity entity = null;
        Vec3d vec3d = null;

        for (Entity entity1 : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter)) {
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(entity1.getCollisionBorderSize());
            Optional<Vec3d> optional = axisalignedbb.rayTrace(startVec, endVec);
            if (axisalignedbb.contains(startVec)) {
                if (d0 >= 0) {
                    entity = entity1;
                    vec3d = optional.orElse(startVec);
                    d0 = 0;
                }
            } else if (optional.isPresent()) {
                Vec3d vec3d1 = optional.get();
                double d1 = startVec.squareDistanceTo(vec3d1);
                if (d1 < d0 || d0 == 0) {
                    if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity() && !entity1.canRiderInteract()) {
                        if (d0 == 0) {
                            entity = entity1;
                            vec3d = vec3d1;
                        }
                    } else {
                        entity = entity1;
                        vec3d = vec3d1;
                        d0 = d1;
                    }
                }
            }
        }

        return entity == null ? Null() : new EntityRayTraceResult(entity, vec3d);
    }

    /**
     * Suppresses warning for unchecked casts.
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    /**
     * Got these values from <a href ="https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/1435515-how-i-can-do-to-move-to-where-i-look#c5">this</a> thread, shortened it a little bit.
     */
    public static Vec3d getEntityForwardsMotion(Entity entity) {
        return new Vec3d(
                -MathHelper.sin(entity.rotationYaw / 180 * (float) Math.PI) * MathHelper.cos(entity.rotationPitch / 180 * (float) Math.PI),
                MathHelper.cos(entity.rotationYaw / 180 * (float) Math.PI) * MathHelper.cos(entity.rotationPitch / 180 * (float) Math.PI),
                MathHelper.cos(entity.rotationYaw / 180 * (float) Math.PI) * MathHelper.cos(entity.rotationPitch / 180 * (float) Math.PI)
        );
    }

    public static AxisAlignedBB getAttackHitbox(AbstractStandEntity standIn, double rangeIn){
        return getAttackHitbox(standIn, rangeIn, 0.25D);
    }

    public static AxisAlignedBB getAttackHitbox(AbstractStandEntity standIn, double rangeIn, double sizeIn){
        return standIn.getBoundingBox()
                .offset(standIn.getLookVec().normalize().mul(1.5, 1.5, 1.5))
                .expand(standIn.getLookVec().scale(rangeIn - 1.0)).grow(sizeIn);
    }

    /**
     * Statically renders the given {@link BlockState} at the given {@link BlockPos}, like {@link net.minecraft.client.renderer.entity.EntityRendererManager#renderEntityStatic(Entity, double, double, double, float, float, MatrixStack, IRenderTypeBuffer, int)}, but for blocks.
     */
    public static void renderBlockStatic(MatrixStack matrixStack, IRenderTypeBuffer.Impl buffer, World world, BlockState blockState, BlockPos blockPos, Vec3d projectedView, boolean occlusionCulling) {
        matrixStack.push();
        matrixStack.translate(-projectedView.x + blockPos.getX(), -projectedView.y + blockPos.getY(), -projectedView.z + blockPos.getZ());
        for (RenderType renderType : RenderType.getBlockRenderTypes()) {
            if (RenderTypeLookup.canRenderInLayer(blockState, renderType))
                Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                        world,
                        Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(blockState),
                        blockState,
                        blockPos,
                        matrixStack,
                        buffer.getBuffer(renderType),
                        occlusionCulling,
                        new Random(),
                        blockState.getPositionRandom(blockPos),
                        OverlayTexture.NO_OVERLAY,
                        EmptyModelData.INSTANCE
                );
        }
        matrixStack.pop();
        buffer.finish();
    }

    /**
     * Used to suppress warnings saying that <code>static final</code> fields are <code>null</code>.
     * Based on diesieben07's solution <a href="http://www.minecraftforge.net/forum/topic/60980-solved-disable-%E2%80%9Cconstant-conditions-exceptions%E2%80%9D-inspection-for-field-in-intellij-idea/?do=findCommentcomment=285024">here</a>.
     *
     * @return null
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T Null() {
        return null;
    }

    public static void travelWithFriction(LivingEntity entity, float slipperiness) {
        Vec3d positionIn = new Vec3d(entity.moveStrafing, entity.moveVertical, entity.moveForward);
        if (entity.isServerWorld() || entity.canPassengerSteer()) {
            double d0 = 0.08;
            boolean flag = entity.getMotion().y <= 0;
            if (flag && entity.isPotionActive(Effects.SLOW_FALLING))
                entity.fallDistance = 0;
            if (!entity.isInWater() || entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying) {
                if (!entity.isInLava() || entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.isFlying) {
                    if (entity.isElytraFlying()) {
                        Vec3d vec3d3 = entity.getMotion();
                        if (vec3d3.y > -0.5)
                            entity.fallDistance = 1;

                        Vec3d vec3d = entity.getLookVec();
                        float f6 = entity.rotationPitch * ((float) Math.PI / 180);
                        double d9 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                        double d11 = Math.sqrt(horizontalMag(vec3d3));
                        double d12 = vec3d.length();
                        float f3 = MathHelper.cos(f6);
                        f3 = (float) ((double) f3 * (double) f3 * Math.min(1, d12 / 0.4));
                        vec3d3 = entity.getMotion().add(0, d0 * (-1 + (double) f3 * 0.75), 0);
                        if (vec3d3.y < 0 && d9 > 0) {
                            double d3 = vec3d3.y * -0.1 * (double) f3;
                            vec3d3 = vec3d3.add(vec3d.x * d3 / d9, d3, vec3d.z * d3 / d9);
                        }
                        if (f6 < 0 && d9 > 0) {
                            double d13 = d11 * (double) (-MathHelper.sin(f6)) * 0.04;
                            vec3d3 = vec3d3.add(-vec3d.x * d13 / d9, d13 * 3.2, -vec3d.z * d13 / d9);
                        }
                        if (d9 > 0)
                            vec3d3 = vec3d3.add((vec3d.x / d9 * d11 - vec3d3.x) * 0.1, 0.0, (vec3d.z / d9 * d11 - vec3d3.z) * 0.1D);

                        entity.setMotion(vec3d3.mul(0.99, 0.98, 0.99));
                        entity.move(MoverType.SELF, entity.getMotion());
                        if (entity.collidedHorizontally && !entity.world.isRemote) {
                            double d14 = Math.sqrt(horizontalMag(entity.getMotion()));
                            double d4 = d11 - d14;
                            float f4 = (float) (d4 * 10 - 3);
                            if (f4 > 0)
                                entity.attackEntityFrom(DamageSource.FLY_INTO_WALL, f4);
                        }
                    } else {
                        BlockPos blockpos = new BlockPos(entity.getPosX(), entity.getBoundingBox().minY - 0.5000001, entity.getPosZ());
                        float f7 = entity.onGround ? slipperiness * 0.91F : 0.91F;
                        entity.moveRelative(entity.onGround ? entity.getAIMoveSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness)) : entity.jumpMovementFactor, positionIn);
                        entity.move(MoverType.SELF, entity.getMotion());
                        Vec3d vec3d5 = entity.getMotion();
                        if ((entity.collidedHorizontally || !entity.onGround) && entity.isOnLadder())
                            vec3d5 = new Vec3d(vec3d5.x, 0.2, vec3d5.z);
                        double d10 = vec3d5.y;
                        if (entity.isPotionActive(Effects.LEVITATION)) {
                            d10 += (0.05 * (double) (entity.getActivePotionEffect(Effects.LEVITATION).getAmplifier() + 1) - vec3d5.y) * 0.2;
                            entity.fallDistance = 0;
                        } else if (entity.world.isRemote && !entity.world.isBlockLoaded(blockpos)) {
                            if (entity.getPosY() > 0)
                                d10 = -0.1;
                            else
                                d10 = 0;
                        } else if (!entity.hasNoGravity())
                            d10 -= d0;
                        entity.setMotion(vec3d5.x * (double) f7, d10 * 0.98, vec3d5.z * (double) f7);
                    }
                } else {
                    double d7 = entity.getPosY();
                    entity.moveRelative(0.02F, positionIn);
                    entity.move(MoverType.SELF, entity.getMotion());
                    entity.setMotion(entity.getMotion().scale(0.5D));
                    if (!entity.hasNoGravity())
                        entity.setMotion(entity.getMotion().add(0, -d0 / 4, 0));
                    Vec3d vec3d4 = entity.getMotion();
                    if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(vec3d4.x, vec3d4.y + 0.6 - entity.getPosY() + d7, vec3d4.z))
                        entity.setMotion(vec3d4.x, 0.3, vec3d4.z);
                }
            } else {
                double d1 = entity.getPosY();
                float f1 = 0.02f;
                float f2 = (float) EnchantmentHelper.getDepthStriderModifier(entity);
                entity.move(MoverType.SELF, entity.getMotion());
                Vec3d vec3d1 = entity.getMotion();
                if (entity.collidedHorizontally && entity.isOnLadder()) {
                    vec3d1 = new Vec3d(vec3d1.x, 0.2, vec3d1.z);
                }

                entity.setMotion(vec3d1.mul(1, 0.8, 1));
                if (!entity.hasNoGravity() && !entity.isSprinting()) {
                    Vec3d vec3d2 = entity.getMotion();
                    double d2;
                    if (flag && Math.abs(vec3d2.y - 0.005) >= 0.003 && Math.abs(vec3d2.y - d0 / 16) < 0.003)
                        d2 = -0.003D;
                    else
                        d2 = vec3d2.y - d0 / 16;
                    entity.setMotion(vec3d2.x, d2, vec3d2.z);
                }

                Vec3d vec3d6 = entity.getMotion();
                if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(vec3d6.x, vec3d6.y + 0.6 - entity.getPosY() + d1, vec3d6.z))
                    entity.setMotion(vec3d6.x, 0.3, vec3d6.z);
            }
        }

        entity.prevLimbSwingAmount = entity.limbSwingAmount;
        double d5 = entity.getPosX() - entity.prevPosX;
        double d6 = entity.getPosZ() - entity.prevPosZ;
        double d8 = entity instanceof IFlyingAnimal ? entity.getPosY() - entity.prevPosY : 0.0D;
        float f8 = MathHelper.sqrt(d5 * d5 + d8 * d8 + d6 * d6) * 4.0F;
        if (f8 > 1.0F) {
            f8 = 1.0F;
        }

        entity.limbSwingAmount += (f8 - entity.limbSwingAmount) * 0.4F;
        entity.limbSwing += entity.limbSwingAmount;
    }

    public static void spawnParticle(LivingEntity trackingEntity, int particleId, double posX, double posY, double posZ, double dX, double dY, double dZ, int amount) {
        DiamondIsUncraftable.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> trackingEntity), new SParticlePacket(particleId, posX, posY, posZ, dX, dY, dZ, amount));
    }

    public static void spawnParticle(DimensionType trackingDimension, int particleId, double posX, double posY, double posZ, double dX, double dY, double dZ, int amount) {
        DiamondIsUncraftable.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> trackingDimension), new SParticlePacket(particleId, posX, posY, posZ, dX, dY, dZ, amount));
    }

    public static void spawnClientParticle(ServerPlayerEntity player, int particleId, double posX, double posY, double posZ, double dX, double dY, double dZ, int amount) {
        DiamondIsUncraftable.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SParticlePacket(particleId, posX, posY, posZ, dX, dY, dZ, amount));
    }

    public static void dealStandDamage(AbstractStandEntity stand, LivingEntity entity, float damage, Vec3d motion, boolean blockable){

        LivingEntity attackedEntity;

        Random random = stand.getRNG();
        boolean blockedFlag = false;

        if (entity instanceof PlayerEntity) {
            if (entity.isActiveItemStackBlocking() && blockable) {
                entity.getHeldEquipment().forEach(itemStack -> {
                    if (itemStack.getItem().equals(Items.SHIELD)) {
                        itemStack.damageItem((int) damage * 4, ((PlayerEntity) entity), (playerEntity) -> {
                            playerEntity.sendBreakAnimation(Hand.MAIN_HAND); //F**k it I'm just gonna play the animation on both hands
                            playerEntity.sendBreakAnimation(Hand.OFF_HAND);
                        });
                    }
                });
                blockedFlag = true;
            }
        }
        if (!blockedFlag) {
            entity.hurtResistantTime = 0;
            if (entity instanceof AbstractStandEntity) {
                ((AbstractStandEntity) entity).getMaster().hurtResistantTime = 0;
            }

            double damageModifier = (float) DiamondIsUncraftableConfig.COMMON.standDamageMultiplier.get().doubleValue();
            double potionModifier = 1.0;

            for(EffectInstance effect : stand.getMaster().getActivePotionEffects()){
                if(effect.getPotion().equals(EffectInit.STAND_STRENGTH.get())){
                    potionModifier += (effect.getAmplifier() + 1) * 0.1;
                }
                if(effect.getPotion().equals(EffectInit.STAND_WEAKNESS.get())){
                    potionModifier -= (effect.getAmplifier() + 1) * 0.1;
                }
            }
            potionModifier = Math.max(potionModifier, 0); //Can't let it go into the negatives
            float modifiedDamage = (float) (damage * damageModifier * potionModifier);
            entity.attackEntityFrom(DamageSource.causeMobDamage(stand.getMaster()), modifiedDamage);


            /*
             * Drawn together by fate achievement
             */
            if(!entity.world.isRemote()) {
                PlayerEntity killedPlayer = null;
                if (!entity.isAlive()) {
                    if (entity instanceof PlayerEntity) {
                        killedPlayer = (PlayerEntity) entity;
                    }
                }
                if (entity instanceof AbstractStandEntity && !((AbstractStandEntity) entity).getMaster().isAlive()){
                    killedPlayer = ((AbstractStandEntity) entity).getMaster();
                }
                if(killedPlayer != null){
                    Stand killedStand = Stand.getCapabilityFromPlayer(killedPlayer);
                    if(killedStand.getStandID() != 0){
                        Util.giveAdvancement((ServerPlayerEntity) stand.getMaster(), "killstanduser");
                    }
                }
            }

            entity.setMotion(motion);

            stand.setMostRecentlyDamagedEntity(entity);

            Util.spawnParticle(stand, 3, entity.getPosX(), entity.getEyeHeight() + entity.getPosY(), entity.getPosZ(), 2.4, 1.4, 2.4, 1);
            Util.spawnParticle(stand, 4, entity.getPosX() + (random.nextFloat() - 0.5), entity.getEyeHeight() + entity.getPosY() + (random.nextFloat() - 0.5), entity.getPosZ() + (random.nextFloat() - 0.5), 0.7, 0.9, 0.7, (int) (damage * 8.5));


            Stand.getLazyOptional(stand.getMaster()).ifPresent(props -> {
                if (stand instanceof IMomentum) {
                    props.setMomentum(Math.min(100, props.getMomentum() + ((IMomentum) stand).addMomentumAmount()));
                }
            });

            if(stand instanceof IOnHit){
                ((IOnHit) stand).onHit(entity, modifiedDamage);
            }
        }else{
            entity.world.playSound(null, stand.getPosition(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.NEUTRAL, 0.25F, (random.nextFloat() * 0.3f + 1) * 2);
        }

    }

    public static void dealUnsummonedStandDamage(PlayerEntity standMaster, LivingEntity entity, float damage, Vec3d motion, boolean blockable){

        LivingEntity attackedEntity;

        Random random = standMaster.getRNG();
        boolean blockedFlag = false;

        if (entity instanceof PlayerEntity) {
            if (entity.isActiveItemStackBlocking() && blockable) {
                entity.getHeldEquipment().forEach(itemStack -> {
                    if (itemStack.getItem().equals(Items.SHIELD)) {
                        itemStack.damageItem((int) damage * 4, ((PlayerEntity) entity), (playerEntity) -> {
                            playerEntity.sendBreakAnimation(Hand.MAIN_HAND); //F**k it I'm just gonna play the animation on both hands
                            playerEntity.sendBreakAnimation(Hand.OFF_HAND);
                        });
                    }
                });
                blockedFlag = true;
            }
        }
        if (!blockedFlag) {
            entity.hurtResistantTime = 0;
            if (entity instanceof AbstractStandEntity) {
                ((AbstractStandEntity) entity).getMaster().hurtResistantTime = 0;
            }

            double damageModifier = (float) DiamondIsUncraftableConfig.COMMON.standDamageMultiplier.get().doubleValue();
            double potionModifier = 1.0;

            for(EffectInstance effect : standMaster.getActivePotionEffects()){
                if(effect.getPotion().equals(EffectInit.STAND_STRENGTH.get())){
                    potionModifier += (effect.getAmplifier() + 1) * 0.1;
                }
                if(effect.getPotion().equals(EffectInit.STAND_WEAKNESS.get())){
                    potionModifier -= (effect.getAmplifier() + 1) * 0.1;
                }
            }
            potionModifier = Math.max(potionModifier, 0); //Can't let it go into the negatives
            float modifiedDamage = (float) (damage * damageModifier * potionModifier);
            entity.attackEntityFrom(DamageSource.causeMobDamage(standMaster), modifiedDamage);


            /*
             * Drawn together by fate achievement
             */
            if(!entity.world.isRemote()) {
                PlayerEntity killedPlayer = null;
                if (!entity.isAlive()) {
                    if (entity instanceof PlayerEntity) {
                        killedPlayer = (PlayerEntity) entity;
                    }
                }
                if (entity instanceof AbstractStandEntity && !((AbstractStandEntity) entity).getMaster().isAlive()){
                    killedPlayer = ((AbstractStandEntity) entity).getMaster();
                }
                if(killedPlayer != null){
                    Stand killedStand = Stand.getCapabilityFromPlayer(killedPlayer);
                    if(killedStand.getStandID() != 0){
                        Util.giveAdvancement((ServerPlayerEntity) standMaster, "killstanduser");
                    }
                }
            }

            entity.setMotion(motion);

        }else{
            entity.world.playSound(null, standMaster.getPosition(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.NEUTRAL, 0.25F, (random.nextFloat() * 0.3f + 1) * 2);
        }

    }

    public static void applyUnactionableTicks(PlayerEntity playerIn, int ticks){
        Stand.getLazyOptional(playerIn).ifPresent(stand -> {
            if(stand.getStandOn()){
                Entity grabbedStand = playerIn.world.getEntityByID(stand.getPlayerStand());
                if(grabbedStand != null){
                    if(grabbedStand instanceof AbstractStandEntity){
                        ((AbstractStandEntity) grabbedStand).getController().setUnactionableTicks(ticks);
                    }
                }
            }
        });
    }

    @CheckForNull
    public static AbstractStandEntity getStandEntityFromPlayer(PlayerEntity playerIn){
        Stand stand = Stand.getCapabilityFromPlayer(playerIn);
            if(stand.getStandOn()){
                Entity grabbedStand = playerIn.world.getEntityByID(stand.getPlayerStand());
                if(grabbedStand instanceof AbstractStandEntity){
                    return (AbstractStandEntity) grabbedStand;
                }
            }
            return null;
    }

    public static class Predicates {
        public static final Predicate<Entity> NOT_STAND = entity -> !(entity instanceof AbstractStandEntity);
        public static final Predicate<Entity> IS_STAND = entity -> entity instanceof AbstractStandEntity;

        public static final Predicate<Entity> STAND_PUNCH_TARGET =
                EntityPredicates.NOT_SPECTATING
                        .and(EntityPredicates.IS_ALIVE)
                        .and(Entity::canBeCollidedWith);

        public static final Predicate<Entity> BREATHS = //Pretty much a list of every entity that doesn't breath.
                ((Predicate<Entity>) entity -> !(entity instanceof ZombieEntity))
                        .and(((Predicate<Entity>) entity -> !(entity instanceof HuskEntity))
                                .and(((Predicate<Entity>) entity -> !(entity instanceof DrownedEntity))
                                        .and(((Predicate<Entity>) entity -> !(entity instanceof ZombieHorseEntity))
                                                .and(((Predicate<Entity>) entity -> !(entity instanceof SkeletonEntity))
                                                        .and(((Predicate<Entity>) entity -> !(entity instanceof WitherSkeletonEntity))
                                                                .and(((Predicate<Entity>) entity -> !(entity instanceof SkeletonHorseEntity))
                                                                        .and(((Predicate<Entity>) entity -> !(entity instanceof GiantEntity))
                                                                                .and(((Predicate<Entity>) entity -> !(entity instanceof ZombieVillagerEntity))
                                                                                        .and(((Predicate<Entity>) entity -> !(entity instanceof StrayEntity))
                                                                                                .and(entity -> !(entity instanceof ZombiePigmanEntity))
                                                                                                .and(entity -> !(entity instanceof PhantomEntity))
                                                                                                .and(entity -> !(entity instanceof AbstractStandAttackEntity))
                                                                                                .and(entity -> !(entity instanceof AbstractStandEntity))
                                                                                                .and(entity -> !(entity instanceof ItemEntity)))))))))));
    }

    public static class StandID {
        public static final int KING_CRIMSON = 1;

        public static final int KILLER_QUEEN = 2;

        public static final int SILVER_CHARIOT = 3;

        public static final int THE_WORLD = 4;

        public static final int PURPLE_HAZE = -69;

        public static final int D4C = -1;

        public static final int GOLD_EXPERIENCE = -2;

        public static final int MADE_IN_HEAVEN = -3;

        public static final int GER = -4;

        public static final int AEROSMITH = -5;

        public static final int WEATHER_REPORT = -6;

        public static final int CRAZY_DIAMOND = -8;

        public static final int THE_EMPEROR = -10;

        public static final int WHITESNAKE = -11;

        public static final int CMOON = -12;

        public static final int STAR_PLATINUM = -13;

        public static final int MAGICIANS_RED = -16;

        public static final int THE_HAND = -17;

        public static final int HIEROPHANT_GREEN = -18;

        public static final int GREEN_DAY = -19;

        public static final int TWENTIETH_CENTURY_BOY = -20;

        public static final int THE_GRATEFUL_DEAD = -21;

        public static final int STICKY_FINGERS = -22;

        public static final int TUSK_ACT_1 = -23;

        public static final int TUSK_ACT_2 = -24;

        public static final int TUSK_ACT_3 = -25;

        public static final int TUSK_ACT_4 = -26;

        public static final int ECHOES_ACT_1 = -27;

        public static final int ECHOES_ACT_2 = -28;

        public static final int ECHOES_ACT_3 = -29;

        public static final int BEACH_BOY = -30;

        /**
         * An array of Stand's that can be obtained through the {@link StandArrowItem}.
         */
        public static final int[] STANDS = {
                KING_CRIMSON,
                KILLER_QUEEN,
                SILVER_CHARIOT,
                THE_WORLD
        };

        public static final List<Integer> MOMENTUM_METER_STANDS = Arrays.asList( //What stands should render the momentum meter on the HUD
            SILVER_CHARIOT,
            THE_WORLD
        );

        public static final List<Integer> ITEM_STANDS = Arrays.asList(
                THE_EMPEROR,
                BEACH_BOY
        );


        public static final List<Integer> STANDS_WITH_ACTS = Arrays.asList(
                MADE_IN_HEAVEN,
                CMOON,
                TUSK_ACT_2,
                TUSK_ACT_3,
                TUSK_ACT_4,
                ECHOES_ACT_2,
                ECHOES_ACT_3,
                BEACH_BOY //Not really, but he uses the switch act button.
        );

        public static final List<Integer> EVOLUTION_STANDS = Arrays.asList(
                WHITESNAKE,
                CMOON,
                TUSK_ACT_1,
                TUSK_ACT_2,
                TUSK_ACT_3,
                ECHOES_ACT_1,
                ECHOES_ACT_2,
                KILLER_QUEEN,
                GOLD_EXPERIENCE
        );

        /**
         * Returns an {@link AbstractStandEntity} based on the StandID inputted.
         *
         * @param standID The StandID of the Stand, see {@link StandID}.
         * @param world   The {@link World} the Stand will be summoned in.
         */
        public static AbstractStandEntity getStandByID(int standID, World world) {
            switch (standID) {
                default:
                    return Null();
                case KING_CRIMSON:
                    return new KingCrimsonEntity(EntityInit.KING_CRIMSON.get(), world);
                case D4C:
                    return new D4CEntity(EntityInit.D4C.get(), world);
                case GOLD_EXPERIENCE:
                    return new GoldExperienceEntity(EntityInit.GOLD_EXPERIENCE.get(), world);
                case MADE_IN_HEAVEN:
                    return new MadeInHeavenEntity(EntityInit.MADE_IN_HEAVEN.get(), world);
                case GER:
                    return new GoldExperienceRequiemEntity(EntityInit.GOLD_EXPERIENCE_REQUIEM.get(), world);
                case AEROSMITH:
                    return new AerosmithEntity(EntityInit.AEROSMITH.get(), world);
                case WEATHER_REPORT:
                    return new WeatherReportEntity(EntityInit.WEATHER_REPORT.get(), world);
                case KILLER_QUEEN:
                    return new KillerQueenEntity(EntityInit.KILLER_QUEEN.get(), world);
                case CRAZY_DIAMOND:
                    return new CrazyDiamondEntity(EntityInit.CRAZY_DIAMOND.get(), world);
                case PURPLE_HAZE:
                    return new PurpleHazeEntity(EntityInit.PURPLE_HAZE.get(), world);
                case WHITESNAKE:
                    return new WhitesnakeEntity(EntityInit.WHITESNAKE.get(), world);
                case CMOON:
                    return new CMoonEntity(EntityInit.CMOON.get(), world);
                case THE_WORLD:
                    return new TheWorldEntity(EntityInit.THE_WORLD.get(), world);
                case STAR_PLATINUM:
                    return new StarPlatinumEntity(EntityInit.STAR_PLATINUM.get(), world);
                case SILVER_CHARIOT:
                    return new SilverChariotEntity(EntityInit.SILVER_CHARIOT.get(), world);
                case MAGICIANS_RED:
                    return new MagiciansRedEntity(EntityInit.MAGICIANS_RED.get(), world);
                case THE_HAND:
                    return new TheHandEntity(EntityInit.THE_HAND.get(), world);
                case HIEROPHANT_GREEN:
                    return new HierophantGreenEntity(EntityInit.HIEROPHANT_GREEN.get(), world);
                case GREEN_DAY:
                    return new GreenDayEntity(EntityInit.GREEN_DAY.get(), world);
                case TWENTIETH_CENTURY_BOY:
                    return new TwentiethCenturyBoyEntity(EntityInit.TWENTIETH_CENTURY_BOY.get(), world);
                case THE_GRATEFUL_DEAD:
                    return new TheGratefulDeadEntity(EntityInit.THE_GRATEFUL_DEAD.get(), world);
                case STICKY_FINGERS:
                    return new StickyFingersEntity(EntityInit.STICKY_FINGERS.get(), world);
                case TUSK_ACT_1:
                    return new TuskAct1Entity(EntityInit.TUSK_ACT_1.get(), world);
                case TUSK_ACT_2:
                    return new TuskAct2Entity(EntityInit.TUSK_ACT_2.get(), world);
                case TUSK_ACT_3:
                    return new TuskAct3Entity(EntityInit.TUSK_ACT_3.get(), world);
                case TUSK_ACT_4:
                    return new TuskAct4Entity(EntityInit.TUSK_ACT_4.get(), world);
                case ECHOES_ACT_1:
                    return new EchoesAct1Entity(EntityInit.ECHOES_ACT_1.get(), world);
                case ECHOES_ACT_2:
                    return new EchoesAct2Entity(EntityInit.ECHOES_ACT_2.get(), world);
                case ECHOES_ACT_3:
                    return new EchoesAct3Entity(EntityInit.ECHOES_ACT_3.get(), world);
            }
        }

        public static ItemStack getItemStandById(int standID) {
            switch (standID) {
                default:
                    return Null();
                case THE_EMPEROR:
                    return new ItemStack(ItemInit.THE_EMPEROR.get());
                case BEACH_BOY:
                    return new ItemStack(ItemInit.BEACH_BOY.get());
            }
        }

        public static void summonItemStand(ServerPlayerEntity master) {
            Stand.getLazyOptional(master).ifPresent(capability -> {
                ItemStack itemStack = Util.StandID.getItemStandById(capability.getStandID());
                if (!master.inventory.hasItemStack(itemStack)) {
                    if (master.inventory.getStackInSlot(master.inventory.getBestHotbarSlot()).isEmpty()) {
                        master.inventory.currentItem = master.inventory.getBestHotbarSlot();
                        master.inventory.add(master.inventory.getBestHotbarSlot(), itemStack);
                        switch (capability.getStandID()) {
                            default:
                                break;
                            case THE_EMPEROR: {
                                master.world.playSound(null, master.getPosition(), SoundInit.SPAWN_THE_EMPEROR.get(), SoundCategory.NEUTRAL, 1, 1);
                                break;
                            }
                            case BEACH_BOY: {
                                master.world.playSound(null, master.getPosition(), SoundInit.SPAWN_BEACH_BOY.get(), SoundCategory.NEUTRAL, 1, 1);
                                break;
                            }
                        }
                        capability.setStandOn(true);
                    } else
                        master.sendMessage(new StringTextComponent("Your hotbar is full!"), ChatType.GAME_INFO);
                } else {
                    itemStack.shrink(1);
                    capability.setStandOn(false);
                }
            });
        }

        public static boolean isStandThatEvolves(int id){
            return EVOLUTION_STANDS.contains(id) || id == MADE_IN_HEAVEN || id == TUSK_ACT_4 || id == ECHOES_ACT_3;
        }
    }


    public static class KeyCodes {
        public static final String SUMMON_STAND = KeyInit.SPAWN_STAND.getLocalizedName().toUpperCase();
        public static final String ABILITY_TOGGLE = KeyInit.TOGGLE_ABILITY.getLocalizedName().toUpperCase();
        public static final String ABILITY_1 = KeyInit.ABILITY1.getLocalizedName().toUpperCase();
        public static final String ABILITY_2 = KeyInit.ABILITY2.getLocalizedName().toUpperCase();
        public static final String ABILITY_3 = KeyInit.ABILITY3.getLocalizedName().toUpperCase();
        public static final String SWITCH_ACT = KeyInit.SWITCH_ACT.getLocalizedName().toUpperCase();
    }

    public static class ResourceLocations {
        public static final ResourceLocation KING_CRIMSON = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/king_crimson.png");
        public static final ResourceLocation KING_CRIMSON_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/king_crimson_punch.png");
        public static final ResourceLocation D4C = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/d4c.png");
        public static final ResourceLocation D4C_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/d4c_punch.png");
        public static final ResourceLocation GOLD_EXPERIENCE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/gold_experience.png");
        public static final ResourceLocation GOLD_EXPERIENCE_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/gold_experience_punch.png");
        public static final ResourceLocation MADE_IN_HEAVEN = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/made_in_heaven.png");
        public static final ResourceLocation MADE_IN_HEAVEN_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/made_in_heaven_punch.png");
        public static final ResourceLocation GER = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/ger.png");
        public static final ResourceLocation GER_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/ger_punch.png");
        public static final ResourceLocation AEROSMITH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/aerosmith.png");
        public static final ResourceLocation AEROSMITH_BULLET = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/aerosmith_bullet.png");
        public static final ResourceLocation WEATHER_REPORT = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/weather_report.png");
        public static final ResourceLocation WEATHER_REPORT_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/weather_report_punch.png");
        public static final ResourceLocation KILLER_QUEEN = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen.png");
        public static final ResourceLocation KILLER_QUEEN_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen_punch.png");
        public static final ResourceLocation SHEER_HEART_ATTACK = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/sheer_heart_attack.png");
        public static final ResourceLocation CRAZY_DIAMOND = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/crazy_diamond.png");
        public static final ResourceLocation CRAZY_DIAMOND_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/crazy_diamond_punch.png");
        public static final ResourceLocation PURPLE_HAZE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/purple_haze.png");
        public static final ResourceLocation PURPLE_HAZE_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/purple_haze_punch.png");
        public static final ResourceLocation EMPEROR_BULLET = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/emperor_bullet.png");
        public static final ResourceLocation WHITESNAKE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/whitesnake.png");
        public static final ResourceLocation WHITESNAKE_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/whitesnake_punch.png");
        public static final ResourceLocation CMOON = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/cmoon.png");
        public static final ResourceLocation CMOON_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/cmoon_punch.png");
        public static final ResourceLocation THE_WORLD = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_world.png");
        public static final ResourceLocation THE_WORLD_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_world_punch.png");
        public static final ResourceLocation STAR_PLATINUM = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/star_platinum.png");
        public static final ResourceLocation STAR_PLATINUM_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/star_platinum_punch.png");
        public static final ResourceLocation SILVER_CHARIOT = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/silver_chariot.png");
        public static final ResourceLocation SILVER_CHARIOT_SWORD = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/silver_chariot_sword.png");
        public static final ResourceLocation MAGICIANS_RED = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/magicians_red.png");
        public static final ResourceLocation MAGICIANS_RED_FLAME = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/magicians_red_flames.png");
        public static final ResourceLocation THE_HAND = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_hand.png");
        public static final ResourceLocation THE_HAND_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_hand_punch.png");
        public static final ResourceLocation HIEROPHANT_GREEN = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/hierophant_green.png");
        public static final ResourceLocation HIEROPHANT_GREEN_TAIL = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/hierophant_green_tail.png");
        public static final ResourceLocation GREEN_DAY = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/green_day.png");
        public static final ResourceLocation GREEN_DAY_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/green_day_punch.png");
        public static final ResourceLocation TWENTIETH_CENTURY_BOY = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/20th_century_boy.png");
        public static final ResourceLocation THE_GRATEFUL_DEAD = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_grateful_dead.png");
        public static final ResourceLocation THE_GRATEFUL_DEAD_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/the_grateful_dead_punch.png");
        public static final ResourceLocation STICKY_FINGERS = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/sticky_fingers.png");
        public static final ResourceLocation STICKY_FINGERS_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/sticky_fingers_punch.png");
        public static final ResourceLocation NAIL_BULLET = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/nail_bullet.png");
        public static final ResourceLocation TUSK_ACT_1 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/tusk_act_1.png");
        public static final ResourceLocation TUSK_ACT_2 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/tusk_act_2.png");
        public static final ResourceLocation TUSK_ACT_3 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/tusk_act_3.png");
        public static final ResourceLocation TUSK_ACT_4 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/tusk_act_4.png");
        public static final ResourceLocation TUSK_ACT_4_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/tusk_act_4_punch.png");
        public static final ResourceLocation ECHOES_ACT_1 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/echoes_act_1.png");
        public static final ResourceLocation ECHOES_SOUND_WAVE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/echoes_act_1_attack.png");
        public static final ResourceLocation ECHOES_ACT_2 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/echoes_act_2.png");
        public static final ResourceLocation ECHOES_ACT_3 = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/echoes_act_3.png");
        public static final ResourceLocation ECHOES_ACT_3_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/echoes_act_3_punch.png");
        public static final ResourceLocation SOFT_AND_WET = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/soft_and_wet.png");
        public static final ResourceLocation SOFT_AND_WET_PUNCH = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/soft_and_wet_punch.png");
        public static final ResourceLocation BUBBLE = new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/bubble.png");
    }

    /**
     * Give the player an advancement from this mod. Yep, I totally cheated implementing this.
     * @param player The player to give the achievement.
     * @param name The name of the advancement.
     * @return true if the player didn't have the advancement prior to this.
     */
    public static boolean giveAdvancement(ServerPlayerEntity player, String name) {

        Advancement advancement = Objects.requireNonNull(player.getServer()).getAdvancementManager().getAdvancement(new ResourceLocation(DiamondIsUncraftable.MOD_ID, name));



        if (advancement != null) {
            AdvancementProgress advancementprogress = player.getAdvancements().getProgress(advancement);
            if (advancementprogress.isDone()) {
                return false;
            } else {
                for (String s : advancementprogress.getRemaningCriteria()) {
                    player.getAdvancements().grantCriterion(advancement, s);
                }

                return true;
            }
        }
        return false;
    }

    public static void standExplosion(PlayerEntity master, World world, Vec3d position, double range, double blockableDistance, double minDamage, double maxDamage) {
        if(!world.isRemote()) {
            Util.spawnParticle(master.dimension, 5, position.getX(), position.getY(), position.getZ(), 1, 1, 1, 1);
            Util.spawnParticle(master.dimension, 14, position.getX(), position.getY(), position.getZ(), 1, 1, 1, 20);
            Explosion explosion = new Explosion(master.world, master, position.getX(), position.getY(), position.getZ(), 4, true, Explosion.Mode.NONE);
            master.world.playSound(null, new BlockPos(position), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
            explosion.doExplosionB(true);

            master.getServer().getWorld(master.dimension).getEntities()
                    .filter(entity -> entity instanceof LivingEntity)
                    .filter(entity -> !(entity instanceof AbstractStandEntity))
                    .filter(entity -> Math.sqrt((entity.getDistanceSq(position))) <= range)
                    .filter(Entity::isAlive)
                    .forEach(entity -> Util.dealUnsummonedStandDamage(master, (LivingEntity) entity, (float) lerp(minDamage, maxDamage, Math.sqrt((entity.getDistanceSq(position))) / range), Vec3d.ZERO, Math.sqrt((entity.getDistanceSq(position))) >= blockableDistance));
        }
    }

    public static void standExplosionFX(PlayerEntity master, World world, Vec3d position) {
        if(!world.isRemote()) {
            Util.spawnParticle(master.dimension, 5, position.getX(), position.getY(), position.getZ(), 1, 1, 1, 1);
            Util.spawnParticle(master.dimension, 14, position.getX(), position.getY(), position.getZ(), 1, 1, 1, 20);
            Explosion explosion = new Explosion(master.world, master, position.getX(), position.getY(), position.getZ(), 4, true, Explosion.Mode.NONE);
            master.world.playSound(null, new BlockPos(position), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
            explosion.doExplosionB(true);
        }
    }

    private static double lerp(double min, double max, double value)
    {
        return min + value * (max - min);
    }
}


