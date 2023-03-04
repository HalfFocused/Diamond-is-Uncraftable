package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class SetBombFrame extends AbstractFrame{

    private double hitboxRange;

    public SetBombFrame(int tickIn, double hitboxRangeIn) {
        super(tickIn);
        hitboxRange = hitboxRangeIn;
    }

    public double getHitboxRange(){
        return hitboxRange;
    }

    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {
        if(standEntity.getServer() == null) {return;}
        World world = standEntity.getServer().getWorld(standEntity.world.getDimensionKey());
        if(world == null || standEntity.getServer().getWorld(standEntity.world.getDimensionKey()) == null) {return;}

        AxisAlignedBB hitbox = Util.getAttackHitbox(standEntity, getHitboxRange(), 0.5);

        List<Entity> listOfEntities = world.getEntitiesWithinAABBExcludingEntity(standEntity, hitbox);

        boolean blockedFlag = false;
        
        for(Entity entity : listOfEntities){
            if(entity instanceof LivingEntity && !(entity instanceof AbstractStandEntity) && !(entity instanceof EnderDragonEntity)){
                if(entity != standEntity.getMaster() && standEntity instanceof KillerQueenEntity){ //There should be no way this frame gets called without the stand being Killer Queen, but I would find a way
                    LivingEntity bombTarget = (LivingEntity) entity;
                    if (entity instanceof PlayerEntity) {
                        if (bombTarget.isActiveItemStackBlocking()) {
                            entity.getHeldEquipment().forEach(itemStack -> {
                                if (itemStack.getItem().equals(Items.SHIELD)) {
                                    itemStack.damageItem(50, ((PlayerEntity) entity), (playerEntity) -> {
                                        playerEntity.sendBreakAnimation(Hand.MAIN_HAND);
                                        playerEntity.sendBreakAnimation(Hand.OFF_HAND);
                                    });
                                }
                            });
                            blockedFlag = true;
                        }
                    }

                    Util.spawnParticle(standEntity, 14, entity.getPosX(), entity.getPosY() + 1, entity.getPosZ(), 1, 1, 1, 20);

                    ((KillerQueenEntity) standEntity).removeFirstBombFromAll();

                    Stand stand = Stand.getCapabilityFromPlayer(standEntity.getMaster());


                    if(bombTarget.getHealth() / bombTarget.getMaxHealth() <= 0.15 || bombTarget.getHealth() <= 3){
                        ((KillerQueenEntity) standEntity).bombEntity = bombTarget;
                        stand.setBombEntityId(bombTarget.getEntityId());
                        standEntity.getController().setMoveActive(8);
                    }else{
                        ((KillerQueenEntity) standEntity).bombEntity = bombTarget;
                        stand.setBombEntityId(bombTarget.getEntityId());
                    }
                }
            }
        }

    }
}
