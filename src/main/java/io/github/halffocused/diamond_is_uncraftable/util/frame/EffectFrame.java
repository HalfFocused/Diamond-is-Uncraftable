package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class EffectFrame extends AbstractFrame{

    private EffectInstance effect;
    private double hitboxRange;

    public EffectFrame(int tickIn, EffectInstance effectIn, double hitboxRangeIn) {
        super(tickIn);
        effect = effectIn;
        hitboxRange = hitboxRangeIn;
    }

    public double getHitboxRange(){
        return hitboxRange;
    }

    public EffectInstance getEffectInstance(){
        return effect;
    }


    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {
        if(standEntity.getServer() == null) {return;}
        World world = standEntity.getServer().getWorld(standEntity.world.getDimensionKey());
        if(world == null || standEntity.getServer().getWorld(standEntity.world.getDimensionKey()) == null) {return;}

        AxisAlignedBB hitbox = Util.getAttackHitbox(standEntity, getHitboxRange());

        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standEntity, hitbox);

        for (Entity parseList : listOfEntities) {
            if (parseList instanceof LivingEntity) {
                ((LivingEntity) parseList).addPotionEffect(getEffectInstance());
            }
        }
    }

}
