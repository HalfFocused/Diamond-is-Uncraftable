package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class DamageFrame extends AbstractFrame {

    private float damage;
    private Vector3d motion;
    private double hitboxRange;
    private int pierce;
    private boolean blockable;

    public DamageFrame(int tickIn, float damageIn, Vector3d motionIn, double hitboxRangeIn, int pierceIn, boolean blockableIn) {
        super(tickIn);
        damage = damageIn;
        motion = motionIn;
        hitboxRange = hitboxRangeIn;
        pierce = pierceIn;
        blockable = blockableIn;
    }

    public float getDamage(){
        return damage;
    }

    public Vector3d getMotion(){
        return motion;
    }

    public double getHitboxRange(){
        return hitboxRange;
    }

    public int getPierce(){
        return pierce;
    }

    public boolean getBlockable(){
        return blockable;
    }

    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {
        if(standEntity.getServer() == null) {return;}
        World world = standEntity.getServer().getWorld(standEntity.world.getDimensionKey());
        if(world == null) {return;}

        AxisAlignedBB hitbox = Util.getAttackHitbox(standEntity, getHitboxRange());

        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standEntity, hitbox);

        Random random = standEntity.getRNG();
        boolean soundFlag = false;
        int entitiesHit = 0;

        for (Entity entity : listOfEntities) {
            if (entity instanceof LivingEntity) {
                if (entity != standEntity && entitiesHit < getPierce()) {
                    if (!(entity.equals(standEntity.getMaster())) || (entity.equals(standEntity.getMaster()) && assignedMove.getCanDamageMaster())) {

                        Util.dealStandDamage(standEntity, (LivingEntity) entity, getDamage(), getMotion(), getBlockable());

                        entitiesHit++;
                    }
                }
            }
        }

        if(entitiesHit > 0){
            world.playSound(null, standEntity.getPosition(), Util.getHitSound(standEntity), SoundCategory.NEUTRAL, 0.5F, 0.6f / (random.nextFloat() * 0.3f + 1) * 2);
        }else{
            world.playSound(null, standEntity.getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 0.25F, 0.6f / (random.nextFloat() * 0.3f + 1) * 2);
            standEntity.setMostRecentlyDamagedEntity(null);
        }
    }
}
