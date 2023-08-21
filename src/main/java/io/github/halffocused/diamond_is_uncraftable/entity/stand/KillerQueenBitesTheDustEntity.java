package io.github.halffocused.diamond_is_uncraftable.entity.stand;


import io.github.halffocused.diamond_is_uncraftable.capability.Stand;
import io.github.halffocused.diamond_is_uncraftable.util.globalabilities.BitesTheDustHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class KillerQueenBitesTheDustEntity extends KillerQueenEntity{

    public KillerQueenBitesTheDustEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick(){
        super.tick();
    }

    public void detonate() {
        if (getMaster() == null || world.isRemote()) return;
        if(getMaster().isSneaking()){
            if(!(BitesTheDustHelper.bitesTheDustActive) && spendEnergy(100)){
                BitesTheDustHelper.startBitesTheDust(this);
                Stand.getCapabilityFromPlayer(master).setStandOn(false);
            }
        }else {
            if (spendEnergy(65)) {
                controller.setMoveActive(5);
            }
        }
    }
}

