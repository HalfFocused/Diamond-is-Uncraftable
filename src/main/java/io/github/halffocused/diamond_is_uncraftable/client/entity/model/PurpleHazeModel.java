package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.PurpleHazeEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.SilverChariotEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PurpleHazeModel extends AnimatedGeoModel<PurpleHazeEntity> {

    @Override
    public ResourceLocation getAnimationFileLocation(PurpleHazeEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/purplehaze.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(PurpleHazeEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/purplehaze.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(PurpleHazeEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/purple_haze.png");
    }
}