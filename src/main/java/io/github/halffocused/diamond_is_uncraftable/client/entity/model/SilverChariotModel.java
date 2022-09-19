package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.SilverChariotEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.TheWorldEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SilverChariotModel extends AnimatedGeoModel<SilverChariotEntity> {

    @Override
    public ResourceLocation getAnimationFileLocation(SilverChariotEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/silverchariot.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(SilverChariotEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/silverchariot.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SilverChariotEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/silver_chariot.png");
    }
}