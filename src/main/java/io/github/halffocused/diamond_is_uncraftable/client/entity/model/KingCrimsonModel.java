package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KingCrimsonEntity;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.PurpleHazeEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KingCrimsonModel extends AnimatedGeoModel<KingCrimsonEntity> {

    @Override
    public ResourceLocation getAnimationFileLocation(KingCrimsonEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/kingcrimson.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(KingCrimsonEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/kingcrimson.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(KingCrimsonEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/king_crimson.png");
    }
}