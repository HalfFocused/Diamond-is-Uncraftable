package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.DiamondIsUncraftable;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.KillerQueenEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KillerQueenModel extends AnimatedGeoModel<KillerQueenEntity> {

    @Override
    public ResourceLocation getAnimationFileLocation(KillerQueenEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "animations/killerqueen.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(KillerQueenEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "geo/killerqueen.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(KillerQueenEntity entity) {
        return new ResourceLocation(DiamondIsUncraftable.MOD_ID, "textures/stands/killer_queen.png");
    }
}