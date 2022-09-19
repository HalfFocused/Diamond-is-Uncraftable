package io.github.halffocused.diamond_is_uncraftable.util;


import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
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

        Vector3d look = startEntity.getLookVec();
        Vector3d start = startEntity.getEyePosition(ticks);

        Vector3d end = new Vector3d(startEntity.getPosX() + look.x * range, startEntity.getPosYEye() + look.y * range, startEntity.getPosZ() + look.z * range);
        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, startEntity);

        RayTraceResult rayTraceResult = world.rayTraceBlocks(context);
        double traceDistance = rayTraceResult.getHitVec().squareDistanceTo(start);

        AxisAlignedBB playerBox = startEntity.getBoundingBox().expand(look.scale(traceDistance)).expand(1.0D, 1.0D, 1.0D);

        Predicate<Entity> filter = entity -> !entity.isSpectator() && entity.canBeCollidedWith() && entity instanceof LivingEntity;
        for (Entity possible : world.getEntitiesInAABBexcluding(startEntity, playerBox, filter)) {
            AxisAlignedBB entityBox = possible.getBoundingBox().grow(0.3D);
            Optional<Vector3d> optional = entityBox.rayTrace(start, end);
            if (optional.isPresent()) {
                Vector3d position = optional.get();
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
    public static EntityRayTraceResult getEntityFromRaytracePos(Vector3d positionIn, Vector3d vectorIn, AxisAlignedBB boundingBoxIn, LivingEntity ignoreEntity, World worldIn, double range, float ticks) {

        Vector3d end = new Vector3d(positionIn.x + vectorIn.x * range, positionIn.y + vectorIn.y * range, positionIn.z + vectorIn.z * range);
        RayTraceContext context = new RayTraceContext(positionIn, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);

        RayTraceResult rayTraceResult = worldIn.rayTraceBlocks(context);
        double traceDistance = rayTraceResult.getHitVec().squareDistanceTo(positionIn);

        AxisAlignedBB playerBox = boundingBoxIn.expand(vectorIn.scale(traceDistance)).expand(1.0D, 1.0D, 1.0D);

        Predicate<Entity> filter = entity -> !entity.isSpectator() && entity.canBeCollidedWith() && entity instanceof LivingEntity;
        for (Entity possible : worldIn.getEntitiesInAABBexcluding(ignoreEntity, playerBox, filter)) {
            AxisAlignedBB entityBox = possible.getBoundingBox().grow(0.3D);
            Optional<Vector3d> optional = entityBox.rayTrace(positionIn, end);
            if (optional.isPresent()) {
                Vector3d position = optional.get();
                double distance = positionIn.squareDistanceTo(position);

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
        Vector3d start = player.getEyePosition(ticks);
        Vector3d end = target.getPositionVec();

        AxisAlignedBB targetBox = target.getBoundingBox().grow(0.3D);
        Optional<Vector3d> optional = targetBox.rayTrace(start, end);

        return optional.map(vector3d -> new EntityRayTraceResult(target, vector3d)).orElse(null);
    }
}
