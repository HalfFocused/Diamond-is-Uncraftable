package io.github.halffocused.diamond_is_uncraftable.util;


import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.CheckForNull;
import java.util.Optional;
import java.util.function.Predicate;

public class HitscanHelper {

    @CheckForNull
    public static EntityRayTraceResult getEntityFromRaytrace(LivingEntity entityIn, double range)
    {
        return getEntityFromRaytrace(entityIn, range, 1.0F);
    }

    @CheckForNull
    public static EntityRayTraceResult getEntityFromRaytrace(LivingEntity startEntity, double range, float ticks) {
        World world = startEntity.getEntityWorld();

        Vec3d look = startEntity.getLookVec();
        Vec3d start = startEntity.getEyePosition(ticks);

        Vec3d end = new Vec3d(startEntity.getPosX() + look.x * range, startEntity.getPosYEye() + look.y * range, startEntity.getPosZ() + look.z * range);
        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, startEntity);

        RayTraceResult rayTraceResult = world.rayTraceBlocks(context);
        double traceDistance = rayTraceResult.getHitVec().squareDistanceTo(start);

        AxisAlignedBB playerBox = startEntity.getBoundingBox().expand(look.scale(traceDistance)).expand(1.0D, 1.0D, 1.0D);

        Predicate<Entity> filter = entity -> !entity.isSpectator() && entity.canBeCollidedWith() && entity instanceof LivingEntity;
        for (Entity possible : world.getEntitiesInAABBexcluding(startEntity, playerBox, filter)) {
            AxisAlignedBB entityBox = possible.getBoundingBox().grow(0.3D);
            Optional<Vec3d> optional = entityBox.rayTrace(start, end);
            if (optional.isPresent()) {
                Vec3d position = optional.get();
                double distance = start.squareDistanceTo(position);

                if (distance < traceDistance) {
                    return new EntityRayTraceResult(possible, position);
                }
            }
        }
        return null;
    }


    @CheckForNull
    public static EntityRayTraceResult getEntityFromRaytracePos(LivingEntity entityIn, double range)
    {
        return getEntityFromRaytrace(entityIn, range, 1.0F);
    }

    @CheckForNull
    public static EntityRayTraceResult getEntityFromRaytracePos(Vec3d positionIn, Vec3d vectorIn, AxisAlignedBB boundingBoxIn, LivingEntity ignoreEntity, World worldIn, double range, float ticks) {
        World world = worldIn;

        Vec3d look = vectorIn;
        Vec3d start = positionIn;

        Vec3d end = new Vec3d(start.x + look.x * range, start.y + look.y * range, start.z + look.z * range);
        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);

        RayTraceResult rayTraceResult = world.rayTraceBlocks(context);
        double traceDistance = rayTraceResult.getHitVec().squareDistanceTo(start);

        AxisAlignedBB playerBox = boundingBoxIn.expand(look.scale(traceDistance)).expand(1.0D, 1.0D, 1.0D);

        Predicate<Entity> filter = entity -> !entity.isSpectator() && entity.canBeCollidedWith() && entity instanceof LivingEntity;
        for (Entity possible : world.getEntitiesInAABBexcluding(ignoreEntity, playerBox, filter)) {
            AxisAlignedBB entityBox = possible.getBoundingBox().grow(0.3D);
            Optional<Vec3d> optional = entityBox.rayTrace(start, end);
            if (optional.isPresent()) {
                Vec3d position = optional.get();
                double distance = start.squareDistanceTo(position);

                if (distance < traceDistance) {
                    return new EntityRayTraceResult(possible, position);
                }
            }
        }
        return null;
    }


    @CheckForNull
    public static EntityRayTraceResult traceToEntity(PlayerEntity player, Entity target)
    {
        return traceToEntity(player, target, 1.0F);
    }

    @CheckForNull
    public static EntityRayTraceResult traceToEntity(PlayerEntity player, Entity target, float ticks) {
        Vec3d start = player.getEyePosition(ticks);
        Vec3d end = target.getPositionVec();

        AxisAlignedBB targetBox = target.getBoundingBox().grow(0.3D);
        Optional<Vec3d> optional = targetBox.rayTrace(start, end);

        return optional.map(vector3d -> new EntityRayTraceResult(target, vector3d)).orElse(null);
    }
}
