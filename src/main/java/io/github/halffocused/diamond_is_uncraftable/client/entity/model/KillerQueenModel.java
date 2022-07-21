package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KillerQueenModel extends AnimatedGeoModel {

    @Override
    public ResourceLocation getAnimationFileLocation(Object entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/killerqueen.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(Object entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/killerqueen.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Object entity) {

        /*
        AtomicReference<ResourceLocation> returnValue = new AtomicReference<>();

        if(entity instanceof KillerQueenEntity) {
            Stand.getLazyOptional(((KillerQueenEntity) entity).getMaster()).ifPresent(props -> {

                if (props.getAbilitiesUnlocked() >= 1) {
                    returnValue.set(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen_btd.png"));
                }else{
                    returnValue.set(new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen.png"));
                }

            });

            return returnValue.get();
        } */

        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen.png");
    }

    @Override
    public void setLivingAnimations(IAnimatable entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
    }
}