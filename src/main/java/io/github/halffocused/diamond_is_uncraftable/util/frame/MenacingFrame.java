package io.github.halffocused.diamond_is_uncraftable.util.frame;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.AbstractStandEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import io.github.halffocused.diamond_is_uncraftable.util.movesets.Move;
import net.minecraft.entity.player.ServerPlayerEntity;

public class MenacingFrame extends AbstractFrame{

    public MenacingFrame(int tickIn){
        super(tickIn);
    }

    @Override
    public void doThing(AbstractStandEntity standEntity, Move assignedMove) {
        if(!standEntity.getMaster().world.isRemote()){
            ServerPlayerEntity playerEntity = (ServerPlayerEntity) standEntity.getMaster();
            Util.giveAdvancement(playerEntity, "menacing");
        }

        Util.spawnParticle(standEntity, 1, standEntity.getPosX(), standEntity.getPosY() + 1.5, standEntity.getPosZ(), 2, 2, 2, 4);

        Util.spawnParticle(standEntity, 1, standEntity.getMaster().getPosX(), standEntity.getMaster().getPosY() + 1.5, standEntity.getMaster().getPosZ(), 2, 2, 2, 4);
    }
}
