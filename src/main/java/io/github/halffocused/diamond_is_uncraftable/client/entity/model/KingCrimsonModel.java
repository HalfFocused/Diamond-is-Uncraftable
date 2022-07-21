package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KingCrimsonModel extends AnimatedGeoModel {

    @Override
    public ResourceLocation getAnimationFileLocation(Object entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/kingcrimson.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(Object entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/kingcrimson.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Object entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/king_crimson.png");
    }

    @Override
    public void setLivingAnimations(IAnimatable entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
    }
}