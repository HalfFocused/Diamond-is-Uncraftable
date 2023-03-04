package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.init.SoundInit;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Random;

public class RadialDamageFrame extends AbstractFrame {

    private float damage;
    private Vector3d motion;
    private double hitboxRange;
    private boolean blockable;

    public RadialDamageFrame(int tickIn, float damageIn, Vector3d motionIn, double hitboxRangeIn, boolean blockableIn) {
        super(tickIn);
        damage = damageIn;
        motion = motionIn;
        hitboxRange = hitboxRangeIn;
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

    public boolean getBlockable(){
        return blockable;
    }

    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {
        if(standEntity.getServer() == null) {return;}
        World world = standEntity.getServer().getWorld(standEntity.world.getDimensionKey());
        if(world == null || standEntity.getServer().getWorld(standEntity.world.getDimensionKey()) == null) {return;}

        standEntity.getServer().getWorld(standEntity.world.getDimensionKey()).getEntities();


        standEntity.getServer().getWorld(standEntity.world.getDimensionKey()).getEntities()
                .filter(entity -> !entity.equals(standEntity))
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> entity.getDistance(standEntity) < getHitboxRange())
                .filter(standEntity::canEntityBeSeen)
                .forEach(entity -> {
                    Util.spawnParticle(standEntity, 2, entity.getPosX(), entity.getEyeHeight() + entity.getPosY(), entity.getPosZ(), 1.2, 1.6, 1.2, 2);

                    int entitiesHit = 0;
                    Random random = ((LivingEntity) entity).getRNG();

                    if(standEntity.getServer() == null) {return;}
                    if(standEntity.getServer().getWorld(standEntity.world.getDimensionKey()) == null) {return;}

                    if (!(entity.equals(standEntity.getMaster())) || (entity.equals(standEntity.getMaster()) && assignedMove.getCanDamageMaster())) {
                        Util.dealStandDamage(standEntity, ((LivingEntity) entity), getDamage(), getMotion(), getBlockable());
                        entitiesHit++;
                    }

                    if (!(entity.equals(standEntity.getMaster())) || (entity.equals(standEntity.getMaster()) && assignedMove.getCanDamageMaster())) {
                        Util.spawnParticle(standEntity, 3, entity.getPosX(), entity.getEyeHeight() + entity.getPosY(), entity.getPosZ(), 2.4, 1.4, 2.4, 1);
                        Util.spawnParticle(standEntity, 4, entity.getPosX() + (random.nextFloat() - 0.5), entity.getEyeHeight() + entity.getPosY() + (random.nextFloat() - 0.5), entity.getPosZ() + (random.nextFloat() - 0.5), 0.7, 0.9, 0.7, (int) (getDamage() * 8.5));
                    }

                    if(entitiesHit > 0){
                        world.playSound(null, standEntity.getPosition(), Util.getHitSound(standEntity), SoundCategory.NEUTRAL, 0.5F, 0.6f / (random.nextFloat() * 0.6f + 1) * 2);
                    }else{
                        world.playSound(null, standEntity.getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 0.25F, 0.6f / (random.nextFloat() * 0.3f + 1) * 2);
                        standEntity.setMostRecentlyDamagedEntity(null);
                    }
                });

        Util.spawnParticle(standEntity, 2, standEntity.getPosX(), standEntity.getEyeHeight() + standEntity.getPosY(), standEntity.getPosZ(), getHitboxRange() * 2, 1.5, getHitboxRange() * 2, 2);
    }
}
